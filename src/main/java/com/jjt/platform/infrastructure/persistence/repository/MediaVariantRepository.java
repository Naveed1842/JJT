package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MediaVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaVariantRepository extends JpaRepository<MediaVariantEntity, UUID> {

    List<MediaVariantEntity> findByMediaId(UUID mediaId);

    Optional<MediaVariantEntity> findByMediaIdAndVariantType(UUID mediaId, String variantType);
}
