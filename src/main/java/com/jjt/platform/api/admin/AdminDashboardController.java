package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.DashboardResponse;
import com.jjt.platform.api.admin.service.AdminAlertService;
import com.jjt.platform.api.admin.service.AdminFundService;
import com.jjt.platform.api.admin.service.AdminPaymentService;
import com.jjt.platform.api.admin.service.AdminPaymentService.MonthlyReconciliation;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminDashboardController {

    private final AdminFundService fundService;
    private final AdminPaymentService paymentService;
    private final AdminAlertService alertService;
    private final ChildJpaRepository childRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;

    public AdminDashboardController(AdminFundService fundService,
                                    AdminPaymentService paymentService,
                                    AdminAlertService alertService,
                                    ChildJpaRepository childRepo,
                                    SponsorshipJpaRepository sponsorshipRepo) {
        this.fundService = fundService;
        this.paymentService = paymentService;
        this.alertService = alertService;
        this.childRepo = childRepo;
        this.sponsorshipRepo = sponsorshipRepo;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal JwtUserDetails principal) {
        UUID orgId = principal.getOrgId();

        // Fund accounts
        List<DashboardResponse.FundSummary> fundSummaries = fundService.listFundAccounts().stream()
                .map(fab -> new DashboardResponse.FundSummary(
                        fab.account().getId(),
                        fab.account().getName(),
                        fab.balance(),
                        fab.account().getCurrency(),
                        fab.isBelowMinReserve()
                ))
                .toList();

        // Children stats
        List<ChildEntity> children = childRepo.findByOrganisationId(orgId);
        int total = children.size();
        int enrolledCount = 0;
        int availableCount = 0;
        for (ChildEntity child : children) {
            boolean hasActive = sponsorshipRepo.existsByChildIdAndStatus(child.getId(), SponsorshipStatus.ACTIVE);
            if (hasActive) {
                enrolledCount++;
            } else {
                boolean hasPending = sponsorshipRepo.existsByChildIdAndStatus(child.getId(), SponsorshipStatus.PENDING);
                if (!hasPending) {
                    availableCount++;
                }
            }
        }
        DashboardResponse.ChildrenStats childrenStats = new DashboardResponse.ChildrenStats(
                total, availableCount, enrolledCount);

        // Payment stats for current month
        LocalDate now = LocalDate.now();
        MonthlyReconciliation recon = paymentService.getMonthlyReconciliation(
                now.getYear(), now.getMonthValue(), orgId);
        MonthlyReconciliation.Summary summary = recon.summary();
        DashboardResponse.PaymentStats paymentStats = new DashboardResponse.PaymentStats(
                recon.year(),
                recon.month(),
                summary.expected(),
                summary.received(),
                summary.overdue(),
                summary.waived(),
                summary.total()
        );

        // Active alert count
        int activeAlertCount = alertService.listActive(orgId).size();

        DashboardResponse response = new DashboardResponse(
                fundSummaries, childrenStats, paymentStats, activeAlertCount);
        return ResponseEntity.ok(response);
    }
}
