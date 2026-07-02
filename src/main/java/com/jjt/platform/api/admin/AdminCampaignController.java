package com.jjt.platform.api.admin;

import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.CampaignEntity;
import com.jjt.platform.infrastructure.persistence.repository.CampaignJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/campaigns")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminCampaignController {

    private final CampaignJpaRepository campaignRepo;

    public AdminCampaignController(CampaignJpaRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CampaignResponse> listCampaigns(@AuthenticationPrincipal JwtUserDetails principal) {
        return campaignRepo.findByOrganisationId(principal.getOrgId())
                .stream().map(this::toResponse).toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CampaignResponse> createCampaign(
            @RequestBody CreateCampaignRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Instant now = Instant.now();
        CampaignEntity entity = new CampaignEntity(
                UUID.randomUUID(),
                principal.getOrgId(),
                request.name(),
                request.description(),
                request.targetAmount(),
                request.targetCurrency() != null ? request.targetCurrency() : "PKR",
                "DRAFT",
                request.startDate(),
                request.endDate(),
                request.fundAccountId(),
                principal.getId(),
                now,
                now
        );
        CampaignEntity saved = campaignRepo.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CampaignResponse> getCampaign(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        CampaignEntity entity = campaignRepo.findById(id)
                .orElseThrow(() -> new DomainException("Campaign not found"));
        if (!entity.getOrganisationId().equals(principal.getOrgId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(toResponse(entity));
    }

    @PostMapping("/{id}/open")
    @Transactional
    public ResponseEntity<CampaignResponse> openCampaign(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        CampaignEntity entity = campaignRepo.findById(id)
                .orElseThrow(() -> new DomainException("Campaign not found"));
        if (!entity.getOrganisationId().equals(principal.getOrgId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new DomainException("Only DRAFT campaigns can be opened");
        }
        entity.setStatus("ACTIVE");
        entity.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(toResponse(campaignRepo.save(entity)));
    }

    @PostMapping("/{id}/close")
    @Transactional
    public ResponseEntity<CampaignResponse> closeCampaign(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        CampaignEntity entity = campaignRepo.findById(id)
                .orElseThrow(() -> new DomainException("Campaign not found"));
        if (!entity.getOrganisationId().equals(principal.getOrgId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new DomainException("Only ACTIVE campaigns can be closed");
        }
        entity.setStatus("CLOSED");
        entity.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(toResponse(campaignRepo.save(entity)));
    }

    private CampaignResponse toResponse(CampaignEntity e) {
        return new CampaignResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getTargetAmount(),
                e.getTargetCurrency(),
                e.getStatus(),
                e.getStartDate(),
                e.getEndDate(),
                e.getFundAccountId(),
                e.getCreatedAt()
        );
    }

    public record CampaignResponse(
            UUID id,
            String name,
            String description,
            BigDecimal targetAmount,
            String targetCurrency,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            UUID fundAccountId,
            Instant createdAt
    ) {}

    public record CreateCampaignRequest(
            String name,
            String description,
            BigDecimal targetAmount,
            String targetCurrency,
            LocalDate startDate,
            LocalDate endDate,
            UUID fundAccountId
    ) {}
}
