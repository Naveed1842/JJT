package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProgressUpdateRepository extends CrudRepository<ProgressUpdateEntity, UUID> {
    Optional<ProgressUpdateEntity> findByChildIdAndUpdateMonth(UUID childId, String updateMonth);
}
