package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.OrgConfigResponse;
import com.jjt.platform.api.admin.dto.UpdateOrgConfigRequest;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.OrganisationEntity;
import com.jjt.platform.infrastructure.persistence.repository.OrganisationJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/org")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminOrgController {

    private final OrganisationJpaRepository orgRepo;

    public AdminOrgController(OrganisationJpaRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @GetMapping("/config")
    @Transactional(readOnly = true)
    public ResponseEntity<OrgConfigResponse> getConfig(@AuthenticationPrincipal JwtUserDetails principal) {
        if (principal.getOrgId() == null) {
            return ResponseEntity.status(403).build();
        }
        return orgRepo.findById(principal.getOrgId())
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/config")
    @PreAuthorize("hasRole('JJT_ADMIN')")
    @Transactional
    public ResponseEntity<OrgConfigResponse> updateConfig(
            @Valid @RequestBody UpdateOrgConfigRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        if (principal.getOrgId() == null) {
            return ResponseEntity.status(403).build();
        }
        OrganisationEntity org = orgRepo.findById(principal.getOrgId())
                .orElseThrow(() -> new DomainException("Organisation not found"));
        if (request.name() != null) org.setName(request.name());
        if (request.baseCurrency() != null) org.setBaseCurrency(request.baseCurrency());
        if (request.paymentDueDay() != null) org.setPaymentDueDay(request.paymentDueDay());
        if (request.minFundReserve() != null) org.setMinFundReserve(request.minFundReserve());
        orgRepo.save(org);
        return ResponseEntity.ok(toResponse(org));
    }

    private OrgConfigResponse toResponse(OrganisationEntity org) {
        return new OrgConfigResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getBaseCurrency(),
                org.getPaymentDueDay(),
                org.getMinFundReserve(),
                org.isActive(),
                org.getCreatedAt()
        );
    }
}
