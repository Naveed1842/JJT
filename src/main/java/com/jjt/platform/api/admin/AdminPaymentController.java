package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.MonthlyReconciliationResponse;
import com.jjt.platform.api.admin.dto.RecordPaymentRequest;
import com.jjt.platform.api.admin.dto.SponsorPaymentResponse;
import com.jjt.platform.api.admin.dto.WaivePaymentRequest;
import com.jjt.platform.api.admin.service.AdminPaymentService;
import com.jjt.platform.api.admin.service.AdminPaymentService.MonthlyReconciliation;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorPayment;
import com.jjt.platform.infrastructure.audit.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminPaymentController {

    private final AdminPaymentService paymentService;
    private final AuditService auditService;

    public AdminPaymentController(AdminPaymentService paymentService, AuditService auditService) {
        this.paymentService = paymentService;
        this.auditService = auditService;
    }

    @PostMapping("/payments/{paymentId}/receive")
    public ResponseEntity<SponsorPaymentResponse> receivePayment(
            @PathVariable("paymentId") UUID paymentId,
            @Valid @RequestBody RecordPaymentRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        SponsorPayment payment = paymentService.recordPaymentReceived(
                paymentId,
                request.receivedAmount(),
                request.currency(),
                request.bankReference(),
                request.receivedDate(),
                principal.getId()
        );
        auditService.log(principal.getOrgId(), "PAYMENT_RECEIVED", principal.getId(), principal.getUsername(),
                "SponsorPayment", paymentId,
                "Recorded payment of " + request.currency() + " " + request.receivedAmount()
                        + " for " + payment.getPaymentMonth().getValue()
                        + (request.bankReference() != null ? " (ref " + request.bankReference() + ")" : ""));
        return ResponseEntity.ok(toResponse(payment));
    }

    @PostMapping("/payments/{paymentId}/waive")
    public ResponseEntity<SponsorPaymentResponse> waivePayment(
            @PathVariable("paymentId") UUID paymentId,
            @Valid @RequestBody WaivePaymentRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        SponsorPayment payment = paymentService.waivePayment(paymentId, request.reason(), principal.getId());
        auditService.log(principal.getOrgId(), "PAYMENT_WAIVED", principal.getId(), principal.getUsername(),
                "SponsorPayment", paymentId,
                "Waived payment for " + payment.getPaymentMonth().getValue() + ": " + request.reason());
        return ResponseEntity.ok(toResponse(payment));
    }

    @GetMapping("/payments/{paymentId}")
    public SponsorPaymentResponse getPayment(@PathVariable("paymentId") UUID paymentId) {
        return toResponse(paymentService.getPayment(paymentId));
    }

    @GetMapping("/sponsorships/{sponsorshipId}/payments")
    public List<SponsorPaymentResponse> getPaymentsBySponsorship(
            @PathVariable("sponsorshipId") UUID sponsorshipId) {
        return paymentService.getPaymentsBySponsorship(sponsorshipId)
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/reconciliation/monthly")
    public MonthlyReconciliationResponse getMonthlyReconciliation(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal JwtUserDetails principal) {
        MonthlyReconciliation result = paymentService.getMonthlyReconciliation(year, month, principal.getOrgId());
        return new MonthlyReconciliationResponse(
                result.year(),
                result.month(),
                new MonthlyReconciliationResponse.Summary(
                        result.summary().expected(),
                        result.summary().received(),
                        result.summary().partial(),
                        result.summary().overdue(),
                        result.summary().waived(),
                        result.summary().total()),
                result.atRisk().stream()
                        .map(ar -> new MonthlyReconciliationResponse.AtRiskSponsorship(
                                ar.sponsorshipId(), ar.sponsorId(), ar.sponsorName(),
                                ar.childId(), ar.childName(),
                                ar.consecutiveOverdueMonths(), ar.requiresEscalation()))
                        .toList(),
                result.payments().stream().map(this::toResponse).toList()
        );
    }

    @PostMapping("/payments/generate")
    @PreAuthorize("hasRole('JJT_ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> triggerGeneratePayments(
            @RequestParam(value = "month", required = false) String month,
            @AuthenticationPrincipal JwtUserDetails principal) {
        YearMonth target = month != null ? YearMonth.parse(month) : YearMonth.now();
        int created = paymentService.generateExpectedPayments(target, principal.getOrgId());
        auditService.log(principal.getOrgId(), "PAYMENTS_GENERATED", principal.getId(), principal.getUsername(),
                "SponsorPayment", null,
                "Generated " + created + " expected payment(s) for " + target);
        return ResponseEntity.ok(java.util.Map.of("month", target.toString(), "created", created));
    }

    private SponsorPaymentResponse toResponse(SponsorPayment sp) {
        return new SponsorPaymentResponse(
                sp.getId(),
                sp.getSponsorshipId(),
                sp.getSponsorId(),
                sp.getChildId(),
                sp.getPaymentMonth().getValue().toString(),
                sp.getStatus().name(),
                sp.getExpectedAmount().getAmount(),
                sp.getExpectedAmount().getCurrency().getCurrencyCode(),
                sp.getReceivedAmount() != null ? sp.getReceivedAmount().getAmount() : null,
                sp.getReceivedAmount() != null ? sp.getReceivedAmount().getCurrency().getCurrencyCode() : null,
                sp.getBankReference(),
                sp.getReceivedDate(),
                sp.getWaiverReason(),
                sp.getFundTransactionId(),
                sp.getLedgerEntryId(),
                sp.getCreatedAt(),
                sp.getUpdatedAt()
        );
    }
}
