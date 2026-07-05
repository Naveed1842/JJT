package com.jjt.platform.infrastructure.persistence.seed;

import com.jjt.platform.infrastructure.persistence.entity.OrganisationEntity;
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
 * Seeds the default JJT organisation on first boot.
 * Runs before AdminUserInitializer (@Order(2)) so the org exists before any user references it.
 * The organisation row is also created by V13 migration; this initializer is a safety net for
 * environments where the migration was applied before this code shipped.
 */
@Component
@Order(1)
public class OrganisationInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OrganisationInitializer.class);

    private final OrganisationJpaRepository orgRepo;
    private final String orgSlug;
    private final String orgName;
    private final String baseCurrency;

    public OrganisationInitializer(OrganisationJpaRepository orgRepo,
                                   @Value("${jjt.org.slug:jjt}") String orgSlug,
                                   @Value("${jjt.org.name:Junior Jinnah Trust}") String orgName,
                                   @Value("${jjt.org.base-currency:PKR}") String baseCurrency) {
        this.orgRepo = orgRepo;
        this.orgSlug = orgSlug;
        this.orgName = orgName;
        this.baseCurrency = baseCurrency;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (orgRepo.findBySlug(orgSlug).isPresent()) {
            return;
        }
        var entity = new OrganisationEntity(
                UUID.randomUUID(), orgName, orgSlug, baseCurrency, 15, BigDecimal.valueOf(20000), true, Instant.now());
        orgRepo.save(entity);
        log.info("Seeded organisation: {} (slug={})", orgName, orgSlug);
    }
}
