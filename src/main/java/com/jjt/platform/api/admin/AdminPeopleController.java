package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.AdminPeopleService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.PayrollProfileEntity;
import com.jjt.platform.infrastructure.persistence.entity.PersonEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/people")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminPeopleController {

    private final AdminPeopleService peopleService;

    public AdminPeopleController(AdminPeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    public List<PersonResponse> list(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return peopleService.list(principal.getOrgId(), active).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PersonResponse get(@PathVariable UUID id) {
        return toResponse(peopleService.get(id));
    }

    @PostMapping
    public ResponseEntity<PersonResponse> create(
            @RequestBody CreatePersonRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        PersonEntity person = peopleService.create(principal.getOrgId(), request.kind(),
                request.firstName(), request.lastName(), request.email(), request.phone());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(person));
    }

    @PutMapping("/{id}")
    public PersonResponse update(@PathVariable UUID id, @RequestBody UpdatePersonRequest request) {
        return toResponse(peopleService.update(id, request.firstName(), request.lastName(),
                request.email(), request.phone(), request.active()));
    }

    @GetMapping("/{id}/payroll-profile")
    public ResponseEntity<PayrollProfileResponse> getPayrollProfile(@PathVariable UUID id) {
        return peopleService.getPayrollProfile(id)
                .map(p -> ResponseEntity.ok(toProfileResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/payroll-profile")
    public PayrollProfileResponse upsertPayrollProfile(
            @PathVariable UUID id,
            @RequestBody PayrollProfileRequest request) {
        return toProfileResponse(peopleService.upsertPayrollProfile(id, request.salaryType(),
                request.amount(), request.currency(), request.costCentreId(), request.paymentMethod()));
    }

    private PersonResponse toResponse(PersonEntity p) {
        return new PersonResponse(p.getId(), p.getOrgId(), p.getKind(),
                p.getFirstName(), p.getLastName(), p.getEmail(), p.getPhone(),
                p.isActive(), p.getCreatedAt());
    }

    private PayrollProfileResponse toProfileResponse(PayrollProfileEntity p) {
        return new PayrollProfileResponse(p.getId(), p.getPersonId(), p.getSalaryType(),
                p.getAmount(), p.getCurrency(), p.getCostCentreId(), p.getPaymentMethod(), p.isActive());
    }

    public record CreatePersonRequest(@NotBlank String kind, @NotBlank String firstName,
                                      @NotBlank String lastName, String email, String phone) {}
    public record UpdatePersonRequest(String firstName, String lastName,
                                      String email, String phone, Boolean active) {}
    public record PayrollProfileRequest(@NotBlank String salaryType, @NotNull BigDecimal amount,
                                        String currency, UUID costCentreId, String paymentMethod) {}
    public record PersonResponse(UUID id, UUID orgId, String kind, String firstName, String lastName,
                                 String email, String phone, boolean active, Instant createdAt) {}
    public record PayrollProfileResponse(UUID id, UUID personId, String salaryType, BigDecimal amount,
                                         String currency, UUID costCentreId, String paymentMethod, boolean active) {}
}
