package com.jjt.platform.api.publics;

import com.jjt.platform.api.publics.dto.PublicSponsorshipRequest;
import com.jjt.platform.api.publics.dto.PublicSponsorshipResponse;
import com.jjt.platform.api.publics.service.PublicSponsorshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PublicSponsorshipResponse> commitPublic(@Valid @RequestBody PublicSponsorshipRequest request) {
        var sponsor = request.sponsor();
        var sponsorship = sponsorshipService.commitPublic(
                request.childId(),
                request.commitmentType(),
                sponsor.name(),
                sponsor.email(),
                sponsor.phone()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PublicSponsorshipResponse(
                        sponsorship.getChildId(),
                        sponsorship.getStartMonth().getValue().toString()
                ));
    }
}
