package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@Entity
@Table(name = "progress_updates",
       uniqueConstraints = @UniqueConstraint(name = "uk_child_month", columnNames = {"child_id", "update_month"}))
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgressUpdateEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "child_id", nullable = false)
    private UUID childId;

    @Column(name = "update_month", nullable = false, length = 7)
    private String updateMonth; // YYYY-MM

    @Column(name = "summary", nullable = false, length = 2000)
    private String summary;
    
    // Validation method using Apache Commons
    public static ProgressUpdateEntity create(UUID id, UUID childId, String updateMonth, String summary) {
        Validate.notNull(id, "Progress update ID cannot be null");
        Validate.notNull(childId, "Child ID cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(updateMonth), "Update month cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(summary), "Summary cannot be blank");
        
        return ProgressUpdateEntity.builder()
                .id(id)
                .childId(childId)
                .updateMonth(StringUtils.trim(updateMonth))
                .summary(StringUtils.trim(summary))
                .build();
    }
}
