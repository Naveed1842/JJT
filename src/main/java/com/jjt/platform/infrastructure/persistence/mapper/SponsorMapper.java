package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Sponsor;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;

import java.util.Objects;

public final class SponsorMapper {

    private SponsorMapper() {}

    public static Sponsor toDomain(SponsorEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return new Sponsor(entity.getId(), entity.getDisplayName(), entity.getContactEmail(), entity.getPhone());
    }

    public static SponsorEntity toEntity(Sponsor sponsor) {
        Objects.requireNonNull(sponsor, "sponsor");
        return new SponsorEntity(sponsor.getId(), sponsor.getDisplayName(), sponsor.getContactEmail(), sponsor.getPhone());
    }
}
