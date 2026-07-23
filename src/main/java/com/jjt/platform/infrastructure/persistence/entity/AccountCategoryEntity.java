package com.jjt.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "account_categories")
public class AccountCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "reporting_class", nullable = false, length = 20)
    private String reportingClass;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected AccountCategoryEntity() {}

    public AccountCategoryEntity(UUID orgId, String code, String name, Integer parentId,
                                 String reportingClass, boolean system) {
        this.orgId = orgId; this.code = code; this.name = name;
        this.parentId = parentId; this.reportingClass = reportingClass;
        this.system = system; this.active = true;
    }

    public Integer getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getParentId() { return parentId; }
    public String getReportingClass() { return reportingClass; }
    public boolean isSystem() { return system; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
