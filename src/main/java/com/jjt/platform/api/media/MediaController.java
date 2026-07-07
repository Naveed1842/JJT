package com.jjt.platform.api.media;

import com.jjt.platform.api.media.dto.MediaAttachmentResponse;
import com.jjt.platform.api.media.dto.MediaConfirmRequest;
import com.jjt.platform.api.media.dto.MediaFileResponse;
import com.jjt.platform.api.media.dto.MediaUploadIntentRequest;
import com.jjt.platform.api.media.dto.MediaUploadIntentResponse;
import com.jjt.platform.api.media.service.MediaService;
import com.jjt.platform.config.security.JwtUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /** Step 1: Request an upload slot and get a pre-signed PUT URL. */
    @PostMapping("/upload-intent")
    public MediaUploadIntentResponse uploadIntent(
            @Valid @RequestBody MediaUploadIntentRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return mediaService.createUploadIntent(req, principal.getOrgId(), principal.getId());
    }

    /** Step 2: Confirm the upload landed; triggers async variant generation. */
    @PostMapping("/{mediaId}/confirm")
    public MediaFileResponse confirm(
            @PathVariable UUID mediaId,
            @RequestBody(required = false) MediaConfirmRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return mediaService.confirmUpload(mediaId, req != null ? req : new MediaConfirmRequest(null), principal.getId());
    }

    @GetMapping("/{mediaId}")
    public MediaFileResponse getMedia(@PathVariable UUID mediaId) {
        return mediaService.getMedia(mediaId);
    }

    /** Returns the serving URL — CDN URL for PUBLIC assets, presigned GET URL for PRIVATE. */
    @GetMapping("/{mediaId}/url")
    public ResponseEntity<String> getUrl(@PathVariable UUID mediaId) {
        return ResponseEntity.ok(mediaService.getMediaUrl(mediaId));
    }

    @GetMapping("/attachments")
    public List<MediaAttachmentResponse> listAttachments(
            @RequestParam String ownerType,
            @RequestParam UUID ownerId,
            @RequestParam(required = false) String role) {
        return mediaService.listAttachments(ownerType, ownerId, role);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        mediaService.softDelete(mediaId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
