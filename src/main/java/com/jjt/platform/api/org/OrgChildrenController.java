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
import com.jjt.platform.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.jjt.platform.infrastructure.persistence.mapper.ProgressUpdateMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/org")
@Tag(name = "Organization", description = "Organization children endpoints")
@SecurityRequirement(name = "bearerAuth")
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

    // NOTE: In real system, enforce ORG role; omitted per instructions

    @GetMapping("/children")
    public List<ChildDto> listChildren() {
        return childRepo.findAll().stream()
                .map(ChildMapper::toDomain)
                .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                .toList();
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<ChildDto> getChild(@PathVariable("childId") UUID childId) {
        return childRepo.findById(childId)
                .map(ChildMapper::toDomain)
                .map(child -> DtoMapper.toChildDto(child, deriveAvailability(child.getId())))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<LedgerDto> getChildLedger(@PathVariable("childId") UUID childId) {
        return ledgerRepo.findByChild_Id(childId)
                .map(ledgerEntity -> toDomainLedger(ledgerEntity, childId))
                .map(DtoMapper::toLedgerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/children/{childId}/progress")
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
