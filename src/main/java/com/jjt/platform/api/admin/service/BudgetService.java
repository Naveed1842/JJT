package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.BudgetEntity;
import com.jjt.platform.infrastructure.persistence.repository.BudgetJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ExpenseJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialPeriodJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetJpaRepository budgetRepo;
    private final ExpenseJpaRepository expenseRepo;
    private final FinancialPeriodJpaRepository periodRepo;

    public BudgetService(BudgetJpaRepository budgetRepo, ExpenseJpaRepository expenseRepo,
                         FinancialPeriodJpaRepository periodRepo) {
        this.budgetRepo = budgetRepo;
        this.expenseRepo = expenseRepo;
        this.periodRepo = periodRepo;
    }

    @Transactional(readOnly = true)
    public List<BudgetEntity> listForPeriod(UUID orgId, UUID periodId) {
        return budgetRepo.findByOrgIdAndPeriodId(orgId, periodId);
    }

    @Transactional
    public BudgetEntity createOrUpdate(UUID orgId, UUID periodId, Integer categoryId,
                                       UUID costCentreId, UUID missionNodeId,
                                       BigDecimal amount, String notes) {
        if (!periodRepo.existsById(periodId)) throw new DomainException("Period not found");
        return budgetRepo.findByOrgIdAndPeriodIdAndCategoryId(orgId, periodId, categoryId)
                .map(existing -> {
                    existing.setAmount(amount);
                    existing.setNotes(notes);
                    return budgetRepo.save(existing);
                })
                .orElseGet(() -> budgetRepo.save(new BudgetEntity(UUID.randomUUID(), orgId,
                        periodId, categoryId, costCentreId, missionNodeId, amount, notes)));
    }

    @Transactional(readOnly = true)
    public List<BudgetVarianceRow> computeVariance(UUID orgId, UUID periodId) {
        List<BudgetEntity> budgets = budgetRepo.findByOrgIdAndPeriodId(orgId, periodId);
        return budgets.stream().map(b -> {
            BigDecimal budgeted = b.getAmount();
            BigDecimal spent = b.getCategoryId() != null
                    ? expenseRepo.sumApprovedByOrgCategoryPeriod(orgId, b.getCategoryId(), periodId)
                    : BigDecimal.ZERO;
            BigDecimal variance = budgeted.subtract(spent);
            BigDecimal pct = budgeted.compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budgeted, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            return new BudgetVarianceRow(b.getId(), b.getCategoryId(), b.getCostCentreId(),
                    budgeted, spent, variance, pct);
        }).collect(Collectors.toList());
    }

    public record BudgetVarianceRow(UUID budgetId, Integer categoryId, UUID costCentreId,
                                    BigDecimal budgeted, BigDecimal spent,
                                    BigDecimal variance, BigDecimal spendPct) {}
}
