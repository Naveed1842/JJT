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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminCommandService adminService;

    @PostMapping("/children")
    public ResponseEntity<CreateChildResponse> createChild(@RequestBody CreateChildRequest request) {
        log.info("Creating child with rollNumber: {}", request.rollNumber());
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(request, "Request cannot be null");
        Validate.notBlank(request.rollNumber(), "Roll number cannot be blank");
        Validate.notBlank(request.fullName(), "Full name cannot be blank");
        
        try {
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
            log.info("Successfully created child with ID: {} and ledger ID: {}", result.child().getId(), result.ledger().getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreateChildResponse(result.child().getId(), result.ledger().getId()));
        } catch (Exception e) {
            log.error("Failed to create child with rollNumber {}: {}", request.rollNumber(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/sponsors")
    public ResponseEntity<CreateSponsorResponse> createSponsor(@RequestBody CreateSponsorRequest request) {
        log.info("Creating sponsor with email: {}", request.contactEmail());
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(request, "Request cannot be null");
        Validate.notBlank(request.displayName(), "Display name cannot be blank");
        Validate.notBlank(request.contactEmail(), "Contact email cannot be blank");
        
        try {
            var sponsor = adminService.createSponsor(
                    request.displayName(),
                    request.contactEmail(),
                    request.phone(),
                    request.sponsorId()
            );
            log.info("Successfully created sponsor with ID: {} and display name: {}", sponsor.getId(), sponsor.getDisplayName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreateSponsorResponse(sponsor.getId(), sponsor.getDisplayName(), sponsor.getContactEmail()));
        } catch (Exception e) {
            log.error("Failed to create sponsor with email {}: {}", request.contactEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/early-support")
    public ResponseEntity<RecordEarlySupportResponse> recordEarlySupport(@RequestBody RecordEarlySupportRequest request) {
        log.info("Recording early support for child: {} for month: {}", request.childId(), request.month());
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(request, "Request cannot be null");
        Validate.notNull(request.childId(), "Child ID cannot be null");
        Validate.notBlank(request.month(), "Month cannot be blank");
        
        try {
            var entry = adminService.recordEarlySupport(
                    request.childId(),
                    YearMonthValue.of(YearMonth.parse(request.month())),
                    Money.of(new BigDecimal(request.educationAmount()), Currency.getInstance(request.educationCurrency())),
                    request.ledgerEntryId()
            );
            LedgerEntryDto dto = DtoMapper.toLedgerEntryDto(entry);
            log.info("Successfully recorded early support for child: {} with amount: {}", request.childId(), request.educationAmount());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RecordEarlySupportResponse(dto.id(), entry.getChildId(), dto.month()));
        } catch (Exception e) {
            log.error("Failed to record early support for child: {} and month: {}: {}", 
                    request.childId(), request.month(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/children/{childId}/progress")
    public ResponseEntity<AddProgressResponse> addProgress(@PathVariable UUID childId,
                                                           @RequestBody AddProgressRequest request) {
        log.info("Adding progress for child: {} for month: {}", childId, request.month());
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(childId, "Child ID cannot be null");
        Validate.notNull(request, "Request cannot be null");
        Validate.notBlank(request.month(), "Month cannot be blank");
        Validate.notBlank(request.summary(), "Summary cannot be blank");
        
        try {
            var progress = adminService.addProgress(
                    childId,
                    YearMonthValue.of(YearMonth.parse(request.month())),
                    request.summary(),
                    request.progressUpdateId()
            );
            log.info("Successfully added progress for child: {} for month: {}", childId, request.month());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AddProgressResponse(progress.getId(), progress.getChildId(), progress.getMonth().getValue().toString()));
        } catch (Exception e) {
            log.error("Failed to add progress for child: {} and month: {}: {}", childId, request.month(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/sponsorships")
    public ResponseEntity<CommitSponsorshipResponse> commitSponsorship(@RequestBody CommitSponsorshipRequest request) {
        log.info("Committing sponsorship for sponsor: {} and child: {}", request.sponsorId(), request.childId());
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN, Role.SPONSOR);
        
        Validate.notNull(request, "Request cannot be null");
        Validate.notNull(request.sponsorId(), "Sponsor ID cannot be null");
        Validate.notNull(request.childId(), "Child ID cannot be null");
        Validate.notBlank(request.startMonth(), "Start month cannot be blank");
        
        try {
            var sponsorship = adminService.commitSponsorship(
                    request.sponsorId(),
                    request.childId(),
                    YearMonthValue.of(YearMonth.parse(request.startMonth())),
                    request.sponsorshipId(),
                    request.commitmentType()
            );
            log.info("Successfully committed sponsorship with ID: {} for sponsor: {} and child: {}", 
                    sponsorship.getId(), request.sponsorId(), request.childId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CommitSponsorshipResponse(
                            sponsorship.getId(),
                            sponsorship.getSponsorId(),
                            sponsorship.getChildId(),
                            sponsorship.getStartMonth().getValue().toString()
                    ));
        } catch (Exception e) {
            log.error("Failed to commit sponsorship for sponsor: {} and child: {}: {}", 
                    request.sponsorId(), request.childId(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/sponsorships")
    public List<SponsorshipSummaryResponse> listSponsorships(@RequestParam(name = "status", required = false) String status) {
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
    public ResponseEntity<CommitSponsorshipResponse> activate(@PathVariable UUID sponsorshipId) {
        log.info("Activating sponsorship with ID: {}", sponsorshipId);
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(sponsorshipId, "Sponsorship ID cannot be null");
        
        try {
            var sponsorship = adminService.activateSponsorship(sponsorshipId);
            log.info("Successfully activated sponsorship with ID: {} for child: {}", sponsorshipId, sponsorship.getChildId());
            return ResponseEntity.ok(new CommitSponsorshipResponse(
                    sponsorship.getId(),
                    sponsorship.getSponsorId(),
                    sponsorship.getChildId(),
                    sponsorship.getStartMonth().getValue().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to activate sponsorship with ID: {}: {}", sponsorshipId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/sponsorships/{sponsorshipId}/expire")
    public ResponseEntity<CommitSponsorshipResponse> expire(@PathVariable UUID sponsorshipId) {
        log.info("Expiring sponsorship with ID: {}", sponsorshipId);
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(sponsorshipId, "Sponsorship ID cannot be null");
        
        try {
            var sponsorship = adminService.expireSponsorship(sponsorshipId);
            log.info("Successfully expired sponsorship with ID: {} for child: {}", sponsorshipId, sponsorship.getChildId());
            return ResponseEntity.ok(new CommitSponsorshipResponse(
                    sponsorship.getId(),
                    sponsorship.getSponsorId(),
                    sponsorship.getChildId(),
                    sponsorship.getStartMonth().getValue().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to expire sponsorship with ID: {}: {}", sponsorshipId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/children/{childId}/sponsorships")
    public List<SponsorshipSummaryResponse> listSponsorshipsByChild(@PathVariable UUID childId) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(childId, "Child ID cannot be null");
        
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
    public Map<String, Boolean> hasActive(@PathVariable UUID childId) {
        AccessGuard.requireRole(Role.JJT_ADMIN, Role.ORG_ADMIN);
        
        Validate.notNull(childId, "Child ID cannot be null");
        
        return Map.of("active", adminService.hasActiveSponsorship(childId));
    }

    @ExceptionHandler(LedgerInvariantViolationException.class)
    public ResponseEntity<String> handleConflict(LedgerInvariantViolationException ex) {
        log.warn("Ledger invariant violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SponsorshipInvariantViolationException.class)
    public ResponseEntity<String> handleSponsorshipConflict(SponsorshipInvariantViolationException ex) {
        log.warn("Sponsorship invariant violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handleDomain(DomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Sponsorship already exists for sponsor, child, and start month.");
    }
}
