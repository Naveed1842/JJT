package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.FundTransaction;
import com.jjt.platform.infrastructure.persistence.entity.FundTransactionEntity;

import java.util.Objects;

public final class FundTransactionMapper {

    private FundTransactionMapper() {}

    public static FundTransaction toDomain(FundTransactionEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return new FundTransaction(
                entity.getId(),
                entity.getFundAccountId(),
                entity.getTransactionType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getReason(),
                entity.getDescription(),
                entity.getExternalReference(),
                entity.getLedgerEntryId(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    public static FundTransactionEntity toEntity(FundTransaction txn) {
        Objects.requireNonNull(txn, "txn");
        return new FundTransactionEntity(
                txn.getId(),
                txn.getFundAccountId(),
                txn.getTransactionType(),
                txn.getAmount(),
                txn.getCurrency(),
                txn.getReason(),
                txn.getDescription(),
                txn.getExternalReference(),
                txn.getLedgerEntryId(),
                txn.getCreatedBy(),
                txn.getCreatedAt()
        );
    }
}
