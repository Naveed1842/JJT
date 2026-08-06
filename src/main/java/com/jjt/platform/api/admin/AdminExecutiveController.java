package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.TransparencyService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import com.jjt.platform.infrastructure.persistence.entity.TransparencySnapshotEntity;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalRequestJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance/executive")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminExecutiveController {

    private final FinancialTransactionJpaRepository2 txRepo;
    private final ApprovalRequestJpaRepository approvalRepo;
    private final TransparencyService transparencyService;

    public AdminExecutiveController(FinancialTransactionJpaRepository2 txRepo,
                                    ApprovalRequestJpaRepository approvalRepo,
                                    TransparencyService transparencyService) {
        this.txRepo = txRepo;
        this.approvalRepo = approvalRepo;
        this.transparencyService = transparencyService;
    }

    @GetMapping("/summary")
    public ExecutiveSummary summary(@AuthenticationPrincipal JwtUserDetails principal) {
        UUID orgId = principal.getOrgId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        BigDecimal mtdIncome  = txRepo.sumIncomeForPeriod(orgId, monthStart, today);
        BigDecimal mtdExpense = txRepo.sumExpenseByReportingClass(orgId, "PROGRAMME", monthStart, today)
                .add(txRepo.sumExpenseByReportingClass(orgId, "ADMIN", monthStart, today))
                .add(txRepo.sumExpenseByReportingClass(orgId, "FUNDRAISING", monthStart, today));

        List<ApprovalRequestEntity> pendingApprovals = approvalRepo.findByOrgIdAndStatusIn(orgId,
                List.of("SUBMITTED", "RECOMMENDED_APPROVE", "RECOMMENDED_REVIEW", "RECOMMENDED_REJECT"));
        BigDecimal pendingValue = BigDecimal.ZERO;

        TransparencySnapshotEntity snapshot = transparencyService.getLatestPublished(orgId).orElse(null);
        BigDecimal programmePct = snapshot != null && snapshot.getProgrammePct() != null
                ? snapshot.getProgrammePct() : BigDecimal.ZERO;

        return new ExecutiveSummary(today.toString(), mtdIncome, mtdExpense,
                pendingApprovals.size(), pendingValue, programmePct,
                snapshot != null ? snapshot.getComputedAt().toString() : null,
                List.of());
    }

    public record ExecutiveSummary(String asOf, BigDecimal mtdIncome, BigDecimal mtdExpense,
                                   int pendingApprovalCount, BigDecimal pendingApprovalValue,
                                   BigDecimal programmePct, String transparencySnapshotDate,
                                   List<AiRecommendation> aiRecommendations) {}
    public record AiRecommendation(UUID requestId, String entityType, String entityRef,
                                   String recommendation, BigDecimal confidence, String summary) {}
}
