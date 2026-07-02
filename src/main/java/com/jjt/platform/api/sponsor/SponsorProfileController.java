package com.jjt.platform.api.sponsor;

import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RestController
@RequestMapping("/api/sponsor/profile")
@PreAuthorize("hasRole('SPONSOR')")
public class SponsorProfileController {

    private final SponsorJpaRepository sponsorRepo;

    public SponsorProfileController(SponsorJpaRepository sponsorRepo) {
        this.sponsorRepo = sponsorRepo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<SponsorProfileResponse> getProfile(
            @AuthenticationPrincipal JwtUserDetails principal) {
        UUID sponsorId = requireSponsorId(principal);
        SponsorEntity sponsor = sponsorRepo.findById(sponsorId)
                .orElseThrow(() -> new DomainException("Sponsor not found"));
        return ResponseEntity.ok(toResponse(sponsor));
    }

    @PatchMapping
    @Transactional
    public ResponseEntity<SponsorProfileResponse> updateProfile(
            @RequestBody UpdateSponsorProfileRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        UUID sponsorId = requireSponsorId(principal);
        SponsorEntity sponsor = sponsorRepo.findById(sponsorId)
                .orElseThrow(() -> new DomainException("Sponsor not found"));

        String newDisplayName = request.displayName() != null ? request.displayName() : sponsor.getDisplayName();
        String newPhone = request.phone() != null ? request.phone() : sponsor.getPhone();
        sponsor.updateProfile(newDisplayName, newPhone);
        sponsorRepo.save(sponsor);
        return ResponseEntity.ok(toResponse(sponsor));
    }

    private UUID requireSponsorId(JwtUserDetails principal) {
        UUID sponsorId = principal.getSponsorId();
        if (sponsorId == null) {
            throw new DomainException("No sponsor account linked to this user");
        }
        return sponsorId;
    }

    private SponsorProfileResponse toResponse(SponsorEntity sponsor) {
        return new SponsorProfileResponse(
                sponsor.getId(),
                sponsor.getDisplayName(),
                sponsor.getContactEmail(),
                sponsor.getPhone()
        );
    }

    public record SponsorProfileResponse(
            UUID id,
            String displayName,
            String contactEmail,
            String phone
    ) {}

    public record UpdateSponsorProfileRequest(
            String displayName,
            String phone
    ) {}
}
