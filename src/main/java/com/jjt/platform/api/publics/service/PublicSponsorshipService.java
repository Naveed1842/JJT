package com.jjt.platform.api.publics.service;

import com.jjt.platform.application.usecase.CommitFutureSponsorshipUseCase;
import com.jjt.platform.application.usecase.CreateSponsorUseCase;
import com.jjt.platform.core.domain.entity.Sponsor;
import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import com.jjt.platform.core.domain.value.YearMonthValue;
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

    public PublicSponsorshipService(ChildJpaRepository childRepo,
                                    SponsorJpaRepository sponsorRepo,
                                    SponsorshipJpaRepository sponsorshipRepo) {
        this.childRepo = childRepo;
        this.sponsorRepo = sponsorRepo;
        this.sponsorshipRepo = sponsorshipRepo;
    }

    @Transactional
    public Sponsorship commitPublic(UUID childId, String sponsorName, String sponsorEmail, String sponsorPhone) {
        if (childId == null) {
            throw new DomainException("childId must not be null");
        }
        if (sponsorName == null || sponsorName.isBlank()) {
            throw new DomainException("sponsor name must not be blank");
        }
        if (sponsorEmail == null || sponsorEmail.isBlank()) {
            throw new DomainException("sponsor email must not be blank");
        }
        if (childRepo.findById(childId).isEmpty()) {
            throw new DomainException("Child not found");
        }

        boolean hasActiveSponsorship = sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE);
        if (hasActiveSponsorship) {
            throw new SponsorshipInvariantViolationException("Child already has an active sponsorship.");
        }

        Sponsor sponsor = createSponsorUseCase.create(
                new CreateSponsorUseCase.Command(null, sponsorName.trim(), sponsorEmail.trim(), sponsorPhone));
        SponsorEntity sponsorEntity = SponsorMapper.toEntity(sponsor);
        sponsorRepo.save(sponsorEntity);

        YearMonthValue startMonth = YearMonthValue.of(YearMonth.now().plusMonths(1));
        Sponsorship sponsorship = commitFutureSponsorshipUseCase.commit(
                new CommitFutureSponsorshipUseCase.Command(null, sponsor.getId(), childId, startMonth,
                        false, SponsorshipStatus.PENDING, Instant.now(), null));
        SponsorshipEntity sponsorshipEntity = SponsorshipMapper.toEntity(sponsorship, sponsorEntity);
        sponsorshipRepo.save(sponsorshipEntity);

        return sponsorship;
    }
}
