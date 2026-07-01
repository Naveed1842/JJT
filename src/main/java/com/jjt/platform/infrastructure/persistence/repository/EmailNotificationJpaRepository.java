package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.EmailNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailNotificationJpaRepository extends JpaRepository<EmailNotificationEntity, UUID> {

    List<EmailNotificationEntity> findByStatusOrderByCreatedAtDesc(String status);
}
