package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SponsorshipJpaRepository extends JpaRepository<SponsorshipEntity, UUID> {

    @Query("select (count(s) > 0) from SponsorshipEntity s where s.childId = :childId and s.startMonth >= :currentMonth")
    boolean existsActiveByChildId(@Param("childId") UUID childId, @Param("currentMonth") String currentMonth);
}
