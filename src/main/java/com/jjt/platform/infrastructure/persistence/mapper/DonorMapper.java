package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Donor;
import com.jjt.platform.infrastructure.persistence.entity.DonorEntity;

public final class DonorMapper {

    private DonorMapper() {}

    public static Donor toDomain(DonorEntity entity) {
        return new Donor(
                entity.getId(),
                entity.getOrganisationId(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getDonorType(),
                entity.getNotes(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    public static DonorEntity toEntity(Donor donor) {
        return new DonorEntity(
                donor.getId(),
                donor.getOrganisationId(),
                donor.getDisplayName(),
                donor.getEmail(),
                donor.getPhone(),
                donor.getDonorType(),
                donor.getNotes(),
                donor.getCreatedBy(),
                donor.getCreatedAt()
        );
    }
}
