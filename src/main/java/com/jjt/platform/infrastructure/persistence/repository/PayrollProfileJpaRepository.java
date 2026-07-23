package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.PayrollProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollProfileJpaRepository extends JpaRepository<PayrollProfileEntity, UUID> {
    Optional<PayrollProfileEntity> findByPersonId(UUID personId);
    List<PayrollProfileEntity> findByActiveTrue();
}
