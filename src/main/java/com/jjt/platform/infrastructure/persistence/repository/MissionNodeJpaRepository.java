package com.jjt.platform.infrastructure.persistence.repository;

import com.jjt.platform.infrastructure.persistence.entity.MissionNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MissionNodeJpaRepository extends JpaRepository<MissionNodeEntity, UUID> {
    List<MissionNodeEntity> findByOrgIdOrderByCreatedAtDesc(UUID orgId);
    List<MissionNodeEntity> findByOrgIdAndParentIdIsNullOrderByCreatedAtDesc(UUID orgId);
    List<MissionNodeEntity> findByParentId(UUID parentId);

    @Query(value = """
            WITH RECURSIVE tree AS (
                SELECT id FROM mission_nodes WHERE id = :rootId
                UNION ALL
                SELECT mn.id FROM mission_nodes mn JOIN tree t ON mn.parent_id = t.id
            )
            SELECT id FROM tree
            """, nativeQuery = true)
    List<UUID> findAllDescendantIds(@Param("rootId") UUID rootId);
}
