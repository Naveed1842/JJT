package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.AdminAlert;
import com.jjt.platform.core.domain.entity.AlertSeverity;
import com.jjt.platform.core.domain.entity.AlertType;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.AdminAlertEntity;
import com.jjt.platform.infrastructure.persistence.repository.AdminAlertJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminAlertService {

    private final AdminAlertJpaRepository alertRepo;

    public AdminAlertService(AdminAlertJpaRepository alertRepo) {
        this.alertRepo = alertRepo;
    }

    /**
     * Creates an alert. Runs in a new transaction so alert creation never rolls back
     * the caller's business transaction on failure.
     * Idempotent: skips if an undismissed alert of the same type already exists
     * for the same related entity.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminAlert raise(UUID orgId, AlertType type, AlertSeverity severity,
                            String title, String message,
                            UUID relatedEntityId, String relatedEntityType) {
        if (relatedEntityId != null && alertRepo
                .existsByOrganisationIdAndAlertTypeAndRelatedEntityIdAndDismissedAtIsNull(
                        orgId, type, relatedEntityId)) {
            return null;
        }
        AdminAlert alert = AdminAlert.createNew(orgId, type, severity, title, message,
                relatedEntityId, relatedEntityType);
        AdminAlertEntity entity = new AdminAlertEntity(
                alert.getId(), alert.getOrganisationId(), alert.getAlertType(),
                alert.getSeverity(), alert.getTitle(), alert.getMessage(),
                alert.getRelatedEntityId(), alert.getRelatedEntityType(), alert.getCreatedAt());
        alertRepo.save(entity);
        return alert;
    }

    @Transactional(readOnly = true)
    public List<AdminAlert> listActive(UUID orgId) {
        return alertRepo.findActiveByOrganisationId(orgId)
                .stream().map(this::toDomain).toList();
    }

    @Transactional
    public AdminAlert dismiss(UUID alertId, UUID orgId, UUID dismissedBy) {
        AdminAlertEntity entity = alertRepo.findById(alertId)
                .orElseThrow(() -> new DomainException("Alert not found"));
        if (!entity.getOrganisationId().equals(orgId)) {
            throw new DomainException("Alert not found");
        }
        entity.dismiss(dismissedBy);
        alertRepo.save(entity);
        return toDomain(entity);
    }

    private AdminAlert toDomain(AdminAlertEntity e) {
        return new AdminAlert(e.getId(), e.getOrganisationId(), e.getAlertType(),
                e.getSeverity(), e.getTitle(), e.getMessage(),
                e.getRelatedEntityId(), e.getRelatedEntityType(),
                e.getDismissedAt(), e.getDismissedBy(), e.getCreatedAt());
    }
}
