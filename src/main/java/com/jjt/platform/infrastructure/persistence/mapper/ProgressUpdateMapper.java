package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;

import java.util.Objects;

public final class ProgressUpdateMapper {

    private ProgressUpdateMapper() {}

    public static ProgressUpdate toDomain(ProgressUpdateEntity entity, EducationSupportLedger ledger) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(ledger, "ledger");
        YearMonthValue month = YearMonthMapper.toDomain(entity.getUpdateMonth());
        return ProgressUpdate.create(
                entity.getId(),
                entity.getChildId(),
                month,
                entity.getSummary(),
                ledger,
                entity.getCreatedBy(),   // nullable for Phase 1 rows
                entity.getCreatedAt()
        );
    }

    public static ProgressUpdateEntity toEntity(ProgressUpdate progressUpdate) {
        Objects.requireNonNull(progressUpdate, "progressUpdate");
        return new ProgressUpdateEntity(
                progressUpdate.getId(),
                progressUpdate.getChildId(),
                YearMonthMapper.toString(progressUpdate.getMonth()),
                progressUpdate.getSummary(),
                progressUpdate.getCreatedBy(),
                progressUpdate.getCreatedAt()
        );
    }
}
