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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicSponsorshipService {

    private final CreateSponsorUseCase createSponsorUseCase = new CreateSponsorUseCase();
    private final CommitFutureSponsorshipUseCase commitFutureSponsorshipUseCase = new CommitFutureSponsorshipUseCase();

    private final ChildJpaRepository childRepo;
    private final SponsorJpaRepository sponsorRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;

    @Transactional
    public Sponsorship commitPublic(UUID childId, com.jjt.platform.core.domain.entity.CommitmentType commitmentType,
                                    String sponsorName, String sponsorEmail, String sponsorPhone) {
        log.info("Committing public sponsorship for child: {} by sponsor: {}", childId, sponsorEmail);
        Validate.notNull(childId, "Child ID cannot be null");
        Validate.notNull(commitmentType, "Commitment type cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(sponsorName), "Sponsor name cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(sponsorEmail), "Sponsor email cannot be blank");
        
        try {
            if (childRepo.findById(childId).isEmpty()) {
                throw new DomainException("Child not found");
            }

            boolean hasActiveSponsorship = sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE);
            if (hasActiveSponsorship) {
                throw new SponsorshipInvariantViolationException("Child already has an active sponsorship.");
            }

            Sponsor sponsor = createSponsorUseCase.create(
                    new CreateSponsorUseCase.Command(null, 
                            StringUtils.trim(sponsorName), 
                            StringUtils.trim(sponsorEmail), 
                            StringUtils.trimToNull(sponsorPhone)));
            SponsorEntity sponsorEntity = SponsorMapper.toEntity(sponsor);
            sponsorRepo.save(sponsorEntity);

            YearMonthValue startMonth = YearMonthValue.of(YearMonth.now().plusMonths(1));
            Sponsorship sponsorship = commitFutureSponsorshipUseCase.commit(
                    new CommitFutureSponsorshipUseCase.Command(null, sponsor.getId(), childId, startMonth,
                            false, SponsorshipStatus.PENDING, Instant.now(), null, commitmentType));
            SponsorshipEntity sponsorshipEntity = SponsorshipMapper.toEntity(sponsorship, sponsorEntity);
            sponsorshipRepo.save(sponsorshipEntity);

            log.info("Successfully committed public sponsorship for child: {} by sponsor: {} with ID: {}", 
                    childId, sponsorEmail, sponsorship.getId());
            return sponsorship;
        } catch (Exception e) {
            log.error("Failed to commit public sponsorship for child: {} by sponsor: {}: {}", 
                    childId, sponsorEmail, e.getMessage(), e);
            throw e;
        }
    }
}
