package com.jjt.platform.api.publics.service;

import com.jjt.platform.api.admin.service.AdminAlertService;
import com.jjt.platform.application.usecase.CommitFutureSponsorshipUseCase;
import com.jjt.platform.application.usecase.CreateSponsorUseCase;
import com.jjt.platform.core.domain.entity.AlertSeverity;
import com.jjt.platform.core.domain.entity.AlertType;
import com.jjt.platform.core.domain.entity.Sponsor;
import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.audit.AuditService;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorMapper;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorshipMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class PublicSponsorshipService {

    private final CreateSponsorUseCase createSponsorUseCase = new CreateSponsorUseCase();
    private final CommitFutureSponsorshipUseCase commitFutureSponsorshipUseCase = new CommitFutureSponsorshipUseCase();

    private final ChildJpaRepository childRepo;
    private final SponsorJpaRepository sponsorRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final AdminAlertService alertService;
    private final AuditService auditService;

    public PublicSponsorshipService(ChildJpaRepository childRepo,
                                    SponsorJpaRepository sponsorRepo,
                                    SponsorshipJpaRepository sponsorshipRepo,
                                    AdminAlertService alertService,
                                    AuditService auditService) {
        this.childRepo = childRepo;
        this.sponsorRepo = sponsorRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    @Transactional
    public Sponsorship commitPublic(UUID childId, com.jjt.platform.core.domain.entity.CommitmentType commitmentType,
                                    String sponsorName, String sponsorEmail, String sponsorPhone) {
        if (childId == null) {
            throw new DomainException("childId must not be null");
        }
        if (commitmentType == null) {
            throw new DomainException("commitmentType must not be null");
        }
        if (sponsorName == null || sponsorName.isBlank()) {
            throw new DomainException("sponsor name must not be blank");
        }
        if (sponsorEmail == null || sponsorEmail.isBlank()) {
            throw new DomainException("sponsor email must not be blank");
        }
        var childEntity = childRepo.findById(childId)
                .orElseThrow(() -> new DomainException("Child not found"));
        UUID orgId = childEntity.getOrganisationId();

        if (sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE)) {
            throw new SponsorshipInvariantViolationException("Child already has an active sponsorship.");
        }
        if (sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.PENDING)) {
            throw new SponsorshipInvariantViolationException("Child already has a pending sponsorship.");
        }

        SponsorEntity sponsorEntity = sponsorRepo.findByContactEmail(sponsorEmail.trim())
                .orElseGet(() -> {
                    Sponsor newSponsor = createSponsorUseCase.create(
                            new CreateSponsorUseCase.Command(null, sponsorName.trim(), sponsorEmail.trim(), sponsorPhone));
                    return sponsorRepo.save(SponsorMapper.toEntity(newSponsor, orgId));
                });
        Sponsor sponsor = SponsorMapper.toDomain(sponsorEntity);

        YearMonthValue startMonth = YearMonthValue.of(YearMonth.now().plusMonths(1));
        // Public commits have no authenticated author; createdBy is null (nullable for Phase 1 / public path).
        Sponsorship sponsorship = commitFutureSponsorshipUseCase.commit(
                new CommitFutureSponsorshipUseCase.Command(null, sponsor.getId(), childId, startMonth,
                        false, Instant.now(), null, commitmentType, null));
        SponsorshipEntity sponsorshipEntity = SponsorshipMapper.toEntity(sponsorship, sponsorEntity, orgId);
        sponsorshipRepo.save(sponsorshipEntity);

        // Notify admins: a public commitment sits PENDING until an admin activates it.
        alertService.raise(orgId, AlertType.GENERAL, AlertSeverity.INFO,
                "New sponsorship commitment: " + childEntity.getFullName(),
                String.format("%s (%s) committed to sponsor %s (%s). Verify payment and activate the sponsorship.",
                        sponsorName.trim(), sponsorEmail.trim(),
                        childEntity.getFullName(), commitmentType.name()),
                sponsorship.getId(), "Sponsorship");
        auditService.log(orgId, "PUBLIC_SPONSORSHIP_COMMITTED", null, sponsorEmail.trim(),
                "Sponsorship", sponsorship.getId(),
                "Public sponsorship commitment by " + sponsorName.trim() + " for child "
                        + childEntity.getFullName() + " starting " + startMonth.getValue());

        return sponsorship;
    }
}
