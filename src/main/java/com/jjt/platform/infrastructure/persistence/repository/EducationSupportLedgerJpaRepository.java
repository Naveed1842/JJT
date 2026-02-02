package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EducationSupportLedgerJpaRepository extends JpaRepository<EducationSupportLedgerEntity, UUID> {
    Optional<EducationSupportLedgerEntity> findByChild_Id(UUID childId);
}
