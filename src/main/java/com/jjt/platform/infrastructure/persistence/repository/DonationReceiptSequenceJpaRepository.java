package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.DonationReceiptSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DonationReceiptSequenceJpaRepository
        extends JpaRepository<DonationReceiptSequenceEntity, DonationReceiptSequenceEntity.SequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DonationReceiptSequenceEntity s WHERE s.id.organisationId = :orgId AND s.id.year = :year")
    Optional<DonationReceiptSequenceEntity> findForUpdate(
            @Param("orgId") UUID orgId,
            @Param("year") int year);
}
