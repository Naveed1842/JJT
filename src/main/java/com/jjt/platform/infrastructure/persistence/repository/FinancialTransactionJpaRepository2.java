package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.FinancialTransactionEntity2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface FinancialTransactionJpaRepository2 extends JpaRepository<FinancialTransactionEntity2, UUID> {

    Page<FinancialTransactionEntity2> findByOrgIdOrderByEffectiveDateDesc(UUID orgId, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE WHEN type = 'INCOME' THEN amount
                     WHEN type IN ('EXPENSE','TRANSFER') THEN -amount
                     ELSE 0 END
            ), 0)
            FROM financial_transactions
            WHERE org_id = :orgId
              AND fund_account_id = :fundAccountId
            """, nativeQuery = true)
    BigDecimal computeBalance(@Param("orgId") UUID orgId,
                              @Param("fundAccountId") UUID fundAccountId);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM financial_transactions
            WHERE org_id = :orgId
              AND type = 'INCOME'
              AND effective_date BETWEEN :start AND :end
            """, nativeQuery = true)
    BigDecimal sumIncomeForPeriod(@Param("orgId") UUID orgId,
                                  @Param("start") java.time.LocalDate start,
                                  @Param("end") java.time.LocalDate end);

    @Query(value = """
            SELECT COALESCE(SUM(ft.amount), 0)
            FROM financial_transactions ft
            JOIN account_categories ac ON ft.category_id = ac.id
            WHERE ft.org_id = :orgId
              AND ft.type IN ('EXPENSE', 'ADJUSTMENT')
              AND ac.reporting_class = :reportingClass
              AND ft.effective_date BETWEEN :start AND :end
            """, nativeQuery = true)
    BigDecimal sumExpenseByReportingClass(@Param("orgId") UUID orgId,
                                          @Param("reportingClass") String reportingClass,
                                          @Param("start") java.time.LocalDate start,
                                          @Param("end") java.time.LocalDate end);
}
