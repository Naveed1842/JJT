package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.SponsorPayment;
import com.jjt.platform.core.domain.entity.SponsorPaymentStatus;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorPaymentEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorPaymentMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorPaymentJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminPaymentService {

    private static final Logger log = LoggerFactory.getLogger(AdminPaymentService.class);

    private final SponsorPaymentJpaRepository paymentRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final ChildJpaRepository childRepo;
    private final AdminCommandService commandService;
    private final AdminFundService fundService;

    public AdminPaymentService(SponsorPaymentJpaRepository paymentRepo,
                               SponsorshipJpaRepository sponsorshipRepo,
                               ChildJpaRepository childRepo,
                               AdminCommandService commandService,
                               AdminFundService fundService) {
        this.paymentRepo = paymentRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.childRepo = childRepo;
        this.commandService = commandService;
        this.fundService = fundService;
    }

    /**
     * Generates EXPECTED payment records for all ACTIVE sponsorships for the given month.
     * Idempotent — skips sponsorships that already have a payment record for that month.
     */
    @Transactional
    public int generateExpectedPayments(YearMonth forMonth) {
        String monthStr = forMonth.toString();
        List<SponsorshipEntity> active = sponsorshipRepo.findByStatus(SponsorshipStatus.ACTIVE);
        int created = 0;
        for (SponsorshipEntity sponsorship : active) {
            if (paymentRepo.existsBySponsorshipIdAndPaymentMonth(sponsorship.getId(), monthStr)) {
                continue;
            }
            ChildEntity child = childRepo.findById(sponsorship.getChildId())
                    .orElse(null);
            if (child == null) {
                log.warn("Skipping payment generation for sponsorship {} — child {} not found",
                        sponsorship.getId(), sponsorship.getChildId());
                continue;
            }
            Money expectedAmount = Money.of(child.getEducationAmount(),
                    Currency.getInstance(child.getEducationCurrency()));
            SponsorPayment payment = SponsorPayment.createExpected(
                    UUID.randomUUID(),
                    sponsorship.getId(),
                    sponsorship.getSponsor().getId(),
                    child.getId(),
                    YearMonthValue.of(forMonth),
                    expectedAmount,
                    null,
                    Instant.now()
            );
            paymentRepo.save(SponsorPaymentMapper.toEntity(payment));
            created++;
        }
        log.info("Generated {} EXPECTED payments for {}", created, monthStr);
        return created;
    }

    /**
     * Marks all EXPECTED payments in months before the given cutoff month as OVERDUE.
     * Used by the daily scheduler: cutoff = current YYYY-MM when today > payment due day.
     */
    @Transactional
    public int markOverduePayments(String cutoffMonth) {
        List<SponsorPaymentEntity> overdue = paymentRepo.findExpectedBefore(cutoffMonth);
        Instant now = Instant.now();
        for (SponsorPaymentEntity entity : overdue) {
            SponsorPayment updated = SponsorPaymentMapper.toDomain(entity)
                    .markOverdue(null, now);
            paymentRepo.save(SponsorPaymentMapper.toEntity(updated));
        }
        if (!overdue.isEmpty()) {
            log.info("Marked {} payments as OVERDUE (cutoff={})", overdue.size(), cutoffMonth);
        }
        return overdue.size();
    }

    /**
     * Records a sponsor payment received. Atomically:
     * 1. Creates a LedgerEntry(SPONSOR) for the child's month
     * 2. Credits the fund
     * 3. Marks the SponsorPayment as RECEIVED or PARTIAL
     */
    @Transactional
    public SponsorPayment recordPaymentReceived(UUID paymentId, BigDecimal receivedAmount,
                                                String currency, String bankReference,
                                                LocalDate receivedDate, UUID actingUserId) {
        SponsorPaymentEntity entity = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new DomainException("Sponsor payment not found"));

        if (entity.getStatus() == SponsorPaymentStatus.RECEIVED) {
            throw new DomainException("Payment already marked as received");
        }
        if (entity.getStatus() == SponsorPaymentStatus.WAIVED) {
            throw new DomainException("Payment has been waived and cannot be marked received");
        }

        SponsorshipEntity sponsorship = sponsorshipRepo.findById(entity.getSponsorshipId())
                .orElseThrow(() -> new DomainException("Sponsorship not found"));

        YearMonthValue paymentMonth = YearMonthValue.of(YearMonth.parse(entity.getPaymentMonth()));
        Money amount = Money.of(
                new BigDecimal(entity.getExpectedAmount().toString()),
                Currency.getInstance(entity.getExpectedCurrency()));

        // Create SPONSOR ledger entry for the child
        var ledgerEntry = commandService.recordSponsorLedgerEntry(
                entity.getChildId(), paymentMonth, amount, UUID.randomUUID(), actingUserId);

        // Credit the fund
        var fundTxn = fundService.findDefaultFundAccount()
                .map(account -> fundService.credit(
                        account.getId(),
                        receivedAmount,
                        currency,
                        "Payment received for sponsorship " + sponsorship.getId(),
                        bankReference,
                        actingUserId))
                .orElse(null);

        Money received = Money.of(receivedAmount, Currency.getInstance(currency));
        SponsorPayment updated = SponsorPaymentMapper.toDomain(entity)
                .markReceived(received, bankReference, receivedDate,
                        fundTxn != null ? fundTxn.getId() : null,
                        ledgerEntry.getId(),
                        actingUserId, Instant.now());

        paymentRepo.save(SponsorPaymentMapper.toEntity(updated));
        return updated;
    }

    /**
     * Waives a payment. Creates an EARLY_SUPPORT ledger entry (funded from general fund)
     * so the child is still covered for the month, then marks the payment as WAIVED.
     */
    @Transactional
    public SponsorPayment waivePayment(UUID paymentId, String reason, UUID actingUserId) {
        SponsorPaymentEntity entity = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new DomainException("Sponsor payment not found"));

        if (entity.getStatus() == SponsorPaymentStatus.WAIVED) {
            throw new DomainException("Payment is already waived");
        }
        if (entity.getStatus() == SponsorPaymentStatus.RECEIVED) {
            throw new DomainException("Payment already received; cannot waive");
        }

        YearMonthValue paymentMonth = YearMonthValue.of(YearMonth.parse(entity.getPaymentMonth()));
        Money amount = Money.of(entity.getExpectedAmount(),
                Currency.getInstance(entity.getExpectedCurrency()));

        // Cover the child via early support (auto-debits fund; force=true since this is intentional)
        var ledgerEntry = commandService.recordEarlySupport(
                entity.getChildId(), paymentMonth, amount, UUID.randomUUID(), actingUserId,
                true, "Payment waived: " + reason);

        SponsorPayment updated = SponsorPaymentMapper.toDomain(entity)
                .waive(reason, ledgerEntry.getId(), actingUserId, Instant.now());

        paymentRepo.save(SponsorPaymentMapper.toEntity(updated));
        return updated;
    }

    @Transactional(readOnly = true)
    public SponsorPayment getPayment(UUID paymentId) {
        return SponsorPaymentMapper.toDomain(
                paymentRepo.findById(paymentId)
                        .orElseThrow(() -> new DomainException("Sponsor payment not found")));
    }

    @Transactional(readOnly = true)
    public List<SponsorPayment> getPaymentsBySponsorship(UUID sponsorshipId) {
        return paymentRepo.findBySponsorshipIdOrderByPaymentMonthDesc(sponsorshipId)
                .stream().map(SponsorPaymentMapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public MonthlyReconciliation getMonthlyReconciliation(int year, int month) {
        String monthStr = String.format("%d-%02d", year, month);
        List<SponsorPaymentEntity> payments = paymentRepo.findByPaymentMonth(monthStr);

        // Batch-load children and sponsorships to avoid N+1
        List<UUID> childIds = payments.stream().map(SponsorPaymentEntity::getChildId).distinct().toList();
        List<UUID> sponsorshipIds = payments.stream().map(SponsorPaymentEntity::getSponsorshipId).distinct().toList();

        Map<UUID, ChildEntity> childMap = childRepo.findAllById(childIds)
                .stream().collect(Collectors.toMap(ChildEntity::getId, c -> c));
        Map<UUID, SponsorshipEntity> sponsorshipMap = sponsorshipRepo.findAllById(sponsorshipIds)
                .stream().collect(Collectors.toMap(SponsorshipEntity::getId, s -> s));

        List<MonthlyReconciliation.AtRiskSponsorship> atRisk = new ArrayList<>();
        int expected = 0, received = 0, partial = 0, overdue = 0, waived = 0;

        for (SponsorPaymentEntity e : payments) {
            switch (e.getStatus()) {
                case EXPECTED -> expected++;
                case RECEIVED -> received++;
                case PARTIAL  -> partial++;
                case OVERDUE  -> overdue++;
                case WAIVED   -> waived++;
                default -> {}
            }
            if (e.getStatus() == SponsorPaymentStatus.OVERDUE) {
                int consecutive = countConsecutiveOverdue(e.getSponsorshipId(), monthStr);
                if (consecutive >= 2) {
                    ChildEntity child = childMap.get(e.getChildId());
                    SponsorshipEntity sp = sponsorshipMap.get(e.getSponsorshipId());
                    String sponsorName = sp != null ? sp.getSponsor().getDisplayName() : "Unknown";
                    String childName = child != null ? child.getFullName() : "Unknown";
                    atRisk.add(new MonthlyReconciliation.AtRiskSponsorship(
                            e.getSponsorshipId(), e.getSponsorId(), sponsorName,
                            e.getChildId(), childName,
                            consecutive, consecutive >= 3));
                }
            }
        }

        List<SponsorPayment> domainPayments = payments.stream()
                .map(e -> enrichToDomain(e, childMap, sponsorshipMap))
                .toList();

        return new MonthlyReconciliation(year, String.format("%02d", month),
                new MonthlyReconciliation.Summary(expected, received, partial, overdue, waived, payments.size()),
                atRisk, domainPayments);
    }

    private int countConsecutiveOverdue(UUID sponsorshipId, String fromMonth) {
        List<SponsorPaymentEntity> history = paymentRepo
                .findBySponsorshipIdAndStatus(sponsorshipId, SponsorPaymentStatus.OVERDUE);
        // Sort descending by month and count consecutive months ending at/before fromMonth
        List<String> overdueMonths = history.stream()
                .map(SponsorPaymentEntity::getPaymentMonth)
                .filter(m -> m.compareTo(fromMonth) <= 0)
                .sorted((a, b) -> b.compareTo(a))
                .toList();
        int count = 0;
        YearMonth cursor = YearMonth.parse(fromMonth);
        for (String m : overdueMonths) {
            if (YearMonth.parse(m).equals(cursor)) {
                count++;
                cursor = cursor.minusMonths(1);
            } else {
                break;
            }
        }
        return count;
    }

    private SponsorPayment enrichToDomain(SponsorPaymentEntity e,
                                          Map<UUID, ChildEntity> childMap,
                                          Map<UUID, SponsorshipEntity> sponsorshipMap) {
        return SponsorPaymentMapper.toDomain(e);
    }

    public record MonthlyReconciliation(
            int year,
            String month,
            Summary summary,
            List<AtRiskSponsorship> atRisk,
            List<SponsorPayment> payments
    ) {
        public record Summary(int expected, int received, int partial, int overdue, int waived, int total) {}
        public record AtRiskSponsorship(
                UUID sponsorshipId, UUID sponsorId, String sponsorName,
                UUID childId, String childName,
                int consecutiveOverdueMonths, boolean requiresEscalation) {}
    }
}
