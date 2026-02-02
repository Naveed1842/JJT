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

    public Child(UUID id, String fullName, Money educationCost) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fullName = Objects.requireNonNull(fullName, "fullName must not be null");
        if (fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
        this.educationCost = Objects.requireNonNull(educationCost, "educationCost must not be null");
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
