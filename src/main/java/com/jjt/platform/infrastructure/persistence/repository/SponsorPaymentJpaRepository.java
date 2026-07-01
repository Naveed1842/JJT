package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.core.domain.entity.SponsorPaymentStatus;
import com.jjt.platform.infrastructure.persistence.entity.SponsorPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SponsorPaymentJpaRepository extends JpaRepository<SponsorPaymentEntity, UUID> {

    boolean existsBySponsorshipIdAndPaymentMonth(UUID sponsorshipId, String paymentMonth);

    Optional<SponsorPaymentEntity> findBySponsorshipIdAndPaymentMonth(UUID sponsorshipId, String paymentMonth);

    List<SponsorPaymentEntity> findByPaymentMonthAndStatus(String paymentMonth, SponsorPaymentStatus status);

    List<SponsorPaymentEntity> findByPaymentMonth(String paymentMonth);

    List<SponsorPaymentEntity> findByOrganisationIdAndPaymentMonth(UUID organisationId, String paymentMonth);

    List<SponsorPaymentEntity> findBySponsorshipIdOrderByPaymentMonthDesc(UUID sponsorshipId);

    @Query("SELECT sp FROM SponsorPaymentEntity sp WHERE sp.status = 'EXPECTED' AND sp.paymentMonth <= :cutoffMonth")
    List<SponsorPaymentEntity> findExpectedBefore(@Param("cutoffMonth") String cutoffMonth);

    List<SponsorPaymentEntity> findBySponsorshipIdAndStatus(UUID sponsorshipId, SponsorPaymentStatus status);
}
