package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.FinancialPeriodEntity;
import com.jjt.platform.infrastructure.persistence.entity.TransparencySnapshotEntity;
import com.jjt.platform.infrastructure.persistence.repository.FinancialPeriodJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.TransparencySnapshotJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransparencyService {

    private static final int MIN_TX_THRESHOLD = 10;

    private final TransparencySnapshotJpaRepository snapshotRepo;
    private final FinancialPeriodJpaRepository periodRepo;
    private final FinancialTransactionJpaRepository2 txRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;

    public TransparencyService(TransparencySnapshotJpaRepository snapshotRepo,
                               FinancialPeriodJpaRepository periodRepo,
                               FinancialTransactionJpaRepository2 txRepo,
                               SponsorshipJpaRepository sponsorshipRepo) {
        this.snapshotRepo = snapshotRepo;
        this.periodRepo = periodRepo;
        this.txRepo = txRepo;
        this.sponsorshipRepo = sponsorshipRepo;
    }

    /** Nightly snapshot job — runs at 02:00 for all orgs with at least one OPEN period. */
    @Scheduled(cron = "0 0 2 * * *")
    public void nightlySnapshot() {
        // Find all orgs that have open periods and generate snapshots
        List<FinancialPeriodEntity> openPeriods = periodRepo.findAll().stream()
                .filter(p -> "OPEN".equals(p.getStatus()))
                .toList();
        for (FinancialPeriodEntity period : openPeriods) {
            try {
                computeAndSave(period.getOrgId(), period.getId(), false);
            } catch (Exception ignored) {
                // Never let one org failure block others
            }
        }
    }

    /** Called on period close — generates and immediately publishes a snapshot. */
    @Transactional
    public void onPeriodClose(UUID orgId, UUID periodId) {
        computeAndSave(orgId, periodId, true);
    }

    @Transactional
    public void computeAndSave(UUID orgId, UUID periodId, boolean publish) {
        FinancialPeriodEntity period = periodRepo.findById(periodId).orElse(null);
        if (period == null) return;

        BigDecimal totalIncome   = txRepo.sumIncomeForPeriod(orgId, period.getStartDate(), period.getEndDate());
        BigDecimal programmeCost = txRepo.sumExpenseByReportingClass(orgId, "PROGRAMME", period.getStartDate(), period.getEndDate());
        BigDecimal adminCost     = txRepo.sumExpenseByReportingClass(orgId, "ADMIN",     period.getStartDate(), period.getEndDate());
        BigDecimal fundraisCost  = txRepo.sumExpenseByReportingClass(orgId, "FUNDRAISING", period.getStartDate(), period.getEndDate());
        BigDecimal totalExpense  = programmeCost.add(adminCost).add(fundraisCost);

        // Minimum data guard
        boolean hasEnoughData = totalExpense.compareTo(BigDecimal.ZERO) > 0
                && totalIncome.compareTo(BigDecimal.valueOf(MIN_TX_THRESHOLD)) >= 0;

        int beneficiaryCount = sponsorshipRepo.findByStatus(SponsorshipStatus.ACTIVE).size();

        TransparencySnapshotEntity snapshot = snapshotRepo.findByOrgIdAndPeriodId(orgId, periodId)
                .orElseGet(() -> new TransparencySnapshotEntity(UUID.randomUUID(), orgId, periodId));

        snapshot.setTotalIncome(totalIncome);
        snapshot.setTotalExpense(totalExpense);
        snapshot.setBeneficiaryCount(beneficiaryCount);

        if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            snapshot.setProgrammePct(pct(programmeCost, totalExpense));
            snapshot.setAdminPct(pct(adminCost, totalExpense));
            snapshot.setFundraisingPct(pct(fundraisCost, totalExpense));
            if (beneficiaryCount > 0) {
                snapshot.setCostPerBeneficiary(totalExpense.divide(
                        BigDecimal.valueOf(beneficiaryCount), 2, RoundingMode.HALF_UP));
            }
        }
        snapshot.setPublished(publish && hasEnoughData);
        snapshotRepo.save(snapshot);
    }

    @Transactional(readOnly = true)
    public Optional<TransparencySnapshotEntity> getLatestPublished(UUID orgId) {
        return snapshotRepo.findFirstByOrgIdAndPublishedTrueOrderByComputedAtDesc(orgId);
    }

    @Transactional(readOnly = true)
    public List<TransparencySnapshotEntity> getHistory(UUID orgId) {
        return snapshotRepo.findByOrgIdAndPublishedTrueOrderByComputedAtDesc(orgId);
    }

    private BigDecimal pct(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return part.multiply(BigDecimal.valueOf(100))
                   .divide(total, 2, RoundingMode.HALF_UP);
    }
}
