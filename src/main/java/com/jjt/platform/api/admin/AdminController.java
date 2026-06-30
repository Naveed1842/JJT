package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.*;
import com.jjt.platform.api.admin.service.AdminCommandService;
import com.jjt.platform.api.common.dto.LedgerEntryDto;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminController {

    private final AdminCommandService adminService;
    private final SponsorJpaRepository sponsorRepo;

    public AdminController(AdminCommandService adminService, SponsorJpaRepository sponsorRepo) {
        this.adminService = adminService;
        this.sponsorRepo = sponsorRepo;
    }

    @GetMapping("/sponsors")
    public List<CreateSponsorResponse> listSponsors() {
        return sponsorRepo.findAll().stream()
                .map(s -> new CreateSponsorResponse(s.getId(), s.getDisplayName(), s.getContactEmail()))
                .toList();
    }

    @PostMapping("/children")
    public ResponseEntity<CreateChildResponse> createChild(@Valid @RequestBody CreateChildRequest request) {
        var result = adminService.createChild(
                request.rollNumber(),
                request.fullName(),
                request.city(),
                request.campusName(),
                request.schoolName(),
                Money.of(new BigDecimal(request.educationAmount()), Currency.getInstance(request.educationCurrency())),
                request.childId(),
                request.ledgerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateChildResponse(result.child().getId(), result.ledger().getId()));
    }

    @PostMapping("/sponsors")
    public ResponseEntity<CreateSponsorResponse> createSponsor(@Valid @RequestBody CreateSponsorRequest request) {
        var sponsor = adminService.createSponsor(
                request.displayName(),
                request.contactEmail(),
                request.phone(),
                request.sponsorId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateSponsorResponse(sponsor.getId(), sponsor.getDisplayName(), sponsor.getContactEmail()));
    }

    @PostMapping("/early-support")
    public ResponseEntity<RecordEarlySupportResponse> recordEarlySupport(
            @Valid @RequestBody RecordEarlySupportRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var entry = adminService.recordEarlySupport(
                request.childId(),
                YearMonthValue.of(YearMonth.parse(request.month())),
                Money.of(new BigDecimal(request.educationAmount()), Currency.getInstance(request.educationCurrency())),
                request.ledgerEntryId(),
                principal.getId(),
                request.force(),
                request.forceReason()
        );
        LedgerEntryDto dto = DtoMapper.toLedgerEntryDto(entry);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RecordEarlySupportResponse(dto.id(), entry.getChildId(), dto.month()));
    }

    @PostMapping("/children/{childId}/progress")
    public ResponseEntity<AddProgressResponse> addProgress(
            @PathVariable("childId") UUID childId,
            @Valid @RequestBody AddProgressRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var progress = adminService.addProgress(
                childId,
                YearMonthValue.of(YearMonth.parse(request.month())),
                request.summary(),
                request.progressUpdateId(),
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddProgressResponse(progress.getId(), progress.getChildId(), progress.getMonth().getValue().toString()));
    }

    @PostMapping("/sponsorships")
    public ResponseEntity<CommitSponsorshipResponse> commitSponsorship(
            @Valid @RequestBody CommitSponsorshipRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var sponsorship = adminService.commitSponsorship(
                request.sponsorId(),
                request.childId(),
                YearMonthValue.of(YearMonth.parse(request.startMonth())),
                request.sponsorshipId(),
                request.commitmentType(),
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommitSponsorshipResponse(
                        sponsorship.getId(),
                        sponsorship.getSponsorId(),
                        sponsorship.getChildId(),
                        sponsorship.getStartMonth().getValue().toString()
                ));
    }

    @GetMapping("/sponsorships")
    public List<SponsorshipSummaryResponse> listSponsorships(
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false) String status) {
        SponsorshipStatus target = status != null ? SponsorshipStatus.valueOf(status) : SponsorshipStatus.PENDING;
        return adminService.findEntitiesByStatus(target).stream()
                .map(s -> new SponsorshipSummaryResponse(
                        s.getId(),
                        s.getChildId(),
                        s.getSponsor().getId(),
                        s.getSponsor().getDisplayName(),
                        s.getSponsor().getContactEmail(),
                        s.getSponsor().getPhone(),
                        s.getCommitmentType().name(),
                        s.getStartMonth(),
                        s.getStatus().name(),
                        s.getCreatedAt()))
                .toList();
    }

    @PostMapping("/sponsorships/{sponsorshipId}/activate")
    public ResponseEntity<CommitSponsorshipResponse> activate(@PathVariable("sponsorshipId") UUID sponsorshipId) {
        var sponsorship = adminService.activateSponsorship(sponsorshipId);
        return ResponseEntity.ok(new CommitSponsorshipResponse(
                sponsorship.getId(),
                sponsorship.getSponsorId(),
                sponsorship.getChildId(),
                sponsorship.getStartMonth().getValue().toString()
        ));
    }

    @PostMapping("/sponsorships/{sponsorshipId}/expire")
    public ResponseEntity<CommitSponsorshipResponse> expire(@PathVariable("sponsorshipId") UUID sponsorshipId) {
        var sponsorship = adminService.expireSponsorship(sponsorshipId);
        return ResponseEntity.ok(new CommitSponsorshipResponse(
                sponsorship.getId(),
                sponsorship.getSponsorId(),
                sponsorship.getChildId(),
                sponsorship.getStartMonth().getValue().toString()
        ));
    }

    @GetMapping("/children/{childId}/sponsorships")
    public List<SponsorshipSummaryResponse> listSponsorshipsByChild(@PathVariable("childId") UUID childId) {
        return adminService.findSponsorshipsByChild(childId).stream()
                .map(s -> new SponsorshipSummaryResponse(
                        s.getId(),
                        s.getChildId(),
                        s.getSponsor().getId(),
                        s.getSponsor().getDisplayName(),
                        s.getSponsor().getContactEmail(),
                        s.getSponsor().getPhone(),
                        s.getCommitmentType().name(),
                        s.getStartMonth(),
                        s.getStatus().name(),
                        s.getCreatedAt()))
                .toList();
    }

    @GetMapping("/children/{childId}/sponsorships/active")
    public Map<String, Boolean> hasActive(@PathVariable("childId") UUID childId) {
        return Collections.singletonMap("active", adminService.hasActiveSponsorship(childId));
    }
}
