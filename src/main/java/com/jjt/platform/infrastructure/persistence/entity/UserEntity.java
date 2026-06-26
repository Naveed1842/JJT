package com.jjt.platform.infrastructure.persistence.entity;

import com.jjt.platform.config.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "sponsor_id")
    private UUID sponsorId;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected UserEntity() {}

    public UserEntity(String email, String passwordHash, Role role, UUID sponsorId, UUID orgId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.sponsorId = sponsorId;
        this.orgId = orgId;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public UUID getSponsorId() { return sponsorId; }
    public UUID getOrgId() { return orgId; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; this.updatedAt = Instant.now(); }
    public void setActive(boolean active) { this.active = active; this.updatedAt = Instant.now(); }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setSponsorId(UUID sponsorId) { this.sponsorId = sponsorId; this.updatedAt = Instant.now(); }
}
