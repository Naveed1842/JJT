package com.jjt.platform.api.publics;

import com.jjt.platform.infrastructure.persistence.entity.CampaignEntity;
import com.jjt.platform.infrastructure.persistence.repository.CampaignJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/campaigns")
public class PublicCampaignController {

    private final CampaignJpaRepository campaignRepo;

    public PublicCampaignController(CampaignJpaRepository campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PublicCampaignResponse> listActiveCampaigns() {
        return campaignRepo.findByStatus("ACTIVE")
                .stream().map(this::toResponse).toList();
    }

    private PublicCampaignResponse toResponse(CampaignEntity e) {
        return new PublicCampaignResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getTargetAmount(),
                e.getTargetCurrency(),
                e.getStartDate(),
                e.getEndDate(),
                e.getCreatedAt()
        );
    }

    public record PublicCampaignResponse(
            UUID id,
            String name,
            String description,
            BigDecimal targetAmount,
            String targetCurrency,
            LocalDate startDate,
            LocalDate endDate,
            Instant createdAt
    ) {}
}
