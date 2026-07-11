package com.jjt.platform.api.org;

import com.jjt.platform.api.common.dto.ChildDto;
import com.jjt.platform.api.common.dto.LedgerDto;
import com.jjt.platform.api.common.dto.ProgressUpdateDto;
import com.jjt.platform.api.common.dto.AvailabilityStatus;
import com.jjt.platform.api.common.mapper.DtoMapper;
import com.jjt.platform.core.domain.entity.EducationSupportLedger;
import com.jjt.platform.core.domain.entity.LedgerEntry;
import com.jjt.platform.core.domain.entity.ProgressUpdate;
import com.jjt.platform.infrastructure.persistence.entity.EducationSupportLedgerEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.mapper.ChildMapper;
import com.jjt.platform.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.jjt.platform.infrastructure.persistence.mapper.ProgressUpdateMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/org")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class OrgChildrenController {

    private final ChildJpaRepository childRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;

    public OrgChildrenController(ChildJpaRepository childRepo,
                                 EducationSupportLedgerJpaRepository ledgerRepo,
                                 LedgerEntryRepository ledgerEntryRepo,
                                 ProgressUpdateRepository progressRepo,
                                 SponsorshipJpaRepository sponsorshipRepo) {
        this.childRepo = childRepo;
        this.ledgerRepo = ledgerRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.progressRepo = progressRepo;
        this.sponsorshipRepo = sponsorshipRepo;
    }

    @GetMapping("/children")
    @PreAuthorize("permitAll()")
    public List<ChildDto> listChildren() {
        Set<UUID> activeChildIds = sponsorshipRepo.findChildIdsByStatus(com.jjt.platform.core.domain.entity.SponsorshipStatus.ACTIVE);
        Set<UUID> pendingChildIds = sponsorshipRepo.findChildIdsByStatus(com.jjt.platform.core.domain.entity.SponsorshipStatus.PENDING);
        return childRepo.findAll().stream()
                .map(entity -> {
                    var child = ChildMapper.toDomain(entity);
                    LocalDate enrolledAt = entity.getCreatedAt() != null
                            ? entity.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate()
                            : null;
                    return DtoMapper.toChildDto(child, deriveAvailability(child.getId(), activeChildIds, pendingChildIds), enrolledAt);
                })
                .toList();
    }

    @GetMapping("/children/{childId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ChildDto> getChild(@PathVariable("childId") UUID childId) {
        return childRepo.findById(childId)
                .map(entity -> {
                    var child = ChildMapper.toDomain(entity);
                    LocalDate enrolledAt = entity.getCreatedAt() != null
                            ? entity.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate()
                            : null;
                    return DtoMapper.toChildDto(child, deriveAvailability(child.getId()), enrolledAt);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/ledger")
    @PreAuthorize("permitAll()")
    public ResponseEntity<LedgerDto> getChildLedger(@PathVariable("childId") UUID childId) {
        return ledgerRepo.findByChild_Id(childId)
                .map(ledgerEntity -> toDomainLedger(ledgerEntity, childId))
                .map(DtoMapper::toLedgerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/progress")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProgressUpdateDto>> getChildProgress(@PathVariable("childId") UUID childId) {
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
        if (sponsorshipRepo.existsByChildIdAndStatus(childId, com.jjt.platform.core.domain.entity.SponsorshipStatus.ACTIVE)) {
            return AvailabilityStatus.ALLOCATED;
        }
        if (sponsorshipRepo.existsByChildIdAndStatus(childId, com.jjt.platform.core.domain.entity.SponsorshipStatus.PENDING)) {
            return AvailabilityStatus.RESERVED;
        }
        return AvailabilityStatus.AVAILABLE;
    }

    private AvailabilityStatus deriveAvailability(UUID childId, Set<UUID> activeChildIds, Set<UUID> pendingChildIds) {
        if (activeChildIds.contains(childId)) {
            return AvailabilityStatus.ALLOCATED;
        }
        if (pendingChildIds.contains(childId)) {
            return AvailabilityStatus.RESERVED;
        }
        return AvailabilityStatus.AVAILABLE;
    }
}
