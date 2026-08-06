package com.jjt.platform.api.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjt.platform.core.domain.entity.ApprovalCheckVerdict;
import com.jjt.platform.core.domain.entity.ApprovalEntityType;
import com.jjt.platform.core.domain.entity.ApprovalStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalCheckResultEntity;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import com.jjt.platform.infrastructure.persistence.entity.ExpenseEntity;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalCheckResultJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalRequestJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.BudgetJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ExpenseJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovalEngineService {

    private final ApprovalRequestJpaRepository requestRepo;
    private final ApprovalCheckResultJpaRepository checkRepo;
    private final ExpenseJpaRepository expenseRepo;
    private final BudgetJpaRepository budgetRepo;
    private final ObjectMapper objectMapper;

    public ApprovalEngineService(ApprovalRequestJpaRepository requestRepo,
                                 ApprovalCheckResultJpaRepository checkRepo,
                                 ExpenseJpaRepository expenseRepo,
                                 BudgetJpaRepository budgetRepo,
                                 ObjectMapper objectMapper) {
        this.requestRepo = requestRepo;
        this.checkRepo = checkRepo;
        this.expenseRepo = expenseRepo;
        this.budgetRepo = budgetRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApprovalRequestEntity createAndRunChecks(UUID orgId, ApprovalEntityType entityType,
                                                    UUID entityId, UUID requestedBy) {
        ApprovalRequestEntity request = new ApprovalRequestEntity(
                UUID.randomUUID(), orgId, entityType.name(), entityId, requestedBy);
        requestRepo.save(request);

        if (entityType == ApprovalEntityType.EXPENSE) {
            expenseRepo.findById(entityId).ifPresent(expense -> {
                runDuplicateCheck(request.getId(), expense);
                runBudgetThresholdCheck(request.getId(), expense);
                runAnomalyCheck(request.getId(), expense);
            });
        }

        ApprovalStatus recommendation = computeRecommendation(request.getId());
        request.setStatus(recommendation.name());
        return requestRepo.save(request);
    }

    @Transactional
    public ApprovalRequestEntity resolve(UUID requestId, UUID approvedBy,
                                         boolean approved, String notes) {
        ApprovalRequestEntity request = requestRepo.findById(requestId)
                .orElseThrow(() -> new DomainException("Approval request not found"));
        if (request.getStatus().equals("APPROVED") || request.getStatus().equals("REJECTED")) {
            throw new DomainException("Request already resolved");
        }
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        request.setApprovedBy(approvedBy);
        request.setReviewedAt(Instant.now());
        request.setNotes(notes);
        return requestRepo.save(request);
    }

    private void runDuplicateCheck(UUID requestId, ExpenseEntity expense) {
        if (expense.getVendorId() == null) return;
        Instant from = expense.getCreatedAt().minus(7, ChronoUnit.DAYS);
        Instant to   = expense.getCreatedAt().plus(7, ChronoUnit.DAYS);
        long count = expenseRepo.countDuplicateCandidates(
                expense.getOrgId(), expense.getVendorId(), expense.getAmount(), from, to, expense.getId());

        ApprovalCheckVerdict verdict = count > 0 ? ApprovalCheckVerdict.REVIEW : ApprovalCheckVerdict.APPROVE;
        BigDecimal confidence = count > 0 ? BigDecimal.valueOf(0.85) : BigDecimal.valueOf(1.0);
        String explanation = toJson(Map.of(
                "reason", count > 0 ? "Potential duplicate: " + count + " similar expense(s) within ±7 days" : "No duplicates found",
                "evidence", List.of("vendor_id=" + expense.getVendorId(), "amount=" + expense.getAmount()),
                "recommendedAction", count > 0 ? "Review for duplicate submission" : "Proceed"
        ));
        checkRepo.save(new ApprovalCheckResultEntity(UUID.randomUUID(), requestId,
                "DUPLICATE_HEURISTIC", verdict.name(), confidence, explanation));
    }

    private void runBudgetThresholdCheck(UUID requestId, ExpenseEntity expense) {
        if (expense.getCategoryId() == null || expense.getPeriodId() == null) {
            checkRepo.save(new ApprovalCheckResultEntity(UUID.randomUUID(), requestId,
                    "BUDGET_THRESHOLD", "APPROVE", BigDecimal.valueOf(0.6),
                    toJson(Map.of("reason", "No category/period assigned — budget check skipped",
                            "evidence", List.of(), "recommendedAction", "Proceed"))));
            return;
        }
        BigDecimal budgeted = budgetRepo.sumBudgetByCategoryAndPeriod(
                expense.getOrgId(), expense.getPeriodId(), expense.getCategoryId());
        BigDecimal spent = expenseRepo.sumApprovedByOrgCategoryPeriod(
                expense.getOrgId(), expense.getCategoryId(), expense.getPeriodId());
        BigDecimal remaining = budgeted.subtract(spent);
        boolean overBudget = expense.getAmount().compareTo(remaining) > 0;
        BigDecimal pct = budgeted.compareTo(BigDecimal.ZERO) > 0
                ? spent.add(expense.getAmount()).divide(budgeted, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        ApprovalCheckVerdict verdict = overBudget ? ApprovalCheckVerdict.REVIEW : ApprovalCheckVerdict.APPROVE;
        checkRepo.save(new ApprovalCheckResultEntity(UUID.randomUUID(), requestId,
                "BUDGET_THRESHOLD", verdict.name(),
                overBudget ? BigDecimal.valueOf(0.90) : BigDecimal.valueOf(1.0),
                toJson(Map.of("reason", overBudget
                                ? "Expense exceeds remaining category budget"
                                : "Within budget",
                        "evidence", List.of("budgeted=" + budgeted, "spent=" + spent,
                                "remaining=" + remaining, "burnPct=" + pct),
                        "recommendedAction", overBudget ? "Seek additional approval or reallocate budget" : "Proceed"))));
    }

    private void runAnomalyCheck(UUID requestId, ExpenseEntity expense) {
        // Stub: flag if amount > 10,000 GBP (threshold configurable in future)
        boolean anomalous = expense.getAmount().compareTo(BigDecimal.valueOf(10_000)) > 0;
        ApprovalCheckVerdict verdict = anomalous ? ApprovalCheckVerdict.REVIEW : ApprovalCheckVerdict.APPROVE;
        checkRepo.save(new ApprovalCheckResultEntity(UUID.randomUUID(), requestId,
                "ANOMALY_DETECTION", verdict.name(),
                anomalous ? BigDecimal.valueOf(0.75) : BigDecimal.valueOf(0.95),
                toJson(Map.of("reason", anomalous
                                ? "Expense amount exceeds high-value threshold (£10,000)"
                                : "Amount within normal range",
                        "evidence", List.of("amount=" + expense.getAmount()),
                        "recommendedAction", anomalous ? "Senior approval required" : "Proceed"))));
    }

    private ApprovalStatus computeRecommendation(UUID requestId) {
        List<ApprovalCheckResultEntity> results = checkRepo.findByRequestId(requestId);
        boolean anyReject = results.stream().anyMatch(r -> "REJECT".equals(r.getVerdict()));
        boolean anyReview = results.stream().anyMatch(r -> "REVIEW".equals(r.getVerdict()));
        if (anyReject) return ApprovalStatus.RECOMMENDED_REJECT;
        if (anyReview) return ApprovalStatus.RECOMMENDED_REVIEW;
        return ApprovalStatus.RECOMMENDED_APPROVE;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
