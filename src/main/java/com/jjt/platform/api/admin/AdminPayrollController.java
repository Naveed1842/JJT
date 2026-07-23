package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.PayrollService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.PayrollItemEntity;
import com.jjt.platform.infrastructure.persistence.entity.PayrollRunEntity;
import jakarta.validation.constraints.NotNull;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance/payroll/runs")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminPayrollController {

    private final PayrollService payrollService;

    public AdminPayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public List<RunResponse> listRuns(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return payrollService.listRuns(principal.getOrgId(), status).stream()
                .map(this::toRunResponse).toList();
    }

    @PostMapping
    public ResponseEntity<RunResponse> createRun(
            @RequestBody CreateRunRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        PayrollRunEntity run = payrollService.createRun(
                principal.getOrgId(), request.periodId(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toRunResponse(run));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        payrollService.approve(id, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/process")
    public RunResponse process(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return toRunResponse(payrollService.process(id, principal.getId()));
    }

    @GetMapping("/{id}/items")
    public List<ItemResponse> listItems(@PathVariable UUID id) {
        return payrollService.listItems(id).stream().map(this::toItemResponse).toList();
    }

    private RunResponse toRunResponse(PayrollRunEntity r) {
        return new RunResponse(r.getId(), r.getOrgId(), r.getPeriodId(), r.getRunDate(),
                r.getStatus(), r.getTotalGross(), r.getCreatedAt());
    }

    private ItemResponse toItemResponse(PayrollItemEntity i) {
        return new ItemResponse(i.getId(), i.getRunId(), i.getPersonId(),
                i.getGrossAmount(), i.getDeductions(), i.getNetAmount(), i.getTxId());
    }

    public record CreateRunRequest(@NotNull UUID periodId) {}
    public record RunResponse(UUID id, UUID orgId, UUID periodId, LocalDate runDate,
                              String status, BigDecimal totalGross, Instant createdAt) {}
    public record ItemResponse(UUID id, UUID runId, UUID personId, BigDecimal grossAmount,
                               BigDecimal deductions, BigDecimal netAmount, UUID txId) {}
}
