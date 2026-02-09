package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.entity.CommitmentType;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;

import java.util.Objects;

public final class SponsorshipMapper {

    private SponsorshipMapper() {}

    public static Sponsorship toDomain(SponsorshipEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return Sponsorship.create(
                entity.getId(),
                entity.getSponsor().getId(),
                entity.getChildId(),
                YearMonthMapper.toDomain(entity.getStartMonth()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getCommitmentType()
        );
    }

    public static SponsorshipEntity toEntity(Sponsorship sponsorship, SponsorEntity sponsorEntity) {
        Objects.requireNonNull(sponsorship, "sponsorship");
        Objects.requireNonNull(sponsorEntity, "sponsorEntity");
        return SponsorshipEntity.create(
                sponsorship.getId(),
                sponsorEntity,
                sponsorship.getChildId(),
                YearMonthMapper.toString(sponsorship.getStartMonth()),
                sponsorship.getStatus(),
                sponsorship.getCreatedAt(),
                sponsorship.getExpiresAt(),
                sponsorship.getCommitmentType()
        );
    }
}
