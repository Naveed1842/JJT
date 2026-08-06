package com.jjt.platform.core.domain.entity;

import java.time.Instant;
import java.util.UUID;

public final class Vendor {
    private final UUID id;
    private final UUID orgId;
    private final String name;
    private final String contactName;
    private final String email;
    private final String phone;
    private final String bankAccount;
    private final String taxId;
    private final boolean active;
    private final Instant createdAt;

    public Vendor(UUID id, UUID orgId, String name, String contactName,
                  String email, String phone, String bankAccount, String taxId,
                  boolean active, Instant createdAt) {
        this.id = id; this.orgId = orgId; this.name = name;
        this.contactName = contactName; this.email = email; this.phone = phone;
        this.bankAccount = bankAccount; this.taxId = taxId;
        this.active = active; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getName() { return name; }
    public String getContactName() { return contactName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBankAccount() { return bankAccount; }
    public String getTaxId() { return taxId; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
