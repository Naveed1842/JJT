package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.*;
import com.jjt.platform.api.admin.service.AdminCommandService;
import com.jjt.platform.api.common.dto.LedgerEntryDto;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.api.common.security.AccessGuard;
import com.jjt.platform.config.security.Role;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.LedgerInvariantViolationException;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.time.YearMonth;
import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin operations for managing children, sponsors, and sponsorships")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminCommandService adminService;

    public AdminController(AdminCommandService adminService) {
        this.adminService = adminService;
    }

    // Placeholder role check: assume caller is admin/org-admin.

    @PostMapping("/children")
    @Operation(summary = "Create a new child", description = "Register a new child in the system")
    public ResponseEntity<CreateChildResponse> createChild(@RequestBody CreateChildRequest request) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
    public ResponseEntity<CreateSponsorResponse> createSponsor(@RequestBody CreateSponsorRequest request) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
    public ResponseEntity<RecordEarlySupportResponse> recordEarlySupport(@RequestBody RecordEarlySupportRequest request) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        var entry = adminService.recordEarlySupport(
                request.childId(),
                YearMonthValue.of(YearMonth.parse(request.month())),
                Money.of(new BigDecimal(request.educationAmount()), Currency.getInstance(request.educationCurrency())),
                request.ledgerEntryId()
        );
        LedgerEntryDto dto = DtoMapper.toLedgerEntryDto(entry);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RecordEarlySupportResponse(dto.id(), entry.getChildId(), dto.month()));
    }

    @PostMapping("/children/{childId}/progress")
    public ResponseEntity<AddProgressResponse> addProgress(@org.springframework.web.bind.annotation.PathVariable java.util.UUID childId,
                                                           @RequestBody AddProgressRequest request) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        var progress = adminService.addProgress(
                childId,
                YearMonthValue.of(YearMonth.parse(request.month())),
                request.summary(),
                request.progressUpdateId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddProgressResponse(progress.getId(), progress.getChildId(), progress.getMonth().getValue().toString()));
    }

    @PostMapping("/sponsorships")
    public ResponseEntity<CommitSponsorshipResponse> commitSponsorship(@RequestBody CommitSponsorshipRequest request) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN, Role.SPONSOR);
        var sponsorship = adminService.commitSponsorship(
                request.sponsorId(),
                request.childId(),
                YearMonthValue.of(YearMonth.parse(request.startMonth())),
                request.sponsorshipId(),
                request.commitmentType()
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
    public List<SponsorshipSummaryResponse> listSponsorships(@org.springframework.web.bind.annotation.RequestParam(name = "status", required = false) String status) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
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
    public java.util.Map<String, Boolean> hasActive(@PathVariable("childId") UUID childId) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        return java.util.Collections.singletonMap("active", adminService.hasActiveSponsorship(childId));
    }

    @ExceptionHandler(LedgerInvariantViolationException.class)
    public ResponseEntity<String> handleConflict(LedgerInvariantViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SponsorshipInvariantViolationException.class)
    public ResponseEntity<String> handleSponsorshipConflict(SponsorshipInvariantViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handleDomain(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Sponsorship already exists for sponsor, child, and start month.");
    }
}
