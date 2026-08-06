package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.service.AdminFinancePeriodService;
import com.jjt.platform.api.admin.service.BudgetService;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.infrastructure.persistence.entity.AccountCategoryEntity;
import com.jjt.platform.infrastructure.persistence.entity.BudgetEntity;
import com.jjt.platform.infrastructure.persistence.entity.CostCentreEntity;
import com.jjt.platform.infrastructure.persistence.entity.FinancialPeriodEntity;
import com.jjt.platform.infrastructure.persistence.entity.FinancialTransactionEntity2;
import com.jjt.platform.infrastructure.persistence.repository.AccountCategoryJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.CostCentreJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminFinanceController {

    private final AccountCategoryJpaRepository categoryRepo;
    private final CostCentreJpaRepository costCentreRepo;
    private final AdminFinancePeriodService periodService;
    private final BudgetService budgetService;
    private final FinancialTransactionJpaRepository2 txRepo;

    public AdminFinanceController(AccountCategoryJpaRepository categoryRepo,
                                  CostCentreJpaRepository costCentreRepo,
                                  AdminFinancePeriodService periodService,
                                  BudgetService budgetService,
                                  FinancialTransactionJpaRepository2 txRepo) {
        this.categoryRepo = categoryRepo;
        this.costCentreRepo = costCentreRepo;
        this.periodService = periodService;
        this.budgetService = budgetService;
        this.txRepo = txRepo;
    }

    // ---- Chart of accounts ----

    @GetMapping("/categories")
    public List<CategoryResponse> listCategories(@AuthenticationPrincipal JwtUserDetails principal) {
        return categoryRepo.findByOrgIdIsNullOrOrgId(principal.getOrgId()).stream()
                .map(this::toCategoryResponse).toList();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        AccountCategoryEntity entity = new AccountCategoryEntity(principal.getOrgId(),
                req.code(), req.name(), req.parentId(), req.reportingClass(), false);
        entity = categoryRepo.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCategoryResponse(entity));
    }

    // ---- Cost centres ----

    @GetMapping("/cost-centres")
    public List<CostCentreResponse> listCostCentres(@AuthenticationPrincipal JwtUserDetails principal) {
        return costCentreRepo.findByOrgId(principal.getOrgId()).stream()
                .map(this::toCostCentreResponse).toList();
    }

    @PostMapping("/cost-centres")
    public ResponseEntity<CostCentreResponse> createCostCentre(
            @RequestBody CreateCostCentreRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        CostCentreEntity entity = new CostCentreEntity(UUID.randomUUID(), principal.getOrgId(),
                req.code(), req.name(), req.parentId());
        entity = costCentreRepo.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCostCentreResponse(entity));
    }

    // ---- Periods ----

    @GetMapping("/periods")
    public List<PeriodResponse> listPeriods(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return periodService.list(principal.getOrgId(), status).stream()
                .map(this::toPeriodResponse).toList();
    }

    @PostMapping("/periods")
    public ResponseEntity<PeriodResponse> createPeriod(
            @RequestBody CreatePeriodRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                toPeriodResponse(periodService.create(principal.getOrgId(), req.label(),
                        req.periodType(), req.startDate(), req.endDate())));
    }

    @PostMapping("/periods/{id}/close")
    public PeriodResponse closePeriod(@PathVariable UUID id,
                                      @AuthenticationPrincipal JwtUserDetails principal) {
        return toPeriodResponse(periodService.close(id, principal.getId()));
    }

    @PostMapping("/periods/{id}/lock")
    public PeriodResponse lockPeriod(@PathVariable UUID id) {
        return toPeriodResponse(periodService.lock(id));
    }

    // ---- Budgets ----

    @GetMapping("/budgets")
    public List<BudgetResponse> listBudgets(
            @RequestParam UUID periodId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return budgetService.listForPeriod(principal.getOrgId(), periodId).stream()
                .map(this::toBudgetResponse).toList();
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestBody CreateBudgetRequest req,
            @AuthenticationPrincipal JwtUserDetails principal) {
        BudgetEntity budget = budgetService.createOrUpdate(principal.getOrgId(), req.periodId(),
                req.categoryId(), req.costCentreId(), req.missionNodeId(), req.amount(), req.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toBudgetResponse(budget));
    }

    @GetMapping("/budgets/variance")
    public List<BudgetService.BudgetVarianceRow> variance(
            @RequestParam UUID periodId,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return budgetService.computeVariance(principal.getOrgId(), periodId);
    }

    // ---- Financial transactions journal (read-only) ----

    @GetMapping("/transactions")
    public Page<TxResponse> listTransactions(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal JwtUserDetails principal) {
        return txRepo.findByOrgIdOrderByEffectiveDateDesc(principal.getOrgId(), pageable)
                .map(this::toTxResponse);
    }

    // ---- Mappers ----

    private CategoryResponse toCategoryResponse(AccountCategoryEntity c) {
        return new CategoryResponse(c.getId(), c.getOrgId(), c.getCode(), c.getName(),
                c.getParentId(), c.getReportingClass(), c.isSystem(), c.isActive());
    }

    private CostCentreResponse toCostCentreResponse(CostCentreEntity c) {
        return new CostCentreResponse(c.getId(), c.getOrgId(), c.getCode(), c.getName(),
                c.getParentId(), c.isActive());
    }

    private PeriodResponse toPeriodResponse(FinancialPeriodEntity p) {
        return new PeriodResponse(p.getId(), p.getOrgId(), p.getLabel(), p.getPeriodType(),
                p.getStartDate(), p.getEndDate(), p.getStatus(), p.getClosedAt());
    }

    private BudgetResponse toBudgetResponse(BudgetEntity b) {
        return new BudgetResponse(b.getId(), b.getOrgId(), b.getPeriodId(), b.getCategoryId(),
                b.getCostCentreId(), b.getAmount(), b.getNotes());
    }

    private TxResponse toTxResponse(FinancialTransactionEntity2 t) {
        return new TxResponse(t.getId(), t.getOrgId(), t.getSourceType(), t.getType(),
                t.getAmount(), t.getCurrency(), t.getEffectiveDate(), t.getDescription(), t.getCreatedAt());
    }

    // ---- Inline DTOs ----

    public record CategoryResponse(Integer id, UUID orgId, String code, String name,
                                   Integer parentId, String reportingClass,
                                   boolean system, boolean active) {}
    public record CreateCategoryRequest(@NotBlank String code, @NotBlank String name,
                                        Integer parentId, @NotBlank String reportingClass) {}
    public record CostCentreResponse(UUID id, UUID orgId, String code, String name,
                                     UUID parentId, boolean active) {}
    public record CreateCostCentreRequest(@NotBlank String code, @NotBlank String name, UUID parentId) {}
    public record PeriodResponse(UUID id, UUID orgId, String label, String periodType,
                                 LocalDate startDate, LocalDate endDate, String status, Instant closedAt) {}
    public record CreatePeriodRequest(@NotBlank String label, @NotBlank String periodType,
                                      @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
    public record BudgetResponse(UUID id, UUID orgId, UUID periodId, Integer categoryId,
                                 UUID costCentreId, BigDecimal amount, String notes) {}
    public record CreateBudgetRequest(@NotNull UUID periodId, Integer categoryId, UUID costCentreId,
                                      UUID missionNodeId, @NotNull BigDecimal amount, String notes) {}
    public record TxResponse(UUID id, UUID orgId, String sourceType, String type, BigDecimal amount,
                             String currency, LocalDate effectiveDate, String description, Instant createdAt) {}
}
