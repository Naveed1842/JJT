package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "children")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "education_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal educationAmount;

    @Column(name = "education_currency", nullable = false, length = 3)
    private String educationCurrency;

    @Column(name = "roll_number", nullable = false, unique = true)
    private String rollNumber;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "campus_name", nullable = false)
    private String campusName;

    @Column(name = "school_name")
    private String schoolName;

    @OneToOne(mappedBy = "child", optional = false)
    private EducationSupportLedgerEntity ledger;
    
    // Validation method using Apache Commons
    public static ChildEntity create(UUID id, String fullName, BigDecimal educationAmount, String educationCurrency,
                                    String rollNumber, String city, String campusName, String schoolName) {
        Validate.notNull(id, "Child ID cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(fullName), "Full name cannot be blank");
        Validate.notNull(educationAmount, "Education amount cannot be null");
        Validate.isTrue(educationAmount.compareTo(BigDecimal.ZERO) > 0, "Education amount must be positive");
        Validate.isTrue(StringUtils.isNotBlank(educationCurrency), "Education currency cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(rollNumber), "Roll number cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(city), "City cannot be blank");
        Validate.isTrue(StringUtils.isNotBlank(campusName), "Campus name cannot be blank");
        
        return ChildEntity.builder()
                .id(id)
                .fullName(StringUtils.trim(fullName))
                .educationAmount(educationAmount)
                .educationCurrency(StringUtils.upperCase(StringUtils.trim(educationCurrency)))
                .rollNumber(StringUtils.trim(rollNumber))
                .city(StringUtils.trim(city))
                .campusName(StringUtils.trim(campusName))
                .schoolName(StringUtils.trimToNull(schoolName))
                .build();
    }
}
