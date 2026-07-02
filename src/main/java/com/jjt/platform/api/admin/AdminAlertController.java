package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.AlertResponse;
import com.jjt.platform.api.admin.service.AdminAlertService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.AdminAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminAlertController {

    private final AdminAlertService alertService;

    public AdminAlertController(AdminAlertService alertService) {
        this.alertService = alertService;
    }

    /** Returns all undismissed alerts for the authenticated user's organisation. */
    @GetMapping
    public ResponseEntity<List<AlertResponse>> listActive(
            @AuthenticationPrincipal JwtUserDetails principal) {
        List<AlertResponse> alerts = alertService.listActive(principal.getOrgId())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(alerts);
    }

    /** Dismisses an alert so it no longer appears in the active list. */
    @PostMapping("/{id}/dismiss")
    public ResponseEntity<AlertResponse> dismiss(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        AdminAlert alert = alertService.dismiss(id, principal.getOrgId(), principal.getId());
        return ResponseEntity.ok(toResponse(alert));
    }

    private AlertResponse toResponse(AdminAlert a) {
        return new AlertResponse(
                a.getId(),
                a.getAlertType().name(),
                a.getSeverity().name(),
                a.getTitle(),
                a.getMessage(),
                a.getRelatedEntityId(),
                a.getRelatedEntityType(),
                a.isDismissed(),
                a.getDismissedAt(),
                a.getCreatedAt()
        );
    }
}
