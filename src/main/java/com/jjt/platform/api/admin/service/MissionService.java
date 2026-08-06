package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.MissionNodeEntity;
import com.jjt.platform.infrastructure.persistence.repository.FinancialTransactionJpaRepository2;
import com.jjt.platform.infrastructure.persistence.repository.MissionNodeJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MissionService {

    private final MissionNodeJpaRepository missionRepo;
    private final FinancialTransactionJpaRepository2 txRepo;

    public MissionService(MissionNodeJpaRepository missionRepo,
                          FinancialTransactionJpaRepository2 txRepo) {
        this.missionRepo = missionRepo;
        this.txRepo = txRepo;
    }

    @Transactional(readOnly = true)
    public List<MissionNodeEntity> listRoots(UUID orgId) {
        return missionRepo.findByOrgIdAndParentIdIsNullOrderByCreatedAtDesc(orgId);
    }

    @Transactional(readOnly = true)
    public List<MissionNodeEntity> listChildren(UUID parentId) {
        return missionRepo.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public MissionNodeEntity get(UUID nodeId) {
        return missionRepo.findById(nodeId)
                .orElseThrow(() -> new DomainException("Mission node not found"));
    }

    @Transactional
    public MissionNodeEntity create(UUID orgId, UUID parentId, String kind,
                                    String name, String description,
                                    LocalDate startDate, LocalDate endDate,
                                    BigDecimal targetAmount) {
        if (parentId != null && !missionRepo.existsById(parentId)) {
            throw new DomainException("Parent mission node not found");
        }
        MissionNodeEntity entity = new MissionNodeEntity(UUID.randomUUID(), orgId, parentId,
                kind, name, description, startDate, endDate, targetAmount);
        return missionRepo.save(entity);
    }

    @Transactional
    public MissionNodeEntity update(UUID nodeId, String name, String description,
                                    String status, LocalDate startDate, LocalDate endDate,
                                    BigDecimal targetAmount) {
        MissionNodeEntity entity = get(nodeId);
        if (name != null) entity.setName(name);
        if (description != null) entity.setDescription(description);
        if (status != null) entity.setStatus(status);
        if (startDate != null) entity.setStartDate(startDate);
        if (endDate != null) entity.setEndDate(endDate);
        if (targetAmount != null) entity.setTargetAmount(targetAmount);
        return missionRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public MissionFinancials getFinancials(UUID nodeId, LocalDate start, LocalDate end) {
        MissionNodeEntity root = get(nodeId);
        List<UUID> nodeIds = missionRepo.findAllDescendantIds(nodeId);
        // Aggregate PROGRAMME-class spend across all descendant nodes
        BigDecimal totalSpend = nodeIds.isEmpty() ? BigDecimal.ZERO
                : txRepo.sumExpenseByReportingClass(root.getOrgId(), "PROGRAMME", start, end);
        return new MissionFinancials(nodeId, totalSpend);
    }

    public record MissionFinancials(UUID nodeId, BigDecimal totalSpend) {}
}
