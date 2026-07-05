package com.jjt.platform.infrastructure.persistence.seed;

import com.jjt.platform.core.domain.entity.FundTransactionType;
import com.jjt.platform.infrastructure.persistence.entity.FundAccountEntity;
import com.jjt.platform.infrastructure.persistence.entity.FundTransactionEntity;
import com.jjt.platform.infrastructure.persistence.repository.FundAccountJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FundTransactionJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.OrganisationJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Seeds the JJT General Education Fund on first boot.
 * Runs after OrganisationInitializer (@Order 1) so the org exists.
 * Records an opening CREDIT transaction for the configured initial balance.
 */
@Component
@Order(4)
public class FundAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FundAccountInitializer.class);

    private final FundAccountJpaRepository fundAccountRepo;
    private final FundTransactionJpaRepository fundTransactionRepo;
    private final OrganisationJpaRepository orgRepo;
    private final String orgSlug;
    private final String baseCurrency;
    private final BigDecimal initialBalance;
    private final BigDecimal minReserve;

    public FundAccountInitializer(FundAccountJpaRepository fundAccountRepo,
                                  FundTransactionJpaRepository fundTransactionRepo,
                                  OrganisationJpaRepository orgRepo,
                                  @Value("${jjt.org.slug:jjt}") String orgSlug,
                                  @Value("${jjt.org.base-currency:PKR}") String baseCurrency,
                                  @Value("${jjt.fund.initial-balance:0.00}") BigDecimal initialBalance,
                                  @Value("${jjt.fund.min-reserve:20000.00}") BigDecimal minReserve) {
        this.fundAccountRepo = fundAccountRepo;
        this.fundTransactionRepo = fundTransactionRepo;
        this.orgRepo = orgRepo;
        this.orgSlug = orgSlug;
        this.baseCurrency = baseCurrency;
        this.initialBalance = initialBalance;
        this.minReserve = minReserve;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var orgOpt = orgRepo.findBySlug(orgSlug);
        if (orgOpt.isEmpty()) {
            log.warn("Organisation '{}' not found; skipping fund account seed", orgSlug);
            return;
        }
        UUID orgId = orgOpt.get().getId();
        if (fundAccountRepo.existsByOrganisationId(orgId)) {
            return;
        }

        UUID fundId = UUID.randomUUID();
        FundAccountEntity fund = new FundAccountEntity(
                fundId,
                "JJT General Education Fund",
                baseCurrency,
                minReserve,
                orgId,
                null,
                Instant.now()
        );
        fundAccountRepo.save(fund);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            FundTransactionEntity opening = new FundTransactionEntity(
                    UUID.randomUUID(),
                    fundId,
                    FundTransactionType.CREDIT,
                    initialBalance,
                    baseCurrency,
                    "OPENING_BALANCE",
                    "Opening balance at fund creation",
                    null,
                    null,
                    null,
                    Instant.now()
            );
            fundTransactionRepo.save(opening);
            log.info("Seeded JJT General Education Fund with opening balance {} {}", initialBalance, baseCurrency);
        } else {
            log.info("Seeded JJT General Education Fund (zero opening balance; credit via POST /api/admin/funds/{id}/credit)");
        }
    }
}
