package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Organisation;
import com.jjt.platform.infrastructure.persistence.entity.OrganisationEntity;

import java.util.Objects;

public final class OrganisationMapper {

    private OrganisationMapper() {}

    public static Organisation toDomain(OrganisationEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return new Organisation(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getBaseCurrency(),
                entity.getPaymentDueDay(),
                entity.getMinFundReserve()
        );
    }

    public static OrganisationEntity toEntity(Organisation org) {
        Objects.requireNonNull(org, "org");
        return new OrganisationEntity(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getBaseCurrency(),
                org.getPaymentDueDay(),
                org.getMinFundReserve(),
                true,
                java.time.Instant.now()
        );
    }
}
