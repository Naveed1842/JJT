package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.core.domain.entity.AlertType;
import com.jjt.platform.infrastructure.persistence.entity.AdminAlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AdminAlertJpaRepository extends JpaRepository<AdminAlertEntity, UUID> {

    @Query("SELECT a FROM AdminAlertEntity a WHERE a.organisationId = :orgId AND a.dismissedAt IS NULL ORDER BY a.createdAt DESC")
    List<AdminAlertEntity> findActiveByOrganisationId(@Param("orgId") UUID orgId);

    Page<AdminAlertEntity> findByOrganisationIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);

    boolean existsByOrganisationIdAndAlertTypeAndRelatedEntityIdAndDismissedAtIsNull(
            UUID organisationId, AlertType alertType, UUID relatedEntityId);
}
