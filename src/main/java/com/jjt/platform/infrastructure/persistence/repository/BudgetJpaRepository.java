package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, UUID> {
    List<BudgetEntity> findByOrgIdAndPeriodId(UUID orgId, UUID periodId);

    Optional<BudgetEntity> findByOrgIdAndPeriodIdAndCategoryId(UUID orgId, UUID periodId, Integer categoryId);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM budgets
            WHERE org_id = :orgId
              AND period_id = :periodId
              AND category_id = :categoryId
            """, nativeQuery = true)
    BigDecimal sumBudgetByCategoryAndPeriod(@Param("orgId") UUID orgId,
                                            @Param("periodId") UUID periodId,
                                            @Param("categoryId") Integer categoryId);
}
