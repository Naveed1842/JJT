package com.jjt.platform.api.admin;

import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.AuditEventEntity;
import com.jjt.platform.infrastructure.persistence.repository.AuditEventJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit-log")
@PreAuthorize("hasRole('JJT_ADMIN')")
public class AdminAuditController {

    private final AuditEventJpaRepository auditRepo;

    public AdminAuditController(AuditEventJpaRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<AuditEventResponse>> listAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditEventResponse> result = auditRepo
                .findByOrganisationIdOrderByCreatedAtDesc(principal.getOrgId(), pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{entityType}/{entityId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AuditEventResponse>> listByEntity(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") UUID entityId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        List<AuditEventResponse> result = auditRepo
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    private AuditEventResponse toResponse(AuditEventEntity e) {
        return new AuditEventResponse(
                e.getId(),
                e.getOrganisationId(),
                e.getEventType(),
                e.getActorId(),
                e.getActorEmail(),
                e.getEntityType(),
                e.getEntityId(),
                e.getDescription(),
                e.getCreatedAt()
        );
    }

    public record AuditEventResponse(
            UUID id,
            UUID organisationId,
            String eventType,
            UUID actorId,
            String actorEmail,
            String entityType,
            UUID entityId,
            String description,
            Instant createdAt
    ) {}
}
