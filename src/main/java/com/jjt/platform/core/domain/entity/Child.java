package com.jjt.platform.core.domain.entity;

import com.jjt.platform.core.domain.value.Money;

import java.util.Objects;
import java.util.UUID;

/**
 * Child exists independently of sponsors; monthly education cost is mandatory.
 * Support status is derived elsewhere (not stored here).
 */
public final class Child {

    private final UUID id;
    private final String fullName;
    private final Money educationCost;
    private final String rollNumber;
    private final String city;
    private final String campusName;
    private final String schoolName; // optional

    public Child(UUID id, String fullName, Money educationCost,
                 String rollNumber, String city, String campusName, String schoolName) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fullName = Objects.requireNonNull(fullName, "fullName must not be null");
        if (fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
        this.educationCost = Objects.requireNonNull(educationCost, "educationCost must not be null");
        this.rollNumber = Objects.requireNonNull(rollNumber, "rollNumber must not be null");
        this.city = Objects.requireNonNull(city, "city must not be null");
        this.campusName = Objects.requireNonNull(campusName, "campusName must not be null");
        if (rollNumber.isBlank()) {
            throw new IllegalArgumentException("rollNumber must not be blank");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        if (campusName.isBlank()) {
            throw new IllegalArgumentException("campusName must not be blank");
        }
        this.schoolName = (schoolName != null && schoolName.isBlank()) ? null : schoolName;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Money getEducationCost() {
        return educationCost;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Child child = (Child) o;
        return id.equals(child.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
