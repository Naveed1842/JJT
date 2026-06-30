package com.jjt.platform.api.admin.service;

import com.jjt.platform.application.usecase.AddMonthlyProgressUseCase;
import com.jjt.platform.application.usecase.CommitFutureSponsorshipUseCase;
import com.jjt.platform.application.usecase.CreateChildUseCase;
import com.jjt.platform.application.usecase.CreateSponsorUseCase;
import com.jjt.platform.application.usecase.RecordEarlySupportUseCase;
import com.jjt.platform.core.domain.entity.Child;
import com.jjt.platform.core.domain.entity.CommitmentType;
import com.jjt.platform.core.domain.entity.CoverageType;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.core.domain.entity.Sponsor;
import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.mapper.ChildMapper;
import com.jjt.platform.infrastructure.persistence.mapper.EducationSupportLedgerMapper;
import com.jjt.platform.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.jjt.platform.infrastructure.persistence.mapper.ProgressUpdateMapper;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorMapper;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorshipMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminCommandService {

    private static final Logger log = LoggerFactory.getLogger(AdminCommandService.class);

    private final CreateChildUseCase createChildUseCase = new CreateChildUseCase();
    private final RecordEarlySupportUseCase recordEarlySupportUseCase = new RecordEarlySupportUseCase();
    private final AddMonthlyProgressUseCase addMonthlyProgressUseCase = new AddMonthlyProgressUseCase();
    private final CommitFutureSponsorshipUseCase commitFutureSponsorshipUseCase = new CommitFutureSponsorshipUseCase();
    private final CreateSponsorUseCase createSponsorUseCase = new CreateSponsorUseCase();

    private final ChildJpaRepository childRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;
    private final SponsorJpaRepository sponsorRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final AdminFundService fundService;

    public AdminCommandService(ChildJpaRepository childRepo,
                               EducationSupportLedgerJpaRepository ledgerRepo,
                               LedgerEntryRepository ledgerEntryRepo,
                               ProgressUpdateRepository progressRepo,
                               SponsorJpaRepository sponsorRepo,
                               SponsorshipJpaRepository sponsorshipRepo,
                               AdminFundService fundService) {
        this.childRepo = childRepo;
        this.ledgerRepo = ledgerRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.progressRepo = progressRepo;
        this.sponsorRepo = sponsorRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.fundService = fundService;
    }

    @Transactional
    public CreateChildResult createChild(String rollNumber, String fullName, String city, String campusName, String schoolName,
                                         Money educationCost, UUID childId, UUID ledgerId) {
        CreateChildUseCase.Result result = createChildUseCase.create(
                new CreateChildUseCase.Command(childId, ledgerId, fullName, educationCost, rollNumber, city, campusName, schoolName));
        ChildEntity childEntity = ChildMapper.toEntity(result.child());
        EducationSupportLedgerEntity ledgerEntity = EducationSupportLedgerMapper.toEntity(result.ledger(), childEntity);
        childRepo.save(childEntity);
        ledgerRepo.save(ledgerEntity);
        return new CreateChildResult(result.child(), result.ledger());
    }

    @Transactional
    public LedgerEntry recordEarlySupport(UUID childId, YearMonthValue month, Money educationCost,
                                          UUID ledgerEntryId, UUID actingUserId,
                                          boolean force, String forceReason) {
        EducationSupportLedger ledger = loadLedgerDomain(childId)
                .orElseThrow(() -> new DomainException("Ledger not found for child"));

        // Balance check before saving — throws InsufficientFundsException (422) unless forced.
        fundService.findDefaultFundAccount().ifPresentOrElse(
                account -> {
                    if (!force) {
                        fundService.assertSufficientFunds(account.getId(), educationCost.getAmount());
                    } else if (forceReason != null) {
                        log.warn("Early support fund check overridden by user {} reason='{}'", actingUserId, forceReason);
                    }
                },
                () -> log.warn("No fund account found; recording early support without fund debit")
        );

        RecordEarlySupportUseCase.Command command = new RecordEarlySupportUseCase.Command(
                ledgerEntryId, childId, month, educationCost, CoverageType.EARLY_SUPPORT, actingUserId);
        EducationSupportLedger updated = recordEarlySupportUseCase.record(command, ledger);
        LedgerEntry entry = updated.getEntriesByMonth().get(month);
        EducationSupportLedgerEntity ledgerEntity = ledgerRepo.findByChild_Id(childId).orElseThrow();
        LedgerEntryEntity entryEntity = LedgerEntryMapper.toEntity(entry, ledgerEntity);
        ledgerEntryRepo.save(entryEntity);

        // Auto-debit fund after ledger entry is saved.
        fundService.findDefaultFundAccount().ifPresent(account ->
                fundService.debitForEarlySupport(
                        account.getId(),
                        entry.getId(),
                        educationCost.getAmount(),
                        educationCost.getCurrency().getCurrencyCode(),
                        actingUserId)
        );

        return entry;
    }

    /**
     * Creates a SPONSOR-coverage ledger entry when a sponsor payment is received.
     * Does not perform any fund debit (fund is credited by the payment, not debited).
     */
    @Transactional
    public LedgerEntry recordSponsorLedgerEntry(UUID childId, YearMonthValue month, Money amount,
                                                UUID ledgerEntryId, UUID actingUserId) {
        EducationSupportLedger ledger = loadLedgerDomain(childId)
                .orElseThrow(() -> new DomainException("Ledger not found for child"));
        RecordEarlySupportUseCase.Command command = new RecordEarlySupportUseCase.Command(
                ledgerEntryId, childId, month, amount, CoverageType.SPONSOR, actingUserId);
        EducationSupportLedger updated = recordEarlySupportUseCase.record(command, ledger);
        LedgerEntry entry = updated.getEntriesByMonth().get(month);
        EducationSupportLedgerEntity ledgerEntity = ledgerRepo.findByChild_Id(childId).orElseThrow();
        LedgerEntryEntity entryEntity = LedgerEntryMapper.toEntity(entry, ledgerEntity);
        ledgerEntryRepo.save(entryEntity);
        return entry;
    }

    @Transactional
    public ProgressUpdate addProgress(UUID childId, YearMonthValue month, String summary,
                                      UUID progressId, UUID actingUserId) {
        EducationSupportLedger ledger = loadLedgerDomain(childId)
                .orElseThrow(() -> new DomainException("Ledger not found for child"));
        ProgressUpdate progress = addMonthlyProgressUseCase.add(
                new AddMonthlyProgressUseCase.Command(progressId, childId, month, summary, actingUserId),
                ledger);
        ProgressUpdateEntity entity = ProgressUpdateMapper.toEntity(progress);
        progressRepo.save(entity);
        return progress;
    }

    @Transactional
    public Sponsor createSponsor(String displayName, String contactEmail, String phone, UUID sponsorId) {
        Sponsor sponsor = createSponsorUseCase.create(
                new CreateSponsorUseCase.Command(sponsorId, displayName, contactEmail, phone));
        SponsorEntity entity = SponsorMapper.toEntity(sponsor);
        sponsorRepo.save(entity);
        return sponsor;
    }

    @Transactional
    public Sponsorship commitSponsorship(UUID sponsorId, UUID childId, YearMonthValue startMonth,
                                         UUID sponsorshipId, CommitmentType commitmentType, UUID actingUserId) {
        SponsorEntity sponsor = sponsorRepo.findById(sponsorId)
                .orElseThrow(() -> new DomainException("Sponsor not found"));
        if (childRepo.findById(childId).isEmpty()) {
            throw new DomainException("Child not found");
        }
        boolean hasActive = sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE);
        Sponsorship sponsorship = commitFutureSponsorshipUseCase.commit(
                new CommitFutureSponsorshipUseCase.Command(
                        sponsorshipId, sponsorId, childId, startMonth, hasActive,
                        Instant.now(), null,
                        commitmentType != null ? commitmentType : CommitmentType.MONTHLY,
                        actingUserId
                ));
        SponsorshipEntity entity = SponsorshipMapper.toEntity(sponsorship, sponsor);
        sponsorshipRepo.save(entity);
        return sponsorship;
    }

    @Transactional(readOnly = true)
    public List<SponsorshipEntity> findEntitiesByStatus(SponsorshipStatus status) {
        return sponsorshipRepo.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<SponsorshipEntity> findSponsorshipsByChild(UUID childId) {
        return sponsorshipRepo.findByChildIdOrderByCreatedAtDesc(childId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSponsorship(UUID childId) {
        return sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE);
    }

    @Transactional
    public Sponsorship activateSponsorship(UUID sponsorshipId) {
        SponsorshipEntity entity = sponsorshipRepo.findById(sponsorshipId)
                .orElseThrow(() -> new DomainException("Sponsorship not found"));
        UUID childId = entity.getChildId();
        boolean otherActive = sponsorshipRepo.existsByChildIdAndStatus(childId, SponsorshipStatus.ACTIVE)
                && entity.getStatus() != SponsorshipStatus.ACTIVE;
        if (otherActive) {
            throw new DomainException("Another active sponsorship exists for this child");
        }
        Sponsorship updated = SponsorshipMapper.toDomain(entity).withStatus(SponsorshipStatus.ACTIVE);
        sponsorshipRepo.save(SponsorshipMapper.toEntity(updated, entity.getSponsor()));
        return updated;
    }

    @Transactional
    public Sponsorship expireSponsorship(UUID sponsorshipId) {
        SponsorshipEntity entity = sponsorshipRepo.findById(sponsorshipId)
                .orElseThrow(() -> new DomainException("Sponsorship not found"));
        Sponsorship updated = SponsorshipMapper.toDomain(entity).withStatus(SponsorshipStatus.EXPIRED);
        sponsorshipRepo.save(SponsorshipMapper.toEntity(updated, entity.getSponsor()));
        return updated;
    }

    private Optional<EducationSupportLedger> loadLedgerDomain(UUID childId) {
        return ledgerRepo.findByChild_Id(childId).map(ledgerEntity -> {
            EducationSupportLedger ledger = EducationSupportLedger.create(ledgerEntity.getId(), childId);
            List<LedgerEntryEntity> entries = ledgerEntryRepo.findByLedger_IdOrderByEntryMonth(ledgerEntity.getId());
            for (LedgerEntryEntity e : entries) {
                ledger = ledger.appendEntry(LedgerEntryMapper.toDomain(e));
            }
            return ledger;
        });
    }

    public record CreateChildResult(Child child, EducationSupportLedger ledger) {}
}
