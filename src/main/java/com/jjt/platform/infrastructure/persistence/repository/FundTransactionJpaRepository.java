package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.FundTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface FundTransactionJpaRepository extends JpaRepository<FundTransactionEntity, UUID> {

    Page<FundTransactionEntity> findByFundAccountIdOrderByCreatedAtDesc(UUID fundAccountId, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE -amount END
            ), 0.00)
            FROM fund_transactions
            WHERE fund_account_id = :fundAccountId
            """, nativeQuery = true)
    BigDecimal computeBalance(@Param("fundAccountId") UUID fundAccountId);

    @Query(value = """
            SELECT COALESCE(SUM(ft.amount), 0)
            FROM fund_transactions ft
            JOIN fund_accounts fa ON ft.fund_account_id = fa.id
            WHERE fa.organisation_id = :orgId
              AND ft.transaction_type = :type
              AND TO_CHAR(ft.created_at AT TIME ZONE 'UTC', 'YYYY-MM') = :yearMonth
            """, nativeQuery = true)
    BigDecimal sumByOrgAndMonthAndType(@Param("orgId") UUID orgId,
                                       @Param("yearMonth") String yearMonth,
                                       @Param("type") String type);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE WHEN ft.transaction_type = 'CREDIT' THEN ft.amount ELSE -ft.amount END
            ), 0)
            FROM fund_transactions ft
            JOIN fund_accounts fa ON ft.fund_account_id = fa.id
            WHERE fa.organisation_id = :orgId
              AND ft.created_at < :before
            """, nativeQuery = true)
    BigDecimal computeOrgBalanceBefore(@Param("orgId") UUID orgId,
                                       @Param("before") Instant before);
}
