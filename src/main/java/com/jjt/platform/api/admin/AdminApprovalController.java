package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.ApprovalEngineService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalCheckResultEntity;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalCheckResultJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalRequestJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/approvals")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminApprovalController {

    private final ApprovalRequestJpaRepository requestRepo;
    private final ApprovalCheckResultJpaRepository checkRepo;
    private final ApprovalEngineService approvalEngine;

    public AdminApprovalController(ApprovalRequestJpaRepository requestRepo,
                                   ApprovalCheckResultJpaRepository checkRepo,
                                   ApprovalEngineService approvalEngine) {
        this.requestRepo = requestRepo;
        this.checkRepo = checkRepo;
        this.approvalEngine = approvalEngine;
    }

    @GetMapping
    public Page<ApprovalResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Page<ApprovalRequestEntity> page;
        if (status != null) {
            page = requestRepo.findByOrgIdAndStatusOrderByCreatedAtDesc(principal.getOrgId(), status, pageable);
        } else if (entityType != null) {
            page = requestRepo.findByOrgIdAndEntityTypeOrderByCreatedAtDesc(principal.getOrgId(), entityType, pageable);
        } else {
            page = requestRepo.findByOrgIdOrderByCreatedAtDesc(principal.getOrgId(), pageable);
        }
        return page.map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ApprovalDetailResponse get(@PathVariable UUID id) {
        ApprovalRequestEntity req = requestRepo.findById(id)
                .orElseThrow(() -> new com.jjt.platform.core.domain.exceptions.DomainException("Not found"));
        List<ApprovalCheckResultEntity> checks = checkRepo.findByRequestId(id);
        return new ApprovalDetailResponse(toResponse(req), checks.stream().map(this::toCheckResponse).toList());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovalResponse> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal JwtUserDetails principal) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(toResponse(approvalEngine.resolve(id, principal.getId(), true, notes)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApprovalResponse> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal JwtUserDetails principal) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(toResponse(approvalEngine.resolve(id, principal.getId(), false, notes)));
    }

    private ApprovalResponse toResponse(ApprovalRequestEntity r) {
        return new ApprovalResponse(r.getId(), r.getOrgId(), r.getEntityType(), r.getEntityId(),
                r.getStatus(), r.getRequestedBy(), r.getApprovedBy(), r.getReviewedAt(),
                r.getNotes(), r.getCreatedAt());
    }

    private CheckResponse toCheckResponse(ApprovalCheckResultEntity c) {
        return new CheckResponse(c.getId(), c.getCheckType(), c.getVerdict(),
                c.getConfidence(), c.getExplanation(), c.getRanAt());
    }

    public record ApprovalResponse(UUID id, UUID orgId, String entityType, UUID entityId,
                                   String status, UUID requestedBy, UUID approvedBy,
                                   Instant reviewedAt, String notes, Instant createdAt) {}
    public record CheckResponse(UUID id, String checkType, String verdict,
                                BigDecimal confidence, String explanation, Instant ranAt) {}
    public record ApprovalDetailResponse(ApprovalResponse request, List<CheckResponse> checks) {}
}
