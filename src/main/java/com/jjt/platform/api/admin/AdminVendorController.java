package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.AdminVendorService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.VendorEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance/vendors")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminVendorController {

    private final AdminVendorService vendorService;

    public AdminVendorController(AdminVendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public List<VendorResponse> list(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return vendorService.list(principal.getOrgId(), active).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public VendorResponse get(@PathVariable UUID id) {
        return toResponse(vendorService.get(id));
    }

    @PostMapping
    public ResponseEntity<VendorResponse> create(
            @RequestBody CreateVendorRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        VendorEntity vendor = vendorService.create(principal.getOrgId(), request.name(),
                request.contactName(), request.email(), request.phone(), request.taxId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(vendor));
    }

    @PutMapping("/{id}")
    public VendorResponse update(
            @PathVariable UUID id,
            @RequestBody UpdateVendorRequest request) {
        return toResponse(vendorService.update(id, request.name(), request.contactName(),
                request.email(), request.phone(), request.active()));
    }

    private VendorResponse toResponse(VendorEntity v) {
        return new VendorResponse(v.getId(), v.getOrgId(), v.getName(), v.getContactName(),
                v.getEmail(), v.getPhone(), v.getTaxId(), v.isActive(), v.getCreatedAt());
    }

    public record CreateVendorRequest(@NotBlank String name, String contactName,
                                      String email, String phone, String taxId) {}
    public record UpdateVendorRequest(String name, String contactName,
                                      String email, String phone, Boolean active) {}
    public record VendorResponse(UUID id, UUID orgId, String name, String contactName,
                                 String email, String phone, String taxId,
                                 boolean active, Instant createdAt) {}
}
