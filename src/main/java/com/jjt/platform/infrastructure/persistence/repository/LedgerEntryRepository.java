package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends CrudRepository<LedgerEntryEntity, UUID> {
    List<LedgerEntryEntity> findByLedger_IdOrderByEntryMonth(UUID ledgerId);
}
