package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;

import java.util.Objects;
import java.util.UUID;

public final class EducationSupportLedgerMapper {

    private EducationSupportLedgerMapper() {}

    public static EducationSupportLedger toDomain(EducationSupportLedgerEntity entity) {
        Objects.requireNonNull(entity, "entity");
        EducationSupportLedger ledger = EducationSupportLedger.create(entity.getId(), entity.getChild().getId());
        for (LedgerEntryEntity entryEntity : entity.getEntries()) {
            LedgerEntry entry = LedgerEntryMapper.toDomain(entryEntity);
            ledger = ledger.appendEntry(entry);
        }
        return ledger;
    }

    public static EducationSupportLedgerEntity toEntity(EducationSupportLedger ledger, ChildEntity childEntity, UUID organisationId) {
        Objects.requireNonNull(ledger, "ledger");
        Objects.requireNonNull(childEntity, "childEntity");
        EducationSupportLedgerEntity entity = new EducationSupportLedgerEntity(ledger.getId(), childEntity, organisationId);
        ledger.getEntriesByMonth().values().forEach(entry -> entity.getEntries().add(LedgerEntryMapper.toEntity(entry, entity, organisationId)));
        return entity;
    }
}
