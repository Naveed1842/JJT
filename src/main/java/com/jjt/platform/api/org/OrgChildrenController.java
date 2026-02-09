package com.jjt.platform.api.org;

import com.jjt.platform.api.common.dto.ChildDto;
import com.jjt.platform.api.common.dto.LedgerDto;
import com.jjt.platform.api.common.dto.ProgressUpdateDto;
import com.jjt.platform.api.common.dto.AvailabilityStatus;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.api.common.security.AccessGuard;
import com.jjt.platform.config.security.Role;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.mapper.ChildMapper;
import com.jjt.platform.infrastructure.persistence.mapper.EducationSupportLedgerMapper;
import com.jjt.platform.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.jjt.platform.infrastructure.persistence.mapper.ProgressUpdateMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
@Slf4j
public class OrgChildrenController {

    private final ChildJpaRepository childRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;

    @GetMapping("/children")
    public List<ChildDto> listChildren() {
        log.info("Fetching all children for organization/sponsor access");
        AccessGuard.requireRole(Role.ORG_ADMIN, Role.SPONSOR);
        
        try {
            var result = childRepo.findAll().stream()
                    .map(ChildMapper::toDomain)
                    .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                    .toList();
            log.info("Successfully retrieved {} children", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch children list: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<ChildDto> getChild(@PathVariable UUID childId) {
        log.info("Fetching child details for ID: {}", childId);
        AccessGuard.requireRole(Role.ORG_ADMIN, Role.SPONSOR);
        Validate.notNull(childId, "Child ID cannot be null");
        
        try {
            return childRepo.findById(childId)
                    .map(ChildMapper::toDomain)
                    .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                    .map(childDto -> {
                        log.info("Successfully retrieved child details for ID: {}", childId);
                        return ResponseEntity.ok(childDto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to fetch child details for ID: {}: {}", childId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<LedgerDto> getChildLedger(@PathVariable("childId") UUID childId) {
        AccessGuard.requireRole(Role.ORG_ADMIN, Role.SPONSOR);
        return ledgerRepo.findByChild_Id(childId)
                .map(ledgerEntity -> toDomainLedger(ledgerEntity, childId))
                .map(DtoMapper::toLedgerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/progress")
    public ResponseEntity<List<ProgressUpdateDto>> getChildProgress(@PathVariable UUID childId) {
        log.info("Fetching progress updates for child ID: {}", childId);
        AccessGuard.requireRole(Role.ORG_ADMIN, Role.SPONSOR);
        Validate.notNull(childId, "Child ID cannot be null");
        
        try {
            return ledgerRepo.findByChild_Id(childId)
                    .map(ledgerEntity -> toDomainLedger(ledgerEntity, childId))
                    .map(ledger -> {
                        List<ProgressUpdateEntity> updates = progressRepo.findByChildIdOrderByUpdateMonth(childId);
                        List<ProgressUpdate> domainUpdates = updates.stream()
                                .map(u -> ProgressUpdateMapper.toDomain(u, ledger))
                                .toList();
                        var result = DtoMapper.toProgressDtos(domainUpdates);
                        log.info("Successfully retrieved {} progress updates for child ID: {}", result.size(), childId);
                        return ResponseEntity.ok(result);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to fetch progress updates for child ID: {}: {}", childId, e.getMessage(), e);
            throw e;
        }
    }

    private EducationSupportLedger toDomainLedger(EducationSupportLedgerEntity ledgerEntity, UUID childId) {
        Validate.notNull(ledgerEntity, "Ledger entity cannot be null");
        Validate.notNull(childId, "Child ID cannot be null");
        
        EducationSupportLedger ledger = EducationSupportLedger.create(ledgerEntity.getId(), childId);
        List<LedgerEntryEntity> entries = ledgerEntryRepo.findByLedger_IdOrderByEntryMonth(ledgerEntity.getId());
        for (LedgerEntryEntity entryEntity : entries) {
            LedgerEntry entry = LedgerEntryMapper.toDomain(entryEntity);
            ledger = ledger.appendEntry(entry);
        }
        return ledger;
    }

    private AvailabilityStatus deriveAvailability(UUID childId) {
        Validate.notNull(childId, "Child ID cannot be null");
        
        boolean hasActive = sponsorshipRepo.existsByChildIdAndStatus(childId, 
                com.jjt.platform.core.domain.entity.SponsorshipStatus.ACTIVE);
        if (hasActive) {
            return AvailabilityStatus.ALLOCATED;
        }
        boolean hasPending = sponsorshipRepo.existsByChildIdAndStatus(childId, 
                com.jjt.platform.core.domain.entity.SponsorshipStatus.PENDING);
        if (hasPending) {
            return AvailabilityStatus.RESERVED;
        }
        return AvailabilityStatus.AVAILABLE;
    }
}
