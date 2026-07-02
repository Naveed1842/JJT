package com.jjt.platform.api.admin;

import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FundTransactionJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminReportController {

    private final FundTransactionJpaRepository fundTransactionRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final ChildJpaRepository childRepo;

    public AdminReportController(FundTransactionJpaRepository fundTransactionRepo,
                                 SponsorshipJpaRepository sponsorshipRepo,
                                 ChildJpaRepository childRepo) {
        this.fundTransactionRepo = fundTransactionRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.childRepo = childRepo;
    }

    @GetMapping("/cash-flow")
    @Transactional(readOnly = true)
    public ResponseEntity<CashFlowReport> getCashFlow(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal JwtUserDetails principal) {
        if (months < 1) months = 1;
        if (months > 12) months = 12;

        UUID orgId = principal.getOrgId();
        YearMonth current = YearMonth.now();
        List<MonthlyBalance> monthlyBalances = new ArrayList<>();

        BigDecimal totalCredits3Month = BigDecimal.ZERO;
        BigDecimal totalDebits3Month = BigDecimal.ZERO;

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String ymStr = ym.toString(); // YYYY-MM

            Instant monthStart = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            BigDecimal openingBalance = fundTransactionRepo.computeOrgBalanceBefore(orgId, monthStart);
            if (openingBalance == null) openingBalance = BigDecimal.ZERO;

            BigDecimal credits = fundTransactionRepo.sumByOrgAndMonthAndType(orgId, ymStr, "CREDIT");
            BigDecimal debits = fundTransactionRepo.sumByOrgAndMonthAndType(orgId, ymStr, "DEBIT");
            if (credits == null) credits = BigDecimal.ZERO;
            if (debits == null) debits = BigDecimal.ZERO;

            BigDecimal closingBalance = openingBalance.add(credits).subtract(debits);

            monthlyBalances.add(new MonthlyBalance(ymStr, openingBalance, credits, debits, closingBalance, false));

            if (i < 3) {
                totalCredits3Month = totalCredits3Month.add(credits);
                totalDebits3Month = totalDebits3Month.add(debits);
            }
        }

        // Current balance = closing balance of most recent month
        BigDecimal currentBalance = monthlyBalances.isEmpty()
                ? BigDecimal.ZERO
                : monthlyBalances.get(monthlyBalances.size() - 1).closingBalance();

        return ResponseEntity.ok(new CashFlowReport(
                monthlyBalances, currentBalance, totalCredits3Month, totalDebits3Month));
    }

    @GetMapping("/portfolio")
    @Transactional(readOnly = true)
    public ResponseEntity<PortfolioReport> getPortfolio(
            @AuthenticationPrincipal JwtUserDetails principal) {
        UUID orgId = principal.getOrgId();
        List<SponsorshipEntity> all = sponsorshipRepo.findByOrganisationId(orgId);

        int activeCount = 0, pendingCount = 0, expiredCount = 0;
        BigDecimal totalMonthlyValue = BigDecimal.ZERO;
        Map<String, Integer> commitmentBreakdown = new LinkedHashMap<>();

        for (SponsorshipEntity s : all) {
            switch (s.getStatus()) {
                case ACTIVE -> activeCount++;
                case PENDING -> pendingCount++;
                case EXPIRED -> expiredCount++;
                default -> {}
            }
            if (s.getStatus() == SponsorshipStatus.ACTIVE) {
                ChildEntity child = childRepo.findById(s.getChildId()).orElse(null);
                if (child != null) {
                    totalMonthlyValue = totalMonthlyValue.add(child.getEducationAmount());
                }
                String ct = s.getCommitmentType() != null ? s.getCommitmentType().name() : "UNKNOWN";
                commitmentBreakdown.merge(ct, 1, Integer::sum);
            }
        }

        // Determine dominant currency from children of active sponsorships (default PKR)
        String currency = "PKR";

        return ResponseEntity.ok(new PortfolioReport(
                activeCount, pendingCount, expiredCount,
                totalMonthlyValue, currency, commitmentBreakdown));
    }

    public record MonthlyBalance(
            String yearMonth,
            BigDecimal openingBalance,
            BigDecimal credits,
            BigDecimal debits,
            BigDecimal closingBalance,
            boolean isForecast
    ) {}

    public record CashFlowReport(
            List<MonthlyBalance> months,
            BigDecimal currentBalance,
            BigDecimal totalCredits3Month,
            BigDecimal totalDebits3Month
    ) {}

    public record PortfolioReport(
            int activeCount,
            int pendingCount,
            int expiredCount,
            BigDecimal totalMonthlyValue,
            String currency,
            Map<String, Integer> commitmentBreakdown
    ) {}
}
