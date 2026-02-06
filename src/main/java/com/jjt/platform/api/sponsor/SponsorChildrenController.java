package com.jjt.platform.api.sponsor;

import com.jjt.platform.api.common.dto.ChildDto;
import com.jjt.platform.api.common.dto.LedgerDto;
import com.jjt.platform.api.common.dto.ProgressUpdateDto;
import com.jjt.platform.api.common.dto.AvailabilityStatus;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.api.common.security.AccessGuard;
import com.jjt.platform.api.common.security.SecurityContext;
import com.jjt.platform.config.security.Role;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.mapper.ChildMapper;
import com.jjt.platform.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.jjt.platform.infrastructure.persistence.mapper.ProgressUpdateMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sponsor")
public class SponsorChildrenController {

    private final SponsorshipJpaRepository sponsorshipRepo;
    private final ChildJpaRepository childRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;

    public SponsorChildrenController(SponsorshipJpaRepository sponsorshipRepo,
                                     ChildJpaRepository childRepo,
                                     EducationSupportLedgerJpaRepository ledgerRepo,
                                     LedgerEntryRepository ledgerEntryRepo,
                                     ProgressUpdateRepository progressRepo) {
        this.sponsorshipRepo = sponsorshipRepo;
        this.childRepo = childRepo;
        this.ledgerRepo = ledgerRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.progressRepo = progressRepo;
    }

    @GetMapping("/children")
    public ResponseEntity<List<ChildDto>> listSponsorChildren() {
        SecurityContext ctx = AccessGuard.requireRole(Role.SPONSOR);
        UUID sponsorId = AccessGuard.requireSponsorId(ctx);
        List<UUID> childIds = sponsoredChildIds(sponsorId);
        List<ChildDto> result = childRepo.findAllById(childIds).stream()
                .map(ChildMapper::toDomain)
                .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<ChildDto> getChild(@PathVariable("childId") UUID childId) {
        SecurityContext ctx = AccessGuard.requireRole(Role.SPONSOR);
        UUID sponsorId = AccessGuard.requireSponsorId(ctx);
        if (!isChildSponsoredBy(sponsorId, childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return childRepo.findById(childId)
                .map(ChildMapper::toDomain)
                .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<LedgerDto> getLedger(@PathVariable("childId") UUID childId) {
        SecurityContext ctx = AccessGuard.requireRole(Role.SPONSOR);
        UUID sponsorId = AccessGuard.requireSponsorId(ctx);
        if (!isChildSponsoredBy(sponsorId, childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ledgerRepo.findByChild_Id(childId)
                .map(entity -> toDomainLedger(entity, childId))
                .map(DtoMapper::toLedgerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/progress")
    public ResponseEntity<List<ProgressUpdateDto>> getProgress(@PathVariable("childId") UUID childId) {
        SecurityContext ctx = AccessGuard.requireRole(Role.SPONSOR);
        UUID sponsorId = AccessGuard.requireSponsorId(ctx);
        if (!isChildSponsoredBy(sponsorId, childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ledgerRepo.findByChild_Id(childId)
                .map(ledgerEntity -> toDomainLedger(ledgerEntity, childId))
                .map(ledger -> {
                    List<ProgressUpdateEntity> updates = progressRepo.findByChildIdOrderByUpdateMonth(childId);
                    List<ProgressUpdate> domainUpdates = updates.stream()
                            .map(u -> ProgressUpdateMapper.toDomain(u, ledger))
                            .collect(Collectors.toList());
                    return DtoMapper.toProgressDtos(domainUpdates);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isChildSponsoredBy(UUID sponsorId, UUID childId) {
        return sponsorshipRepo.findAll().stream()
                .anyMatch(s -> s.getSponsor().getId().equals(sponsorId) && s.getChildId().equals(childId));
    }

    private List<UUID> sponsoredChildIds(UUID sponsorId) {
        return sponsorshipRepo.findAll().stream()
                .filter(s -> s.getSponsor().getId().equals(sponsorId))
                .map(SponsorshipEntity::getChildId)
                .toList();
    }

    private EducationSupportLedger toDomainLedger(EducationSupportLedgerEntity ledgerEntity, UUID childId) {
        EducationSupportLedger ledger = EducationSupportLedger.create(ledgerEntity.getId(), childId);
        List<LedgerEntryEntity> entries = ledgerEntryRepo.findByLedger_IdOrderByEntryMonth(ledgerEntity.getId());
        for (LedgerEntryEntity entryEntity : entries) {
            LedgerEntry entry = LedgerEntryMapper.toDomain(entryEntity);
            ledger = ledger.appendEntry(entry);
        }
        return ledger;
    }

    private AvailabilityStatus deriveAvailability(UUID childId) {
        boolean hasActive = sponsorshipRepo.existsByChildIdAndStatus(childId, com.jjt.platform.core.domain.entity.SponsorshipStatus.ACTIVE);
        if (hasActive) {
            return AvailabilityStatus.ALLOCATED;
        }
        boolean hasPending = sponsorshipRepo.existsByChildIdAndStatus(childId, com.jjt.platform.core.domain.entity.SponsorshipStatus.PENDING);
        if (hasPending) {
            return AvailabilityStatus.RESERVED;
        }
        return AvailabilityStatus.AVAILABLE;
    }
}
