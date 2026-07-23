package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.entity.ApprovalEntityType;
import com.jjt.platform.infrastructure.persistence.entity.ApprovalRequestEntity;
import com.jjt.platform.infrastructure.persistence.entity.ExpenseEntity;
import com.jjt.platform.infrastructure.persistence.entity.FinancialTransactionEntity2;
import com.jjt.platform.infrastructure.persistence.entity.VendorEntity;
import com.jjt.platform.infrastructure.persistence.repository.ApprovalRequestJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.ExpenseJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import com.jjt.platform.infrastructure.persistence.repository.VendorJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdminExpenseService {

    private final ExpenseJpaRepository expenseRepo;
    private final VendorJpaRepository vendorRepo;
    private final FinancialTransactionJpaRepository2 txRepo;
    private final ApprovalRequestJpaRepository approvalRepo;
    private final ApprovalEngineService approvalEngine;

    public AdminExpenseService(ExpenseJpaRepository expenseRepo,
                               VendorJpaRepository vendorRepo,
                               FinancialTransactionJpaRepository2 txRepo,
                               ApprovalRequestJpaRepository approvalRepo,
                               ApprovalEngineService approvalEngine) {
        this.expenseRepo = expenseRepo;
        this.vendorRepo = vendorRepo;
        this.txRepo = txRepo;
        this.approvalRepo = approvalRepo;
        this.approvalEngine = approvalEngine;
    }

    @Transactional
    public ExpenseEntity create(UUID orgId, UUID vendorId, String payeeText,
                                Integer categoryId, UUID costCentreId, UUID missionNodeId,
                                UUID periodId, String invoiceRef, BigDecimal amount,
                                String currency, String description, UUID createdBy) {
        UUID resolvedVendorId = vendorId;
        if (resolvedVendorId == null && payeeText != null && !payeeText.isBlank()) {
            resolvedVendorId = quickCreateVendor(orgId, payeeText);
        }
        ExpenseEntity entity = new ExpenseEntity(UUID.randomUUID(), orgId, resolvedVendorId,
                payeeText, categoryId, costCentreId, missionNodeId, periodId, invoiceRef,
                amount, currency != null ? currency : "GBP", description, createdBy);
        return expenseRepo.save(entity);
    }

    @Transactional
    public ApprovalRequestEntity submit(UUID expenseId, UUID submittedBy) {
        ExpenseEntity expense = findExpense(expenseId);
        if (!"DRAFT".equals(expense.getStatus())) {
            throw new DomainException("Only DRAFT expenses can be submitted");
        }
        expense.setStatus("SUBMITTED");
        expenseRepo.save(expense);
        return approvalEngine.createAndRunChecks(expense.getOrgId(),
                ApprovalEntityType.EXPENSE, expenseId, submittedBy);
    }

    @Transactional
    public ExpenseEntity approve(UUID expenseId, UUID approvedBy, String notes) {
        ExpenseEntity expense = findExpense(expenseId);
        if (!"SUBMITTED".equals(expense.getStatus())) {
            throw new DomainException("Only SUBMITTED expenses can be approved");
        }
        approvalRepo.findByEntityTypeAndEntityId("EXPENSE", expenseId)
                .ifPresent(req -> approvalEngine.resolve(req.getId(), approvedBy, true, notes));
        expense.setStatus("APPROVED");
        return expenseRepo.save(expense);
    }

    @Transactional
    public ExpenseEntity reject(UUID expenseId, UUID rejectedBy, String notes) {
        ExpenseEntity expense = findExpense(expenseId);
        if (!"SUBMITTED".equals(expense.getStatus())) {
            throw new DomainException("Only SUBMITTED expenses can be rejected");
        }
        approvalRepo.findByEntityTypeAndEntityId("EXPENSE", expenseId)
                .ifPresent(req -> approvalEngine.resolve(req.getId(), rejectedBy, false, notes));
        expense.setStatus("REJECTED");
        return expenseRepo.save(expense);
    }

    @Transactional
    public ExpenseEntity pay(UUID expenseId, UUID paidBy) {
        ExpenseEntity expense = findExpense(expenseId);
        if (!"APPROVED".equals(expense.getStatus())) {
            throw new DomainException("Only APPROVED expenses can be paid");
        }
        FinancialTransactionEntity2 tx = new FinancialTransactionEntity2(
                UUID.randomUUID(), expense.getOrgId(), null, expense.getCategoryId(),
                expense.getCostCentreId(), expense.getMissionNodeId(), "EXPENSE",
                expense.getId(), null, "EXPENSE", expense.getAmount(), expense.getCurrency(),
                LocalDate.now(), "Payment for expense: " + expense.getDescription(), paidBy);
        tx = txRepo.save(tx);
        expense.setStatus("PAID");
        expense.setPaidAt(LocalDate.now());
        expense.setTxId(tx.getId());
        return expenseRepo.save(expense);
    }

    @Transactional
    public ExpenseEntity voidExpense(UUID expenseId) {
        ExpenseEntity expense = findExpense(expenseId);
        if ("PAID".equals(expense.getStatus())) {
            throw new DomainException("Paid expenses cannot be voided");
        }
        expense.setStatus("VOIDED");
        return expenseRepo.save(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseEntity> list(UUID orgId, String status, UUID periodId, Pageable pageable) {
        if (status != null) return expenseRepo.findByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status, pageable);
        if (periodId != null) return expenseRepo.findByOrgIdAndPeriodIdOrderByCreatedAtDesc(orgId, periodId, pageable);
        return expenseRepo.findByOrgIdOrderByCreatedAtDesc(orgId, pageable);
    }

    @Transactional(readOnly = true)
    public ExpenseEntity get(UUID expenseId) {
        return findExpense(expenseId);
    }

    private ExpenseEntity findExpense(UUID id) {
        return expenseRepo.findById(id)
                .orElseThrow(() -> new DomainException("Expense not found"));
    }

    private UUID quickCreateVendor(UUID orgId, String name) {
        return vendorRepo.findByOrgIdAndNameIgnoreCase(orgId, name)
                .map(v -> v.getId())
                .orElseGet(() -> {
                    VendorEntity v = new VendorEntity(UUID.randomUUID(), orgId, name, null, null, null, null);
                    return vendorRepo.save(v).getId();
                });
    }
}
