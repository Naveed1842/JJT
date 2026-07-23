package com.jjt.platform.api.publics;

import com.jjt.platform.api.admin.service.TransparencyService;
import com.jjt.platform.infrastructure.persistence.entity.TransparencySnapshotEntity;
import com.jjt.platform.infrastructure.persistence.repository.TransparencySnapshotJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/transparency")
public class PublicTransparencyController {

    private final TransparencyService transparencyService;
    private final TransparencySnapshotJpaRepository snapshotRepo;

    public PublicTransparencyController(TransparencyService transparencyService,
                                        TransparencySnapshotJpaRepository snapshotRepo) {
        this.transparencyService = transparencyService;
        this.snapshotRepo = snapshotRepo;
    }

    @GetMapping
    public ResponseEntity<TransparencyResponse> getLatest(@RequestParam(required = false) UUID orgId) {
        // If no orgId provided, return the most recent published snapshot across all orgs
        TransparencySnapshotEntity snapshot = orgId != null
                ? transparencyService.getLatestPublished(orgId).orElse(null)
                : snapshotRepo.findAll().stream()
                        .filter(s -> s.isPublished())
                        .max((a, b) -> a.getComputedAt().compareTo(b.getComputedAt()))
                        .orElse(null);

        if (snapshot == null) return ResponseEntity.notFound().build();

        List<TransparencySnapshotEntity> history = orgId != null
                ? transparencyService.getHistory(orgId)
                : List.of();

        return ResponseEntity.ok(toResponse(snapshot, history));
    }

    private TransparencyResponse toResponse(TransparencySnapshotEntity s,
                                             List<TransparencySnapshotEntity> history) {
        List<TrendPoint> trend = history.stream()
                .limit(6)
                .map(h -> new TrendPoint(h.getComputedAt().toString(),
                        h.getProgrammePct(), h.getTotalIncome()))
                .toList();

        return new TransparencyResponse(
                s.getComputedAt().toString(),
                s.getProgrammePct(),
                s.getAdminPct(),
                s.getFundraisingPct(),
                s.getTotalIncome(),
                s.getTotalExpense(),
                s.getBeneficiaryCount(),
                s.getCostPerBeneficiary(),
                trend,
                s.isPublished()
        );
    }

    public record TransparencyResponse(
            String asOf,
            BigDecimal programmePct,
            BigDecimal adminPct,
            BigDecimal fundraisingPct,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            int beneficiaryCount,
            BigDecimal costPerBeneficiary,
            List<TrendPoint> trend,
            boolean published
    ) {}

    public record TrendPoint(String period, BigDecimal programmePct, BigDecimal totalIncome) {}
}
