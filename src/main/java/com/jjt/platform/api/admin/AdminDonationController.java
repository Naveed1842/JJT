package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.CreateDonorRequest;
import com.jjt.platform.api.admin.dto.CreateRecurringDonationRequest;
import com.jjt.platform.api.admin.dto.DonationReceiptResponse;
import com.jjt.platform.api.admin.dto.DonationResponse;
import com.jjt.platform.api.admin.dto.DonorResponse;
import com.jjt.platform.api.admin.dto.RecordDonationRequest;
import com.jjt.platform.api.admin.dto.RecurringDonationResponse;
import com.jjt.platform.api.admin.service.AdminDonationService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.Donation;
import com.jjt.platform.core.domain.entity.Donor;
import com.jjt.platform.core.domain.entity.RecurringDonationSchedule;
import com.jjt.platform.core.domain.entity.RecurringDonationStatus;
import com.jjt.platform.infrastructure.persistence.repository.DonorJpaRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminDonationController {

    private final AdminDonationService donationService;
    private final DonorJpaRepository donorRepo;

    public AdminDonationController(AdminDonationService donationService,
                                   DonorJpaRepository donorRepo) {
        this.donationService = donationService;
        this.donorRepo = donorRepo;
    }

    // ── Donors ───────────────────────────────────────────────────────────────

    @PostMapping("/donors")
    public ResponseEntity<DonorResponse> createDonor(
            @Valid @RequestBody CreateDonorRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donor donor = donationService.createDonor(
                request.displayName(), request.email(), request.phone(),
                request.donorType(), request.notes(),
                principal.getOrgId(), principal.getId());
        return ResponseEntity.ok(toDonorResponse(donor));
    }

    @GetMapping("/donors")
    public ResponseEntity<List<DonorResponse>> listDonors(
            @AuthenticationPrincipal JwtUserDetails principal) {
        List<DonorResponse> donors = donationService.listDonors(principal.getOrgId())
                .stream().map(this::toDonorResponse).toList();
        return ResponseEntity.ok(donors);
    }

    @GetMapping("/donors/{id}")
    public ResponseEntity<DonorResponse> getDonor(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donor donor = donationService.getDonor(id, principal.getOrgId());
        return ResponseEntity.ok(toDonorResponse(donor));
    }

    // ── Donations ────────────────────────────────────────────────────────────

    @PostMapping("/donations")
    public ResponseEntity<DonationResponse> recordDonation(
            @Valid @RequestBody RecordDonationRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donation donation = donationService.recordDonation(
                request.donorId(),
                request.donationType(),
                new BigDecimal(request.amount()),
                request.currency(),
                request.donationDate(),
                request.notes(),
                request.fundAccountId(),
                principal.getOrgId(),
                principal.getId());
        return ResponseEntity.ok(toDonationResponse(donation, principal.getOrgId()));
    }

    @GetMapping("/donations")
    public ResponseEntity<Page<DonationResponse>> listDonations(
            @AuthenticationPrincipal JwtUserDetails principal,
            @PageableDefault(size = 20, sort = "donationDate") Pageable pageable) {
        Page<DonationResponse> page = donationService.listDonations(principal.getOrgId(), pageable)
                .map(d -> toDonationResponse(d, principal.getOrgId()));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/donations/{id}")
    public ResponseEntity<DonationResponse> getDonation(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donation donation = donationService.getDonation(id, principal.getOrgId());
        return ResponseEntity.ok(toDonationResponse(donation, principal.getOrgId()));
    }

    @GetMapping("/donations/{id}/receipt")
    public ResponseEntity<DonationReceiptResponse> getReceipt(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        AdminDonationService.ReceiptData receipt = donationService.getReceiptData(id, principal.getOrgId());
        return ResponseEntity.ok(new DonationReceiptResponse(
                receipt.receiptNumber(),
                receipt.donorName(),
                receipt.donorEmail(),
                receipt.donorPhone(),
                receipt.amount(),
                receipt.currency(),
                receipt.donationDate(),
                receipt.donationType(),
                receipt.organisationName(),
                receipt.issuedDate()
        ));
    }

    @PostMapping("/donations/{id}/receive")
    public ResponseEntity<DonationResponse> receiveExpected(
            @PathVariable UUID id,
            @RequestParam(required = false) BigDecimal actualAmount,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donation donation = donationService.receiveExpectedDonation(
                id, actualAmount, principal.getOrgId(), principal.getId());
        return ResponseEntity.ok(toDonationResponse(donation, principal.getOrgId()));
    }

    @PostMapping("/donations/{id}/reverse")
    public ResponseEntity<DonationResponse> reverseDonation(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        Donation donation = donationService.reverseDonation(id, principal.getOrgId(), principal.getId());
        return ResponseEntity.ok(toDonationResponse(donation, principal.getOrgId()));
    }

    // ── Recurring schedules ──────────────────────────────────────────────────

    @PostMapping("/donations/recurring")
    public ResponseEntity<RecurringDonationResponse> createRecurring(
            @Valid @RequestBody CreateRecurringDonationRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        RecurringDonationSchedule schedule = donationService.createRecurringSchedule(
                request.donorId(),
                request.donationType(),
                new BigDecimal(request.amount()),
                request.currency(),
                request.frequency(),
                request.startDate(),
                request.endDate(),
                request.fundAccountId(),
                request.notes(),
                principal.getOrgId(),
                principal.getId());
        return ResponseEntity.ok(toRecurringResponse(schedule, principal.getOrgId()));
    }

    @GetMapping("/donations/recurring")
    public ResponseEntity<List<RecurringDonationResponse>> listRecurring(
            @AuthenticationPrincipal JwtUserDetails principal) {
        List<RecurringDonationResponse> schedules = donationService.listRecurringSchedules(principal.getOrgId())
                .stream().map(s -> toRecurringResponse(s, principal.getOrgId())).toList();
        return ResponseEntity.ok(schedules);
    }

    @PatchMapping("/donations/recurring/{id}/pause")
    public ResponseEntity<RecurringDonationResponse> pauseRecurring(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        RecurringDonationSchedule schedule = donationService.updateRecurringStatus(
                id, RecurringDonationStatus.PAUSED, principal.getOrgId());
        return ResponseEntity.ok(toRecurringResponse(schedule, principal.getOrgId()));
    }

    @PatchMapping("/donations/recurring/{id}/cancel")
    public ResponseEntity<RecurringDonationResponse> cancelRecurring(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        RecurringDonationSchedule schedule = donationService.updateRecurringStatus(
                id, RecurringDonationStatus.CANCELLED, principal.getOrgId());
        return ResponseEntity.ok(toRecurringResponse(schedule, principal.getOrgId()));
    }

    @PostMapping("/donations/recurring/generate")
    @PreAuthorize("hasRole('JJT_ADMIN')")
    public ResponseEntity<Integer> generateExpectedDonations(
            @AuthenticationPrincipal JwtUserDetails principal) {
        int created = donationService.generateExpectedFromRecurring(principal.getOrgId());
        return ResponseEntity.ok(created);
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private DonorResponse toDonorResponse(Donor donor) {
        return new DonorResponse(
                donor.getId(),
                donor.getDisplayName(),
                donor.getEmail(),
                donor.getPhone(),
                donor.getDonorType().name(),
                donor.getNotes(),
                donor.getCreatedAt()
        );
    }

    private DonationResponse toDonationResponse(Donation donation, UUID orgId) {
        String donorName = null;
        if (donation.getDonorId() != null) {
            donorName = donorRepo.findById(donation.getDonorId())
                    .map(e -> e.getDisplayName())
                    .orElse(null);
        }
        return new DonationResponse(
                donation.getId(),
                donation.getDonorId(),
                donorName,
                donation.getDonationType().name(),
                donation.getAmount().getAmount(),
                donation.getAmount().getCurrency().getCurrencyCode(),
                donation.getDonationDate(),
                donation.getReceiptNumber(),
                donation.getStatus().name(),
                donation.getFundAccountId(),
                donation.getFundTransactionId(),
                donation.getRecurringScheduleId(),
                donation.getCreatedAt()
        );
    }

    private RecurringDonationResponse toRecurringResponse(RecurringDonationSchedule schedule, UUID orgId) {
        String donorName = donorRepo.findById(schedule.getDonorId())
                .map(e -> e.getDisplayName())
                .orElse(null);
        return new RecurringDonationResponse(
                schedule.getId(),
                schedule.getDonorId(),
                donorName,
                schedule.getDonationType().name(),
                schedule.getAmount().getAmount(),
                schedule.getAmount().getCurrency().getCurrencyCode(),
                schedule.getFrequency().name(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getNextDueDate(),
                schedule.getStatus().name(),
                schedule.getCreatedAt()
        );
    }
}
