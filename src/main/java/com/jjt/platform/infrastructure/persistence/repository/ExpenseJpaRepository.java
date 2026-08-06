package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.ExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {
    Page<ExpenseEntity> findByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);
    Page<ExpenseEntity> findByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, String status, Pageable pageable);
    Page<ExpenseEntity> findByOrgIdAndPeriodIdOrderByCreatedAtDesc(UUID orgId, UUID periodId, Pageable pageable);
    List<ExpenseEntity> findByOrgIdAndStatusIn(UUID orgId, List<String> statuses);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM expenses
            WHERE org_id = :orgId
              AND category_id = :categoryId
              AND period_id = :periodId
              AND status NOT IN ('DRAFT', 'REJECTED', 'VOIDED')
            """, nativeQuery = true)
    BigDecimal sumApprovedByOrgCategoryPeriod(@Param("orgId") UUID orgId,
                                              @Param("categoryId") Integer categoryId,
                                              @Param("periodId") UUID periodId);

    @Query(value = """
            SELECT COUNT(*) FROM expenses
            WHERE org_id = :orgId
              AND vendor_id = :vendorId
              AND amount = :amount
              AND created_at BETWEEN :from AND :to
              AND id != :excludeId
            """, nativeQuery = true)
    long countDuplicateCandidates(@Param("orgId") UUID orgId,
                                  @Param("vendorId") UUID vendorId,
                                  @Param("amount") BigDecimal amount,
                                  @Param("from") Instant from,
                                  @Param("to") Instant to,
                                  @Param("excludeId") UUID excludeId);
}
