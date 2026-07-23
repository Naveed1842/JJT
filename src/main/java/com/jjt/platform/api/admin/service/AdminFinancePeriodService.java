package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.FinancialPeriodEntity;
import com.jjt.platform.infrastructure.persistence.repository.ExpenseJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FinancialPeriodJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminFinancePeriodService {

    private final FinancialPeriodJpaRepository periodRepo;
    private final ExpenseJpaRepository expenseRepo;

    public AdminFinancePeriodService(FinancialPeriodJpaRepository periodRepo,
                                     ExpenseJpaRepository expenseRepo) {
        this.periodRepo = periodRepo;
        this.expenseRepo = expenseRepo;
    }

    @Transactional(readOnly = true)
    public List<FinancialPeriodEntity> list(UUID orgId, String status) {
        if (status != null) return periodRepo.findByOrgIdAndStatusOrderByStartDateDesc(orgId, status);
        return periodRepo.findByOrgIdOrderByStartDateDesc(orgId);
    }

    @Transactional(readOnly = true)
    public FinancialPeriodEntity get(UUID periodId) {
        return periodRepo.findById(periodId)
                .orElseThrow(() -> new DomainException("Period not found"));
    }

    @Transactional
    public FinancialPeriodEntity create(UUID orgId, String label, String periodType,
                                        LocalDate startDate, LocalDate endDate) {
        if (periodRepo.findByOrgIdAndLabel(orgId, label).isPresent()) {
            throw new DomainException("A period with label '" + label + "' already exists");
        }
        return periodRepo.save(new FinancialPeriodEntity(UUID.randomUUID(), orgId, label,
                periodType, startDate, endDate));
    }

    @Transactional
    public FinancialPeriodEntity close(UUID periodId, UUID closedBy) {
        FinancialPeriodEntity period = get(periodId);
        if (!"OPEN".equals(period.getStatus())) {
            throw new DomainException("Period is not OPEN");
        }
        List<String> openStatuses = List.of("SUBMITTED");
        if (!expenseRepo.findByOrgIdAndStatusIn(period.getOrgId(), openStatuses).isEmpty()) {
            throw new DomainException("Cannot close period while expenses are in SUBMITTED state");
        }
        period.setStatus("CLOSED");
        period.setClosedAt(Instant.now());
        period.setClosedBy(closedBy);
        return periodRepo.save(period);
    }

    @Transactional
    public FinancialPeriodEntity lock(UUID periodId) {
        FinancialPeriodEntity period = get(periodId);
        if (!"CLOSED".equals(period.getStatus())) {
            throw new DomainException("Period must be CLOSED before locking");
        }
        period.setStatus("LOCKED");
        return periodRepo.save(period);
    }
}
