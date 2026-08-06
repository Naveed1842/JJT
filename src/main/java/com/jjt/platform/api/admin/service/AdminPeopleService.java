package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.infrastructure.persistence.entity.PayrollProfileEntity;
import com.jjt.platform.infrastructure.persistence.entity.PersonEntity;
import com.jjt.platform.infrastructure.persistence.repository.PayrollProfileJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.PersonJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminPeopleService {

    private final PersonJpaRepository personRepo;
    private final PayrollProfileJpaRepository profileRepo;

    public AdminPeopleService(PersonJpaRepository personRepo, PayrollProfileJpaRepository profileRepo) {
        this.personRepo = personRepo;
        this.profileRepo = profileRepo;
    }

    @Transactional(readOnly = true)
    public List<PersonEntity> list(UUID orgId, Boolean active) {
        if (active != null) return personRepo.findByOrgIdAndActiveOrderByLastNameAsc(orgId, active);
        return personRepo.findByOrgIdOrderByLastNameAsc(orgId);
    }

    @Transactional(readOnly = true)
    public PersonEntity get(UUID personId) {
        return personRepo.findById(personId)
                .orElseThrow(() -> new DomainException("Person not found"));
    }

    @Transactional
    public PersonEntity create(UUID orgId, String kind, String firstName, String lastName,
                               String email, String phone) {
        PersonEntity entity = new PersonEntity(UUID.randomUUID(), orgId, kind,
                firstName, lastName, email, phone);
        return personRepo.save(entity);
    }

    @Transactional
    public PersonEntity update(UUID personId, String firstName, String lastName,
                               String email, String phone, Boolean active) {
        PersonEntity entity = get(personId);
        if (firstName != null) entity.setFirstName(firstName);
        if (lastName != null) entity.setLastName(lastName);
        if (email != null) entity.setEmail(email);
        if (phone != null) entity.setPhone(phone);
        if (active != null) entity.setActive(active);
        return personRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PayrollProfileEntity> getPayrollProfile(UUID personId) {
        return profileRepo.findByPersonId(personId);
    }

    @Transactional
    public PayrollProfileEntity upsertPayrollProfile(UUID personId, String salaryType,
                                                      BigDecimal amount, String currency,
                                                      UUID costCentreId, String paymentMethod) {
        if (!personRepo.existsById(personId)) throw new DomainException("Person not found");
        return profileRepo.findByPersonId(personId)
                .map(existing -> {
                    existing.setAmount(amount);
                    existing.setCurrency(currency);
                    existing.setCostCentreId(costCentreId);
                    existing.setPaymentMethod(paymentMethod);
                    return profileRepo.save(existing);
                })
                .orElseGet(() -> profileRepo.save(new PayrollProfileEntity(
                        UUID.randomUUID(), personId, salaryType, amount,
                        currency, costCentreId, paymentMethod)));
    }
}
