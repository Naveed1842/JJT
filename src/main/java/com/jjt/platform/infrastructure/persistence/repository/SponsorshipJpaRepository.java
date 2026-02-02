package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SponsorshipJpaRepository extends JpaRepository<SponsorshipEntity, UUID> {
}
