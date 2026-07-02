package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.AdminChildDetailResponse;
import com.jjt.platform.api.admin.dto.AdminChildSummaryResponse;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;
import com.jjt.platform.infrastructure.persistence.entity.LedgerEntryEntity;
import com.jjt.platform.infrastructure.persistence.entity.ProgressUpdateEntity;
import com.jjt.platform.infrastructure.persistence.entity.SponsorshipEntity;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.EducationSupportLedgerJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.LedgerEntryRepository;
import com.jjt.platform.infrastructure.persistence.repository.ProgressUpdateRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorshipJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminChildController {

    private final ChildJpaRepository childRepo;
    private final SponsorshipJpaRepository sponsorshipRepo;
    private final EducationSupportLedgerJpaRepository ledgerRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProgressUpdateRepository progressRepo;

    public AdminChildController(ChildJpaRepository childRepo,
                                SponsorshipJpaRepository sponsorshipRepo,
                                EducationSupportLedgerJpaRepository ledgerRepo,
                                LedgerEntryRepository ledgerEntryRepo,
                                ProgressUpdateRepository progressRepo) {
        this.childRepo = childRepo;
        this.sponsorshipRepo = sponsorshipRepo;
        this.ledgerRepo = ledgerRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.progressRepo = progressRepo;
    }

    @GetMapping("/children/list")
    public List<AdminChildSummaryResponse> listChildren(@AuthenticationPrincipal JwtUserDetails principal) {
        UUID orgId = principal.getOrgId();
        List<ChildEntity> children = orgId != null
                ? childRepo.findByOrganisationId(orgId)
                : childRepo.findAll();

        List<SponsorshipEntity> allSponsorships = orgId != null
                ? sponsorshipRepo.findByOrganisationId(orgId)
                : sponsorshipRepo.findAll();

        Map<UUID, SponsorshipEntity> activeBySponsor = allSponsorships.stream()
                .filter(s -> s.getStatus() == SponsorshipStatus.ACTIVE)
                .collect(Collectors.toMap(SponsorshipEntity::getChildId, s -> s, (a, b) -> a));

        Map<UUID, SponsorshipEntity> pendingBySponsor = allSponsorships.stream()
                .filter(s -> s.getStatus() == SponsorshipStatus.PENDING)
                .collect(Collectors.toMap(SponsorshipEntity::getChildId, s -> s, (a, b) -> a));

        return children.stream().map(child -> {
            SponsorshipEntity active = activeBySponsor.get(child.getId());
            SponsorshipEntity pending = active == null ? pendingBySponsor.get(child.getId()) : null;
            SponsorshipEntity current = active != null ? active : pending;

            String status = active != null ? "ALLOCATED" : pending != null ? "RESERVED" : "AVAILABLE";
            String sponsorName = current != null && current.getSponsor() != null
                    ? current.getSponsor().getDisplayName() : null;
            String sponsorEmail = current != null && current.getSponsor() != null
                    ? current.getSponsor().getContactEmail() : null;

            return new AdminChildSummaryResponse(
                    child.getId(),
                    child.getRollNumber(),
                    child.getFullName(),
                    child.getCity(),
                    child.getCampusName(),
                    child.getSchoolName(),
                    child.getEducationAmount().toPlainString(),
                    child.getEducationCurrency(),
                    status,
                    sponsorName,
                    sponsorEmail
            );
        }).toList();
    }

    @GetMapping("/children/{id}/detail")
    public ResponseEntity<AdminChildDetailResponse> getChildDetail(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal JwtUserDetails principal) {

        ChildEntity child = childRepo.findById(id).orElse(null);
        if (child == null) return ResponseEntity.notFound().build();

        List<SponsorshipEntity> sponsorships = sponsorshipRepo.findByChildIdOrderByCreatedAtDesc(id);

        List<LedgerEntryEntity> ledgerEntries = ledgerRepo.findByChild_Id(id)
                .map(ledger -> ledgerEntryRepo.findByLedger_IdOrderByEntryMonth(ledger.getId()))
                .orElse(List.of());

        List<ProgressUpdateEntity> progress = progressRepo.findByChildIdOrderByUpdateMonth(id);

        String status = sponsorships.stream()
                .filter(s -> s.getStatus() == SponsorshipStatus.ACTIVE).findFirst()
                .map(s -> "ALLOCATED")
                .orElseGet(() -> sponsorships.stream()
                        .filter(s -> s.getStatus() == SponsorshipStatus.PENDING).findFirst()
                        .map(s -> "RESERVED")
                        .orElse("AVAILABLE"));

        List<AdminChildDetailResponse.SponsorshipItem> sponsorshipHistory = sponsorships.stream()
                .map(s -> new AdminChildDetailResponse.SponsorshipItem(
                        s.getId(),
                        s.getSponsor() != null ? s.getSponsor().getDisplayName() : null,
                        s.getSponsor() != null ? s.getSponsor().getContactEmail() : null,
                        s.getStartMonth(),
                        s.getStatus().name(),
                        s.getCommitmentType().name(),
                        s.getCreatedAt()
                )).toList();

        List<AdminChildDetailResponse.LedgerItem> ledgerItems = ledgerEntries.stream()
                .map(e -> new AdminChildDetailResponse.LedgerItem(
                        e.getId(),
                        e.getEntryMonth(),
                        e.getEducationAmount().toPlainString(),
                        e.getEducationCurrency(),
                        e.getCoverageType().name()
                )).toList();

        List<AdminChildDetailResponse.ProgressItem> progressItems = progress.stream()
                .sorted((a, b) -> b.getUpdateMonth().compareTo(a.getUpdateMonth()))
                .map(p -> new AdminChildDetailResponse.ProgressItem(
                        p.getId(), p.getUpdateMonth(), p.getSummary()
                )).toList();

        return ResponseEntity.ok(new AdminChildDetailResponse(
                child.getId(),
                child.getRollNumber(),
                child.getFullName(),
                child.getCity(),
                child.getCampusName(),
                child.getSchoolName(),
                child.getEducationAmount().toPlainString(),
                child.getEducationCurrency(),
                status,
                sponsorshipHistory,
                ledgerItems,
                progressItems
        ));
    }
}
