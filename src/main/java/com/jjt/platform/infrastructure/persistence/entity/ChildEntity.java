package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "children")
public class ChildEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "education_amount", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal educationAmount;

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

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "profile_photo_media_id")
    private UUID profilePhotoMediaId;

    @OneToOne(mappedBy = "child", optional = false)
    private EducationSupportLedgerEntity ledger;

    protected ChildEntity() {
    }

    public ChildEntity(UUID id, String fullName, java.math.BigDecimal educationAmount, String educationCurrency,
                       String rollNumber, String city, String campusName, String schoolName, UUID organisationId) {
        this.id = id;
        this.fullName = fullName;
        this.educationAmount = educationAmount;
        this.educationCurrency = educationCurrency;
        this.rollNumber = rollNumber;
        this.city = city;
        this.campusName = campusName;
        this.schoolName = schoolName;
        this.organisationId = organisationId;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public java.math.BigDecimal getEducationAmount() {
        return educationAmount;
    }

    public String getEducationCurrency() {
        return educationCurrency;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getCity() {
        return city;
    }

    public String getCampusName() {
        return campusName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public UUID getOrganisationId() { return organisationId; }

    public UUID getProfilePhotoMediaId() { return profilePhotoMediaId; }

    public void setProfilePhotoMediaId(UUID profilePhotoMediaId) {
        this.profilePhotoMediaId = profilePhotoMediaId;
    }

    public EducationSupportLedgerEntity getLedger() {
        return ledger;
    }
}
