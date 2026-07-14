package com.jjt.platform.api.media;

import com.jjt.platform.api.media.dto.BulkImportJobResponse;
import com.jjt.platform.api.media.service.BulkImportService;
import com.jjt.platform.config.security.JwtUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/media/import")
public class BulkImportController {

    private final BulkImportService bulkImportService;

    public BulkImportController(BulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    /**
     * Upload a ZIP of child photos.
     * Each image filename stem must match a child's roll number (e.g. "A123.jpg").
     * Returns 202 with Location: /api/media/import/{jobId} for polling.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BulkImportJobResponse> startImport(
            @RequestParam("file") MultipartFile archive,
            @AuthenticationPrincipal JwtUserDetails principal) throws IOException {

        if (archive.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UUID jobId = bulkImportService.createJob(archive, principal.getOrgId(), principal.getId());
        BulkImportJobResponse status = bulkImportService.getJob(jobId, principal.getOrgId());
        return ResponseEntity.accepted()
                .location(URI.create("/api/media/import/" + jobId))
                .body(status);
    }

    @GetMapping("/{jobId}")
    public BulkImportJobResponse getStatus(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return bulkImportService.getJob(jobId, principal.getOrgId());
    }
}
