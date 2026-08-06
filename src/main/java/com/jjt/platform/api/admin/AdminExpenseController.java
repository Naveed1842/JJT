package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.ExpenseRequest;
import com.jjt.platform.api.admin.dto.ExpenseResponse;
import com.jjt.platform.api.admin.service.AdminExpenseService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.audit.AuditService;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import com.jjt.platform.infrastructure.persistence.entity.ExpenseEntity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance/expenses")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminExpenseController {

    private final AdminExpenseService expenseService;
    private final AuditService auditService;

    public AdminExpenseController(AdminExpenseService expenseService, AuditService auditService) {
        this.expenseService = expenseService;
        this.auditService = auditService;
    }

    @GetMapping
    public Page<ExpenseResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID periodId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return expenseService.list(principal.getOrgId(), status, periodId, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable UUID id) {
        return toResponse(expenseService.get(id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        ExpenseEntity expense = expenseService.create(
                principal.getOrgId(), request.vendorId(), request.payeeText(),
                request.categoryId(), request.costCentreId(), request.missionNodeId(),
                request.periodId(), request.invoiceRef(), request.amount(),
                request.currency(), request.description(), principal.getId());
        auditService.log(principal.getOrgId(), "EXPENSE_CREATED", principal.getId(),
                principal.getUsername(), "Expense", expense.getId(), "Created expense: " + request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(expense));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, String>> submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        ApprovalRequestEntity approval = expenseService.submit(id, principal.getId());
        auditService.log(principal.getOrgId(), "EXPENSE_SUBMITTED", principal.getId(),
                principal.getUsername(), "Expense", id, "Submitted for approval");
        return ResponseEntity.ok(Map.of("approvalId", approval.getId().toString(),
                "recommendation", approval.getStatus()));
    }

    @PostMapping("/{id}/approve")
    public ExpenseResponse approve(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal JwtUserDetails principal) {
        String notes = body != null ? body.get("notes") : null;
        ExpenseEntity expense = expenseService.approve(id, principal.getId(), notes);
        auditService.log(principal.getOrgId(), "EXPENSE_APPROVED", principal.getId(),
                principal.getUsername(), "Expense", id, "Approved");
        return toResponse(expense);
    }

    @PostMapping("/{id}/reject")
    public ExpenseResponse reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal JwtUserDetails principal) {
        String notes = body != null ? body.get("notes") : null;
        ExpenseEntity expense = expenseService.reject(id, principal.getId(), notes);
        auditService.log(principal.getOrgId(), "EXPENSE_REJECTED", principal.getId(),
                principal.getUsername(), "Expense", id, "Rejected: " + notes);
        return toResponse(expense);
    }

    @PostMapping("/{id}/pay")
    public ExpenseResponse pay(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        ExpenseEntity expense = expenseService.pay(id, principal.getId());
        auditService.log(principal.getOrgId(), "EXPENSE_PAID", principal.getId(),
                principal.getUsername(), "Expense", id, "Marked as paid");
        return toResponse(expense);
    }

    @PostMapping("/{id}/void")
    public ExpenseResponse voidExpense(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {
        ExpenseEntity expense = expenseService.voidExpense(id);
        auditService.log(principal.getOrgId(), "EXPENSE_VOIDED", principal.getId(),
                principal.getUsername(), "Expense", id, "Voided");
        return toResponse(expense);
    }

    private ExpenseResponse toResponse(ExpenseEntity e) {
        return new ExpenseResponse(e.getId(), e.getOrgId(), e.getVendorId(), e.getPayeeText(),
                e.getCategoryId(), e.getCostCentreId(), e.getPeriodId(), e.getInvoiceRef(),
                e.getAmount(), e.getCurrency(), e.getDescription(), e.getStatus(),
                e.getPaidAt(), e.getTxId(), e.getCreatedAt());
    }
}
