package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.util.UUID;

public final class Person {
    private final UUID id;
    private final UUID orgId;
    private final PersonKind kind;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final boolean active;
    private final Instant createdAt;

    public Person(UUID id, UUID orgId, PersonKind kind, String firstName, String lastName,
                  String email, String phone, boolean active, Instant createdAt) {
        this.id = id; this.orgId = orgId; this.kind = kind;
        this.firstName = firstName; this.lastName = lastName;
        this.email = email; this.phone = phone;
        this.active = active; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public PersonKind getKind() { return kind; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
