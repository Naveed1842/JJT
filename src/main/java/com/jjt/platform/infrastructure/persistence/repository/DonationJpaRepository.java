package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.core.domain.entity.DonationStatus;
import com.jjt.platform.core.domain.entity.DonationType;
import com.jjt.platform.infrastructure.persistence.entity.DonationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DonationJpaRepository extends JpaRepository<DonationEntity, UUID> {

    Page<DonationEntity> findByOrganisationId(UUID organisationId, Pageable pageable);

    Page<DonationEntity> findByOrganisationIdAndDonationType(
            UUID organisationId, DonationType donationType, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DonationEntity d " +
           "WHERE d.organisationId = :orgId AND d.donationType = :type AND d.status = 'RECEIPTED'")
    BigDecimal sumReceiptedByType(@Param("orgId") UUID orgId, @Param("type") DonationType type);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DonationEntity d " +
           "WHERE d.organisationId = :orgId AND d.donationType = :type AND d.status = 'RECEIPTED' " +
           "AND d.donationDate >= :from")
    BigDecimal sumReceiptedByTypeSince(@Param("orgId") UUID orgId, @Param("type") DonationType type,
                                       @Param("from") LocalDate from);

    long countByOrganisationIdAndDonationTypeAndStatus(
            UUID organisationId, DonationType donationType, DonationStatus status);

    List<DonationEntity> findByOrganisationIdAndDonorId(UUID organisationId, UUID donorId);

    List<DonationEntity> findByOrganisationIdAndStatus(UUID organisationId, DonationStatus status);

    List<DonationEntity> findByRecurringScheduleIdAndStatus(UUID recurringScheduleId, DonationStatus status);
}
