package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.FundTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
}
