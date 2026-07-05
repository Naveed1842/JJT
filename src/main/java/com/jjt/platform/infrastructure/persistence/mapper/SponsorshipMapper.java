package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;

import java.util.Objects;
import java.util.UUID;

public final class SponsorshipMapper {

    private SponsorshipMapper() {}

    public static Sponsorship toDomain(SponsorshipEntity entity) {
        Objects.requireNonNull(entity, "entity");
        // restore() is used here — not create/createPending — because DB rows may have
        // past start months and must not be re-validated against "future start" invariant.
        return Sponsorship.restore(
                entity.getId(),
                entity.getSponsor().getId(),
                entity.getChildId(),
                YearMonthMapper.toDomain(entity.getStartMonth()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getCommitmentType(),
                entity.getCreatedBy()    // nullable for Phase 1 rows
        );
    }

    public static SponsorshipEntity toEntity(Sponsorship sponsorship, SponsorEntity sponsorEntity, UUID organisationId) {
        Objects.requireNonNull(sponsorship, "sponsorship");
        Objects.requireNonNull(sponsorEntity, "sponsorEntity");
        return new SponsorshipEntity(
                sponsorship.getId(),
                sponsorEntity,
                sponsorship.getChildId(),
                YearMonthMapper.toString(sponsorship.getStartMonth()),
                sponsorship.getStatus(),
                sponsorship.getCreatedAt(),
                sponsorship.getExpiresAt(),
                sponsorship.getCommitmentType(),
                sponsorship.getCreatedBy(),
                organisationId
        );
    }
}
