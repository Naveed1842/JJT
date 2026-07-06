package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.Donation;
import com.jjt.platform.core.domain.entity.DonationStatus;
import com.jjt.platform.core.domain.entity.DonationType;
import com.jjt.platform.core.domain.entity.Donor;
import com.jjt.platform.core.domain.entity.RecurringDonationSchedule;
import com.jjt.platform.core.domain.entity.RecurringDonationStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.infrastructure.persistence.entity.DonationEntity;
import com.jjt.platform.infrastructure.persistence.entity.DonationReceiptSequenceEntity;
import com.jjt.platform.infrastructure.persistence.entity.DonorEntity;
import com.jjt.platform.infrastructure.persistence.entity.OrganisationEntity;
import com.jjt.platform.infrastructure.persistence.entity.RecurringDonationScheduleEntity;
import com.jjt.platform.infrastructure.persistence.mapper.DonationMapper;
import com.jjt.platform.infrastructure.persistence.mapper.DonorMapper;
import com.jjt.platform.infrastructure.persistence.mapper.RecurringDonationScheduleMapper;
import com.jjt.platform.infrastructure.persistence.repository.DonationJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.DonationReceiptSequenceJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.DonorJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FundAccountJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.OrganisationJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.RecurringDonationScheduleJpaRepository;
import com.jjt.platform.infrastructure.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminDonationService {

    private final DonorJpaRepository donorRepo;
    private final DonationJpaRepository donationRepo;
    private final DonationReceiptSequenceJpaRepository receiptSequenceRepo;
    private final RecurringDonationScheduleJpaRepository recurringRepo;
    private final FundAccountJpaRepository fundAccountRepo;
    private final OrganisationJpaRepository orgRepo;
    private final AdminFundService fundService;
    private final NotificationService notificationService;

    public AdminDonationService(DonorJpaRepository donorRepo,
                                DonationJpaRepository donationRepo,
                                DonationReceiptSequenceJpaRepository receiptSequenceRepo,
                                RecurringDonationScheduleJpaRepository recurringRepo,
                                FundAccountJpaRepository fundAccountRepo,
                                OrganisationJpaRepository orgRepo,
                                AdminFundService fundService,
                                NotificationService notificationService) {
        this.donorRepo = donorRepo;
        this.donationRepo = donationRepo;
        this.receiptSequenceRepo = receiptSequenceRepo;
        this.recurringRepo = recurringRepo;
        this.fundAccountRepo = fundAccountRepo;
        this.orgRepo = orgRepo;
        this.fundService = fundService;
        this.notificationService = notificationService;
    }

    // ── Donor management ────────────────────────────────────────────────────

    @Transactional
    public Donor createDonor(String displayName, String email, String phone,
                             com.jjt.platform.core.domain.entity.DonorType donorType,
                             String notes, UUID orgId, UUID createdBy) {
        Donor donor = Donor.createNew(orgId, displayName, email, phone, donorType, notes, createdBy);
        donorRepo.save(DonorMapper.toEntity(donor));
        return donor;
    }

    @Transactional(readOnly = true)
    public List<Donor> listDonors(UUID orgId) {
        return donorRepo.findByOrganisationIdOrderByDisplayNameAsc(orgId)
                .stream()
                .map(DonorMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Donor getDonor(UUID donorId, UUID orgId) {
        DonorEntity entity = donorRepo.findById(donorId)
                .orElseThrow(() -> new DomainException("Donor not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Donor not found");
        }
        return DonorMapper.toDomain(entity);
    }

    // ── Donation recording ──────────────────────────────────────────────────

    /**
     * Records a donation, credits the appropriate fund account, and generates a receipt number.
     * IN_KIND donations do not credit a fund account.
     */
    @Transactional
    public Donation recordDonation(UUID donorId, DonationType donationType,
                                   BigDecimal amount, String currency,
                                   LocalDate donationDate, String notes,
                                   UUID fundAccountId, UUID orgId, UUID createdBy) {
        // Validate donor belongs to this org
        if (donorId != null) {
            donorRepo.findById(donorId)
                    .filter(d -> d.getOrganisationId().equals(orgId))
                    .orElseThrow(() -> new DomainException("Donor not found in this organisation"));
        }

        // Resolve fund account (explicit or default); not required for IN_KIND
        UUID resolvedFundAccountId = null;
        UUID fundTransactionId = null;
        if (donationType != DonationType.IN_KIND) {
            resolvedFundAccountId = resolveFundAccount(fundAccountId, orgId);
            if (!fundAccountRepo.existsById(resolvedFundAccountId)) {
                throw new DomainException("Fund account not found");
            }
            String description = String.format("%s donation of %s %s", donationType, amount, currency);
            var tx = fundService.credit(resolvedFundAccountId, amount, currency, description, null, createdBy);
            fundTransactionId = tx.getId();
        }

        OrganisationEntity org = orgRepo.findById(orgId)
                .orElseThrow(() -> new DomainException("Organisation not found"));
        String receiptNumber = generateReceiptNumber(orgId, org.getSlug(), donationDate.getYear());

        Donation donation = Donation.createNew(
                orgId, donorId, donationType,
                Money.of(amount, java.util.Currency.getInstance(currency)),
                donationDate, receiptNumber, notes,
                resolvedFundAccountId, fundTransactionId, createdBy);

        donationRepo.save(DonationMapper.toEntity(donation));

        // Send receipt email to donor if they have a contact email
        if (donorId != null) {
            DonorEntity donor = donorRepo.findById(donorId).orElse(null);
            if (donor != null && donor.getEmail() != null) {
                notificationService.sendDonationReceipt(
                        orgId, donor.getEmail(), donor.getDisplayName(),
                        donor.getDisplayName(), receiptNumber,
                        amount.toPlainString(), currency,
                        donationType.name(), donationDate.toString(),
                        org.getName(), donation.getId());
            }
        }

        return donation;
    }

    @Transactional(readOnly = true)
    public Donation getDonation(UUID donationId, UUID orgId) {
        DonationEntity entity = donationRepo.findById(donationId)
                .orElseThrow(() -> new DomainException("Donation not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Donation not found");
        }
        return DonationMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<Donation> listDonations(UUID orgId, Pageable pageable) {
        return donationRepo.findByOrganisationId(orgId, pageable)
                .map(DonationMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Page<Donation> listDonationsByType(UUID orgId, DonationType type, Pageable pageable) {
        return donationRepo.findByOrganisationIdAndDonationType(orgId, type, pageable)
                .map(DonationMapper::toDomain);
    }

    /**
     * Zakat headline figures. Zakat is tracked independently (a public promise on
     * the Trust page): totals cover RECEIPTED donations only; pending counts
     * EXPECTED ones awaiting confirmation.
     */
    @Transactional(readOnly = true)
    public ZakatStats getZakatStats(UUID orgId) {
        LocalDate now = LocalDate.now();
        String currency = orgRepo.findById(orgId)
                .map(OrganisationEntity::getBaseCurrency).orElse("PKR");
        return new ZakatStats(
                donationRepo.sumReceiptedByType(orgId, DonationType.ZAKAT),
                donationRepo.sumReceiptedByTypeSince(orgId, DonationType.ZAKAT, now.withDayOfYear(1)),
                donationRepo.sumReceiptedByTypeSince(orgId, DonationType.ZAKAT, now.withDayOfMonth(1)),
                donationRepo.countByOrganisationIdAndDonationTypeAndStatus(orgId, DonationType.ZAKAT, DonationStatus.RECEIPTED),
                donationRepo.countByOrganisationIdAndDonationTypeAndStatus(orgId, DonationType.ZAKAT, DonationStatus.EXPECTED),
                currency
        );
    }

    public record ZakatStats(
            BigDecimal totalReceived,
            BigDecimal receivedThisYear,
            BigDecimal receivedThisMonth,
            long receiptedCount,
            long pendingCount,
            String currency
    ) {}

    /**
     * Confirms receipt of an EXPECTED donation (from a recurring schedule),
     * credits the fund, and generates a receipt number.
     */
    @Transactional
    public Donation receiveExpectedDonation(UUID donationId, BigDecimal actualAmount, UUID orgId, UUID updatedBy) {
        DonationEntity entity = donationRepo.findById(donationId)
                .orElseThrow(() -> new DomainException("Donation not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Donation not found");
        }
        if (entity.getStatus() != DonationStatus.EXPECTED) {
            throw new DomainException("Donation is not in EXPECTED status");
        }

        BigDecimal amountToCredit = actualAmount != null ? actualAmount : entity.getAmount();
        UUID resolvedFundAccountId = entity.getFundAccountId();
        if (resolvedFundAccountId == null) {
            resolvedFundAccountId = resolveFundAccount(null, orgId);
        }
        String currency = entity.getCurrency();
        String description = String.format("%s recurring donation of %s %s", entity.getDonationType(), amountToCredit, currency);
        var tx = fundService.credit(resolvedFundAccountId, amountToCredit, currency, description, null, updatedBy);

        OrganisationEntity org = orgRepo.findById(orgId)
                .orElseThrow(() -> new DomainException("Organisation not found"));
        String receiptNumber = generateReceiptNumber(orgId, org.getSlug(), LocalDate.now().getYear());

        entity.setStatus(DonationStatus.RECEIPTED);
        entity.setReceiptNumber(receiptNumber);
        entity.setFundTransactionId(tx.getId());
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(java.time.Instant.now());
        donationRepo.save(entity);

        return DonationMapper.toDomain(entity);
    }

    /**
     * Reverses a donation. For RECEIPTED donations that credited a fund, a compensating
     * DEBIT (reason DONATION_REVERSAL) is appended in the same transaction so the derived
     * fund balance self-corrects — the original CREDIT row is never modified.
     *
     * EXPECTED donations (never credited) and IN_KIND donations (no fund involvement)
     * are status-only reversals: there is nothing financial to undo.
     */
    @Transactional
    public Donation reverseDonation(UUID donationId, UUID orgId, UUID updatedBy) {
        DonationEntity entity = donationRepo.findById(donationId)
                .orElseThrow(() -> new DomainException("Donation not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Donation not found");
        }
        if (entity.getStatus() == DonationStatus.REVERSED) {
            throw new DomainException("Donation is already reversed");
        }

        boolean fundWasCredited = entity.getStatus() == DonationStatus.RECEIPTED
                && entity.getFundTransactionId() != null
                && entity.getFundAccountId() != null;

        Donation reversed = DonationMapper.toDomain(entity).reverse(updatedBy);
        donationRepo.save(DonationMapper.toEntity(reversed));

        if (fundWasCredited) {
            fundService.debitForDonationReversal(
                    entity.getFundAccountId(),
                    donationId,
                    entity.getReceiptNumber(),
                    entity.getAmount(),
                    entity.getCurrency(),
                    updatedBy);
        }

        return reversed;
    }

    // ── Receipt data ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ReceiptData getReceiptData(UUID donationId, UUID orgId) {
        DonationEntity donation = donationRepo.findById(donationId)
                .orElseThrow(() -> new DomainException("Donation not found"));
        if (!donation.getOrganisationId().equals(orgId)) {
            throw new DomainException("Donation not found");
        }
        if (donation.getStatus() == DonationStatus.REVERSED) {
            throw new DomainException("This donation has been reversed — its receipt is void");
        }
        if (donation.getReceiptNumber() == null) {
            throw new DomainException("No receipt available for this donation — it may still be EXPECTED");
        }

        String donorName = "Anonymous";
        String donorEmail = null;
        String donorPhone = null;
        if (donation.getDonorId() != null) {
            DonorEntity donor = donorRepo.findById(donation.getDonorId()).orElse(null);
            if (donor != null) {
                donorName = donor.getDisplayName();
                donorEmail = donor.getEmail();
                donorPhone = donor.getPhone();
            }
        }

        OrganisationEntity org = orgRepo.findById(orgId)
                .orElseThrow(() -> new DomainException("Organisation not found"));

        return new ReceiptData(
                donation.getReceiptNumber(),
                donorName,
                donorEmail,
                donorPhone,
                donation.getAmount(),
                donation.getCurrency(),
                donation.getDonationDate(),
                donation.getDonationType().name(),
                org.getName(),
                LocalDate.now()
        );
    }

    public record ReceiptData(String receiptNumber, String donorName, String donorEmail,
                              String donorPhone, BigDecimal amount, String currency,
                              LocalDate donationDate, String donationType,
                              String organisationName, LocalDate issuedDate) {}

    // ── Recurring schedules ─────────────────────────────────────────────────

    @Transactional
    public RecurringDonationSchedule createRecurringSchedule(
            UUID donorId, DonationType donationType,
            BigDecimal amount, String currency,
            com.jjt.platform.core.domain.entity.DonationFrequency frequency,
            LocalDate startDate, LocalDate endDate,
            UUID fundAccountId, String notes,
            UUID orgId, UUID createdBy) {

        donorRepo.findById(donorId)
                .filter(d -> d.getOrganisationId().equals(orgId))
                .orElseThrow(() -> new DomainException("Donor not found in this organisation"));

        UUID resolvedFundAccountId = fundAccountId != null
                ? fundAccountId
                : resolveFundAccount(null, orgId);

        RecurringDonationSchedule schedule = RecurringDonationSchedule.createNew(
                orgId, donorId, donationType,
                Money.of(amount, java.util.Currency.getInstance(currency)),
                frequency, startDate, endDate, resolvedFundAccountId, notes, createdBy);

        recurringRepo.save(RecurringDonationScheduleMapper.toEntity(schedule));
        return schedule;
    }

    @Transactional(readOnly = true)
    public List<RecurringDonationSchedule> listRecurringSchedules(UUID orgId) {
        return recurringRepo.findByOrganisationId(orgId)
                .stream()
                .map(RecurringDonationScheduleMapper::toDomain)
                .toList();
    }

    /**
     * Generates EXPECTED donation records for all ACTIVE recurring schedules whose
     * {@code next_due_date} is on or before today. Advances {@code next_due_date} by frequency.
     * Idempotent per schedule per due date — skips if an EXPECTED record already exists.
     * Called by the scheduler daily and can also be triggered manually.
     */
    @Transactional
    public int generateExpectedFromRecurring(UUID orgId) {
        List<RecurringDonationScheduleEntity> due;
        if (orgId != null) {
            due = recurringRepo.findByOrganisationIdAndStatus(orgId, RecurringDonationStatus.ACTIVE)
                    .stream()
                    .filter(s -> !s.getNextDueDate().isAfter(LocalDate.now()))
                    .toList();
        } else {
            due = recurringRepo.findByStatusAndNextDueDateLessThanEqual(
                    RecurringDonationStatus.ACTIVE, LocalDate.now());
        }

        int created = 0;
        for (RecurringDonationScheduleEntity scheduleEntity : due) {
            // Skip if already generated for this schedule at this due date
            boolean alreadyExists = !donationRepo.findByRecurringScheduleIdAndStatus(
                    scheduleEntity.getId(), DonationStatus.EXPECTED).isEmpty();
            if (alreadyExists) {
                continue;
            }

            Donation expected = Donation.createExpected(
                    scheduleEntity.getOrganisationId(),
                    scheduleEntity.getDonorId(),
                    scheduleEntity.getDonationType(),
                    Money.of(scheduleEntity.getAmount(), java.util.Currency.getInstance(scheduleEntity.getCurrency())),
                    scheduleEntity.getNextDueDate(),
                    scheduleEntity.getNotes(),
                    scheduleEntity.getFundAccountId(),
                    scheduleEntity.getId(),
                    scheduleEntity.getCreatedBy()
            );
            donationRepo.save(DonationMapper.toEntity(expected));

            LocalDate next = advanceDueDate(scheduleEntity.getNextDueDate(),
                    scheduleEntity.getFrequency());
            if (scheduleEntity.getEndDate() != null && next.isAfter(scheduleEntity.getEndDate())) {
                scheduleEntity.setStatus(RecurringDonationStatus.COMPLETED);
            } else {
                scheduleEntity.setNextDueDate(next);
            }
            recurringRepo.save(scheduleEntity);
            created++;
        }
        return created;
    }

    @Transactional
    public RecurringDonationSchedule updateRecurringStatus(UUID scheduleId,
                                                           RecurringDonationStatus newStatus,
                                                           UUID orgId) {
        RecurringDonationScheduleEntity entity = recurringRepo.findById(scheduleId)
                .orElseThrow(() -> new DomainException("Recurring schedule not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Recurring schedule not found");
        }
        entity.setStatus(newStatus);
        recurringRepo.save(entity);
        return RecurringDonationScheduleMapper.toDomain(entity);
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private UUID resolveFundAccount(UUID requested, UUID orgId) {
        if (requested != null) {
            return requested;
        }
        return fundAccountRepo.findAll().stream()
                .filter(a -> a.getOrganisationId() != null && a.getOrganisationId().equals(orgId))
                .findFirst()
                .or(() -> fundAccountRepo.findAll().stream().findFirst())
                .map(a -> a.getId())
                .orElseThrow(() -> new DomainException("No fund account found for this organisation"));
    }

    /**
     * Generates the next sequential receipt number for the org in a given year.
     * Uses a pessimistic lock on the sequence row to prevent duplicates under concurrent load.
     * Format: {ORG_SLUG_UPPER}-{YEAR}-{SEQUENCE:04d}  e.g. JJT-2026-0001
     */
    private String generateReceiptNumber(UUID orgId, String orgSlug, int year) {
        DonationReceiptSequenceEntity.SequenceId seqId =
                new DonationReceiptSequenceEntity.SequenceId(orgId, year);
        DonationReceiptSequenceEntity seq = receiptSequenceRepo.findForUpdate(orgId, year)
                .orElseGet(() -> receiptSequenceRepo.save(new DonationReceiptSequenceEntity(seqId, 0)));
        seq.setLastSequence(seq.getLastSequence() + 1);
        receiptSequenceRepo.save(seq);
        return String.format("%s-%d-%04d", orgSlug.toUpperCase(), year, seq.getLastSequence());
    }

    private LocalDate advanceDueDate(LocalDate current,
                                     com.jjt.platform.core.domain.entity.DonationFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> current.plusMonths(1);
            case QUARTERLY -> current.plusMonths(3);
            case ANNUAL -> current.plusYears(1);
        };
    }
}
