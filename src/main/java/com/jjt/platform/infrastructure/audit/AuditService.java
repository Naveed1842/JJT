package com.jjt.platform.infrastructure.audit;

import com.jjt.platform.infrastructure.persistence.entity.AuditEventEntity;
import com.jjt.platform.infrastructure.persistence.repository.AuditEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventJpaRepository auditRepo;

    public AuditService(AuditEventJpaRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Persists an audit event. Runs in a separate transaction so audit writing
     * never causes the caller's business transaction to roll back on failure.
     * Never throws: a failed audit write is logged and swallowed so it cannot
     * fail the business operation it describes.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID orgId, String eventType, UUID actorId, String actorEmail,
                    String entityType, UUID entityId, String description) {
        try {
            AuditEventEntity entity = new AuditEventEntity(
                    UUID.randomUUID(),
                    orgId,
                    eventType,
                    actorId,
                    actorEmail,
                    entityType,
                    entityId,
                    description,
                    Instant.now()
            );
            auditRepo.save(entity);
        } catch (Exception e) {
            log.error("Failed to write audit event {} for org {}: {}", eventType, orgId, e.getMessage());
        }
    }
}
