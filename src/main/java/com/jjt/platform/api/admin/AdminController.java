package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.*;
import com.jjt.platform.api.admin.service.AdminCommandService;
import com.jjt.platform.api.common.dto.LedgerEntryDto;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.audit.AuditService;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    private final AuditService auditService;

    public AdminController(AdminCommandService adminService, SponsorJpaRepository sponsorRepo,
                           AuditService auditService) {
        this.adminService = adminService;
        this.sponsorRepo = sponsorRepo;
        this.auditService = auditService;
    }

    @GetMapping("/sponsors")
    public List<CreateSponsorResponse> listSponsors(@AuthenticationPrincipal JwtUserDetails principal) {
        UUID orgId = principal.getOrgId();
        List<?> sponsors = orgId != null
                ? sponsorRepo.findByOrganisationId(orgId)
                : sponsorRepo.findAll();
        return sponsors.stream()
                .map(s -> {
                    var sp = (com.jjt.platform.infrastructure.persistence.entity.SponsorEntity) s;
                    return new CreateSponsorResponse(sp.getId(), sp.getDisplayName(), sp.getContactEmail());
                })
                .toList();
    }

    @PostMapping("/children")
    public ResponseEntity<CreateChildResponse> createChild(
            @Valid @RequestBody CreateChildRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var result = adminService.createChild(
                request.rollNumber(),
                request.fullName(),
                request.city(),
                request.campusName(),
                request.schoolName(),
                Money.of(new BigDecimal(request.educationAmount()), Currency.getInstance(request.educationCurrency())),
                request.childId(),
                request.ledgerId(),
                principal.getOrgId()
        );
        auditService.log(principal.getOrgId(), "CHILD_CREATED", principal.getId(), principal.getUsername(),
                "Child", result.child().getId(),
                "Created child '" + request.fullName() + "' (roll " + request.rollNumber() + ", " + request.campusName() + ")");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateChildResponse(result.child().getId(), result.ledger().getId()));
    }

    @PostMapping("/sponsors")
    public ResponseEntity<CreateSponsorResponse> createSponsor(
            @Valid @RequestBody CreateSponsorRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var sponsor = adminService.createSponsor(
                request.displayName(),
                request.contactEmail(),
                request.phone(),
                request.sponsorId(),
                principal.getOrgId()
        );
        auditService.log(principal.getOrgId(), "SPONSOR_CREATED", principal.getId(), principal.getUsername(),
                "Sponsor", sponsor.getId(),
                "Created sponsor '" + sponsor.getDisplayName() + "' (" + sponsor.getContactEmail() + ")");
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
                request.forceReason(),
                principal.getOrgId()
        );
        LedgerEntryDto dto = DtoMapper.toLedgerEntryDto(entry);
        auditService.log(principal.getOrgId(), "EARLY_SUPPORT_RECORDED", principal.getId(), principal.getUsername(),
                "LedgerEntry", dto.id(),
                "Recorded early support for child " + entry.getChildId() + " for " + dto.month()
                        + " (" + request.educationCurrency() + " " + request.educationAmount() + ")"
                        + (request.force() ? " [FORCED: " + request.forceReason() + "]" : ""));
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
                principal.getId(),
                principal.getOrgId()
        );
        auditService.log(principal.getOrgId(), "PROGRESS_ADDED", principal.getId(), principal.getUsername(),
                "ProgressUpdate", progress.getId(),
                "Added progress update for child " + childId + " for " + progress.getMonth().getValue());
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
                principal.getId(),
                principal.getOrgId()
        );
        auditService.log(principal.getOrgId(), "SPONSORSHIP_COMMITTED", principal.getId(), principal.getUsername(),
                "Sponsorship", sponsorship.getId(),
                "Committed sponsorship for child " + request.childId() + " by sponsor " + request.sponsorId()
                        + " starting " + request.startMonth());
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
            @RequestParam(name = "status", required = false) String status,
            @AuthenticationPrincipal JwtUserDetails principal) {
        SponsorshipStatus target = status != null ? SponsorshipStatus.valueOf(status) : SponsorshipStatus.PENDING;
        return adminService.findEntitiesByStatus(target, principal.getOrgId()).stream()
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
    public ResponseEntity<CommitSponsorshipResponse> activate(
            @PathVariable("sponsorshipId") UUID sponsorshipId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var sponsorship = adminService.activateSponsorship(sponsorshipId);
        auditService.log(principal.getOrgId(), "SPONSORSHIP_ACTIVATED", principal.getId(), principal.getUsername(),
                "Sponsorship", sponsorshipId,
                "Activated sponsorship for child " + sponsorship.getChildId());
        return ResponseEntity.ok(new CommitSponsorshipResponse(
                sponsorship.getId(),
                sponsorship.getSponsorId(),
                sponsorship.getChildId(),
                sponsorship.getStartMonth().getValue().toString()
        ));
    }

    @PostMapping("/sponsorships/{sponsorshipId}/expire")
    public ResponseEntity<CommitSponsorshipResponse> expire(
            @PathVariable("sponsorshipId") UUID sponsorshipId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        var sponsorship = adminService.expireSponsorship(sponsorshipId);
        auditService.log(principal.getOrgId(), "SPONSORSHIP_EXPIRED", principal.getId(), principal.getUsername(),
                "Sponsorship", sponsorshipId,
                "Expired sponsorship for child " + sponsorship.getChildId());
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
