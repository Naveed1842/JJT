package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.MissionService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.MissionNodeEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/missions")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminMissionController {

    private final MissionService missionService;

    public AdminMissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<NodeResponse> listRoots(@AuthenticationPrincipal JwtUserDetails principal) {
        return missionService.listRoots(principal.getOrgId()).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public NodeResponse get(@PathVariable UUID id) {
        return toResponse(missionService.get(id));
    }

    @GetMapping("/{id}/children")
    public List<NodeResponse> children(@PathVariable UUID id) {
        return missionService.listChildren(id).stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<NodeResponse> create(
            @RequestBody CreateNodeRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        MissionNodeEntity node = missionService.create(principal.getOrgId(), req.parentId(),
                req.kind(), req.name(), req.description(), req.startDate(), req.endDate(), req.targetAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(node));
    }

    @PutMapping("/{id}")
    public NodeResponse update(@PathVariable UUID id, @RequestBody UpdateNodeRequest req) {
        return toResponse(missionService.update(id, req.name(), req.description(),
                req.status(), req.startDate(), req.endDate(), req.targetAmount()));
    }

    @GetMapping("/{id}/financials")
    public MissionService.MissionFinancials financials(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        LocalDate start = LocalDate.now().withDayOfYear(1);
        LocalDate end   = LocalDate.now();
        return missionService.getFinancials(id, start, end);
    }

    private NodeResponse toResponse(MissionNodeEntity n) {
        return new NodeResponse(n.getId(), n.getOrgId(), n.getParentId(), n.getKind(),
                n.getName(), n.getDescription(), n.getStatus(), n.getStartDate(),
                n.getEndDate(), n.getTargetAmount(), n.getCreatedAt());
    }

    public record CreateNodeRequest(@NotBlank String kind, @NotBlank String name, String description,
                                    UUID parentId, LocalDate startDate, LocalDate endDate,
                                    BigDecimal targetAmount) {}
    public record UpdateNodeRequest(String name, String description, String status,
                                    LocalDate startDate, LocalDate endDate, BigDecimal targetAmount) {}
    public record NodeResponse(UUID id, UUID orgId, UUID parentId, String kind, String name,
                               String description, String status, LocalDate startDate,
                               LocalDate endDate, BigDecimal targetAmount, Instant createdAt) {}
}
