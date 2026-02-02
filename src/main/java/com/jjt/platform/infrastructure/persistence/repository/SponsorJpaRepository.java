package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SponsorJpaRepository extends JpaRepository<SponsorEntity, UUID> {
}
