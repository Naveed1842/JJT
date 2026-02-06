package com.jjt.platform.api.publics;

import com.jjt.platform.api.publics.dto.PublicSponsorshipRequest;
import com.jjt.platform.api.publics.dto.PublicSponsorshipResponse;
import com.jjt.platform.api.publics.service.PublicSponsorshipService;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.SponsorshipInvariantViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicSponsorshipController {

    private final PublicSponsorshipService sponsorshipService;

    public PublicSponsorshipController(PublicSponsorshipService sponsorshipService) {
        this.sponsorshipService = sponsorshipService;
    }

    @PostMapping("/sponsorships")
    public ResponseEntity<PublicSponsorshipResponse> commitPublic(@RequestBody PublicSponsorshipRequest request) {
        if (request == null || request.childId() == null || request.commitmentType() == null || request.sponsor() == null) {
            return ResponseEntity.badRequest().build();
        }
        var sponsor = request.sponsor();
        var sponsorship = sponsorshipService.commitPublic(
                request.childId(),
                request.commitmentType(),
                sponsor != null ? sponsor.name() : null,
                sponsor != null ? sponsor.email() : null,
                sponsor != null ? sponsor.phone() : null
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PublicSponsorshipResponse(
                        sponsorship.getChildId(),
                        sponsorship.getStartMonth().getValue().toString()
                ));
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
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Child already has an active sponsorship.");
    }
}
