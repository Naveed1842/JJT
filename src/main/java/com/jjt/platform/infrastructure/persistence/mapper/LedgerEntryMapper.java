package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.CoverageType;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;

import java.util.Objects;

public final class LedgerEntryMapper {

    private LedgerEntryMapper() {}

    public static LedgerEntry toDomain(LedgerEntryEntity entity) {
        Objects.requireNonNull(entity, "entity");
        CoverageType coverageType = entity.getCoverageType() != null ? entity.getCoverageType() : CoverageType.EARLY_SUPPORT;
        return new LedgerEntry(
                entity.getId(),
                entity.getChildId(),
                YearMonthMapper.toDomain(entity.getEntryMonth()),
                MoneyMapper.toDomain(entity.getEducationAmount(), entity.getEducationCurrency()),
                coverageType
        );
    }

    public static LedgerEntryEntity toEntity(LedgerEntry entry, EducationSupportLedgerEntity ledgerEntity) {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(ledgerEntity, "ledgerEntity");
        return new LedgerEntryEntity(
                entry.getId(),
                ledgerEntity,
                entry.getChildId(),
                YearMonthMapper.toString(entry.getMonth()),
                MoneyMapper.amount(entry.getEducationCost()),
                MoneyMapper.currency(entry.getEducationCost()),
                entry.getCoverageType()
        );
    }
}
