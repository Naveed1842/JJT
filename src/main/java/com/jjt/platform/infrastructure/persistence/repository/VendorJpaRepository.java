package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorJpaRepository extends JpaRepository<VendorEntity, UUID> {
    List<VendorEntity> findByOrgIdAndActiveOrderByNameAsc(UUID orgId, boolean active);
    List<VendorEntity> findByOrgIdOrderByNameAsc(UUID orgId);

    @Query("SELECT v FROM VendorEntity v WHERE v.orgId = :orgId AND LOWER(v.name) = LOWER(:name)")
    Optional<VendorEntity> findByOrgIdAndNameIgnoreCase(@Param("orgId") UUID orgId, @Param("name") String name);
}
