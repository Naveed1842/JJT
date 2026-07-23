package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.ApprovalEntityType;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.FinancialTransactionEntity2;
import com.jjt.platform.infrastructure.persistence.entity.PayrollItemEntity;
import com.jjt.platform.infrastructure.persistence.entity.PayrollProfileEntity;
import com.jjt.platform.infrastructure.persistence.entity.PayrollRunEntity;
import com.jjt.platform.infrastructure.persistence.entity.PersonEntity;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import com.jjt.platform.infrastructure.persistence.repository.PayrollItemJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.PayrollProfileJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.PayrollRunJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.PersonJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PayrollService {

    private final PayrollRunJpaRepository runRepo;
    private final PayrollItemJpaRepository itemRepo;
    private final PersonJpaRepository personRepo;
    private final PayrollProfileJpaRepository profileRepo;
    private final FinancialTransactionJpaRepository2 txRepo;
    private final ApprovalEngineService approvalEngine;

    public PayrollService(PayrollRunJpaRepository runRepo, PayrollItemJpaRepository itemRepo,
                          PersonJpaRepository personRepo, PayrollProfileJpaRepository profileRepo,
                          FinancialTransactionJpaRepository2 txRepo, ApprovalEngineService approvalEngine) {
        this.runRepo = runRepo; this.itemRepo = itemRepo;
        this.personRepo = personRepo; this.profileRepo = profileRepo;
        this.txRepo = txRepo; this.approvalEngine = approvalEngine;
    }

    @Transactional
    public PayrollRunEntity createRun(UUID orgId, UUID periodId, UUID createdBy) {
        PayrollRunEntity run = new PayrollRunEntity(UUID.randomUUID(), orgId, periodId,
                LocalDate.now(), createdBy);
        runRepo.save(run);

        List<PersonEntity> active = personRepo.findByOrgIdAndActiveOrderByLastNameAsc(orgId, true);
        BigDecimal totalGross = BigDecimal.ZERO;
        for (PersonEntity person : active) {
            PayrollProfileEntity profile = profileRepo.findByPersonId(person.getId()).orElse(null);
            if (profile == null || !profile.isActive()) continue;
            BigDecimal gross = profile.getAmount();
            BigDecimal deductions = BigDecimal.ZERO;
            BigDecimal net = gross.subtract(deductions);
            itemRepo.save(new PayrollItemEntity(UUID.randomUUID(), run.getId(),
                    person.getId(), gross, deductions, net));
            totalGross = totalGross.add(gross);
        }
        run.setTotalGross(totalGross);
        return runRepo.save(run);
    }

    @Transactional
    public void approve(UUID runId, UUID approvedBy) {
        PayrollRunEntity run = findRun(runId);
        if (!"DRAFT".equals(run.getStatus())) throw new DomainException("Run is not in DRAFT");
        approvalEngine.createAndRunChecks(run.getOrgId(), ApprovalEntityType.PAYROLL_RUN,
                runId, approvedBy);
        run.setStatus("APPROVED");
        runRepo.save(run);
    }

    @Transactional
    public PayrollRunEntity process(UUID runId, UUID processedBy) {
        PayrollRunEntity run = findRun(runId);
        if (!"APPROVED".equals(run.getStatus())) throw new DomainException("Run must be APPROVED first");
        List<PayrollItemEntity> items = itemRepo.findByRunId(runId);
        for (PayrollItemEntity item : items) {
            FinancialTransactionEntity2 tx = new FinancialTransactionEntity2(
                    UUID.randomUUID(), run.getOrgId(), null, null, null, null,
                    "PAYROLL_ITEM", item.getId(), null, "EXPENSE",
                    item.getNetAmount(), "GBP", run.getRunDate(),
                    "Payroll payment for person " + item.getPersonId(), processedBy);
            tx = txRepo.save(tx);
            item.setTxId(tx.getId());
            itemRepo.save(item);
        }
        run.setStatus("PAID");
        return runRepo.save(run);
    }

    @Transactional(readOnly = true)
    public List<PayrollRunEntity> listRuns(UUID orgId, String status) {
        if (status != null) return runRepo.findByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status);
        return runRepo.findByOrgIdOrderByCreatedAtDesc(orgId);
    }

    @Transactional(readOnly = true)
    public List<PayrollItemEntity> listItems(UUID runId) {
        return itemRepo.findByRunId(runId);
    }

    private PayrollRunEntity findRun(UUID runId) {
        return runRepo.findById(runId)
                .orElseThrow(() -> new DomainException("Payroll run not found"));
    }
}
