package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.entity.CommitmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sponsorships", uniqueConstraints = @UniqueConstraint(name = "uk_sponsor_child_start", columnNames = {"sponsor_id", "child_id", "start_month"}))
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class SponsorshipEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private SponsorEntity sponsor;

    @Column(name = "child_id", nullable = false)
    private UUID childId;

    @Column(name = "start_month", nullable = false, length = 7)
    private String startMonth; // YYYY-MM

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Setter
    private SponsorshipStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "commitment_type", nullable = false, length = 20)
    private CommitmentType commitmentType;
    
    // Validation method using Apache Commons
    public static SponsorshipEntity create(UUID id, SponsorEntity sponsor, UUID childId, String startMonth,
                                          SponsorshipStatus status, Instant createdAt, Instant expiresAt,
                                          CommitmentType commitmentType) {
        Validate.notNull(id, "Sponsorship ID cannot be null");
        Validate.notNull(sponsor, "Sponsor cannot be null");
        Validate.notNull(childId, "Child ID cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(startMonth), "Start month cannot be blank");
        Validate.notNull(status, "Status cannot be null");
        Validate.notNull(createdAt, "Created at cannot be null");
        Validate.notNull(commitmentType, "Commitment type cannot be null");
        
        return SponsorshipEntity.builder()
                .id(id)
                .sponsor(sponsor)
                .childId(childId)
                .startMonth(StringUtils.trim(startMonth))
                .status(status)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .commitmentType(commitmentType)
                .build();
    }
}
