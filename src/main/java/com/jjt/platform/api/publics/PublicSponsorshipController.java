package com.jjt.platform.api.publics;

import com.jjt.platform.api.publics.dto.PublicSponsorshipRequest;
import com.jjt.platform.api.publics.dto.PublicSponsorshipResponse;
import com.jjt.platform.api.publics.service.PublicSponsorshipService;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/public")
public class PublicSponsorshipController {

    private final PublicSponsorshipService sponsorshipService;

    @PostMapping("/sponsorships")
    public ResponseEntity<PublicSponsorshipResponse> commitPublic(@RequestBody PublicSponsorshipRequest request) {
        log.info("Received public sponsorship request for child: {}", request != null ? request.childId() : "null");
        
        Validate.notNull(request, "Request cannot be null");
        Validate.notNull(request.childId(), "Child ID cannot be null");
        Validate.notNull(request.commitmentType(), "Commitment type cannot be null");
        Validate.notNull(request.sponsor(), "Sponsor information cannot be null");
        
        try {
            var sponsor = request.sponsor();
            var sponsorship = sponsorshipService.commitPublic(
                    request.childId(),
                    request.commitmentType(),
                    sponsor != null ? sponsor.name() : null,
                    sponsor != null ? sponsor.email() : null,
                    sponsor != null ? sponsor.phone() : null
            );
            log.info("Successfully created public sponsorship with ID: {} for child: {}", 
                    sponsorship.getId(), request.childId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new PublicSponsorshipResponse(
                            sponsorship.getChildId(),
                            sponsorship.getStartMonth().getValue().toString()
                    ));
        } catch (Exception e) {
            log.error("Failed to create public sponsorship for child: {}: {}", 
                    request.childId(), e.getMessage(), e);
            throw e;
        }
    }

    @ExceptionHandler(SponsorshipInvariantViolationException.class)
    public ResponseEntity<String> handleSponsorshipConflict(SponsorshipInvariantViolationException ex) {
        log.warn("Sponsorship conflict: {}", ex.getMessage());
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
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Child already has an active sponsorship.");
    }
}
