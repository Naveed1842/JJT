package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "cost_centres")
public class CostCentreEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected CostCentreEntity() {}

    public CostCentreEntity(UUID id, UUID orgId, String code, String name, UUID parentId) {
        this.id = id; this.orgId = orgId; this.code = code;
        this.name = name; this.parentId = parentId; this.active = true;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getParentId() { return parentId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
