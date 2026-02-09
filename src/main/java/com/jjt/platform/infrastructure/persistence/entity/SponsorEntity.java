package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

@Entity
@Table(name = "sponsors")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SponsorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "phone")
    private String phone;
    
    // Validation method using Apache Commons
    public static SponsorEntity create(UUID id, String displayName, String contactEmail, String phone) {
        Validate.notNull(id, "Sponsor ID cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(displayName), "Display name cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(contactEmail), "Contact email cannot be blank");
        
        return SponsorEntity.builder()
                .id(id)
                .displayName(StringUtils.trim(displayName))
                .contactEmail(StringUtils.trim(contactEmail))
                .phone(StringUtils.trimToNull(phone))
                .build();
    }
}
