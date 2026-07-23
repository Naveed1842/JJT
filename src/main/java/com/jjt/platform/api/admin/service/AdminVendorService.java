package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.VendorEntity;
import com.jjt.platform.infrastructure.persistence.repository.VendorJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminVendorService {

    private final VendorJpaRepository vendorRepo;

    public AdminVendorService(VendorJpaRepository vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    @Transactional(readOnly = true)
    public List<VendorEntity> list(UUID orgId, Boolean active) {
        if (active != null) return vendorRepo.findByOrgIdAndActiveOrderByNameAsc(orgId, active);
        return vendorRepo.findByOrgIdOrderByNameAsc(orgId);
    }

    @Transactional(readOnly = true)
    public VendorEntity get(UUID vendorId) {
        return vendorRepo.findById(vendorId)
                .orElseThrow(() -> new DomainException("Vendor not found"));
    }

    @Transactional
    public VendorEntity create(UUID orgId, String name, String contactName,
                               String email, String phone, String taxId) {
        VendorEntity entity = new VendorEntity(UUID.randomUUID(), orgId, name,
                contactName, email, phone, taxId);
        return vendorRepo.save(entity);
    }

    @Transactional
    public VendorEntity update(UUID vendorId, String name, String contactName,
                               String email, String phone, Boolean active) {
        VendorEntity entity = get(vendorId);
        if (name != null) entity.setName(name);
        if (contactName != null) entity.setContactName(contactName);
        if (email != null) entity.setEmail(email);
        if (phone != null) entity.setPhone(phone);
        if (active != null) entity.setActive(active);
        return vendorRepo.save(entity);
    }
}
