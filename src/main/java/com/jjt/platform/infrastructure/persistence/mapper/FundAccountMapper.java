package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.FundAccount;
import com.jjt.platform.infrastructure.persistence.entity.FundAccountEntity;

import java.util.Objects;

public final class FundAccountMapper {

    private FundAccountMapper() {}

    public static FundAccount toDomain(FundAccountEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return new FundAccount(
                entity.getId(),
                entity.getName(),
                entity.getCurrency(),
                entity.getMinReserve(),
                entity.getOrganisationId(),
                entity.getCreatedAt()
        );
    }

    public static FundAccountEntity toEntity(FundAccount fund, java.util.UUID createdBy) {
        Objects.requireNonNull(fund, "fund");
        return new FundAccountEntity(
                fund.getId(),
                fund.getName(),
                fund.getCurrency(),
                fund.getMinReserve(),
                fund.getOrganisationId(),
                createdBy,
                fund.getCreatedAt() != null ? fund.getCreatedAt() : java.time.Instant.now()
        );
    }
}
