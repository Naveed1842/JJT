package com.jjt.platform.infrastructure.persistence.seed;

import com.jjt.platform.api.admin.service.AdminCommandService;
import com.jjt.platform.core.domain.entity.Sponsor;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.core.domain.value.YearMonthValue;
import com.jjt.platform.infrastructure.persistence.entity.SponsorEntity;
import com.jjt.platform.infrastructure.persistence.mapper.SponsorMapper;
import com.jjt.platform.infrastructure.persistence.repository.ChildJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.SponsorJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Currency;
import java.util.UUID;

/**
 * Deterministic dev-only seed data to exercise Phase-1 flows.
 * Uses application services to preserve domain invariants.
 */
@Component
@Profile("dev")
public class TestDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);

    private static final UUID CHILD_A_ID = UUID.nameUUIDFromBytes("CHILD-001".getBytes());
    private static final UUID LEDGER_A_ID = UUID.nameUUIDFromBytes("LEDGER-001".getBytes());
    private static final UUID CHILD_B_ID = UUID.nameUUIDFromBytes("CHILD-002".getBytes());
    private static final UUID LEDGER_B_ID = UUID.nameUUIDFromBytes("LEDGER-002".getBytes());
    private static final UUID SPONSOR_ID = UUID.nameUUIDFromBytes("SPONSOR-001".getBytes());

    private final AdminCommandService adminCommands;
    private final ChildJpaRepository childRepo;
    private final SponsorJpaRepository sponsorRepo;

    public TestDataInitializer(AdminCommandService adminCommands,
                               ChildJpaRepository childRepo,
                               SponsorJpaRepository sponsorRepo) {
        this.adminCommands = adminCommands;
        this.childRepo = childRepo;
        this.sponsorRepo = sponsorRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (childRepo.findById(CHILD_A_ID).isPresent() || childRepo.findById(CHILD_B_ID).isPresent()) {
            log.info("Test data already present; skipping seeding.");
            return;
        }

        Money educationCost = Money.of("120.00", Currency.getInstance("USD").getCurrencyCode());
        YearMonth now = YearMonth.now();
        YearMonthValue previousMonth = YearMonthValue.of(now.minusMonths(1));
        YearMonthValue currentMonth = YearMonthValue.of(now);
        YearMonthValue nextMonth = YearMonthValue.of(now.plusMonths(1));

        seedSponsor();
        seedChildA(previousMonth, currentMonth, educationCost);
        seedChildB(previousMonth, nextMonth, currentMonth, educationCost);

        log.info("Dev seed data loaded: children {}, {}, sponsor {}", CHILD_A_ID, CHILD_B_ID, SPONSOR_ID);
    }

    private void seedSponsor() {
        if (sponsorRepo.findById(SPONSOR_ID).isPresent()) {
            return;
        }
        Sponsor sponsor = new Sponsor(SPONSOR_ID, "Future Path Sponsor", "sponsor@example.org");
        SponsorEntity entity = SponsorMapper.toEntity(sponsor);
        sponsorRepo.save(entity);
    }

    private void seedChildA(YearMonthValue previousMonth, YearMonthValue currentMonth, Money educationCost) {
        var createResult = adminCommands.createChild("Child A (Early Support)", educationCost, CHILD_A_ID, LEDGER_A_ID);

        adminCommands.recordEarlySupport(CHILD_A_ID, previousMonth, educationCost, UUID.nameUUIDFromBytes("LE-A-1".getBytes()));
        adminCommands.recordEarlySupport(CHILD_A_ID, currentMonth, educationCost, UUID.nameUUIDFromBytes("LE-A-2".getBytes()));

        adminCommands.addProgress(CHILD_A_ID, previousMonth, "Attended classes and received materials.", UUID.nameUUIDFromBytes("PU-A-1".getBytes()));
        adminCommands.addProgress(CHILD_A_ID, currentMonth, "Continuing studies with early support.", UUID.nameUUIDFromBytes("PU-A-2".getBytes()));
    }

    private void seedChildB(YearMonthValue previousMonth, YearMonthValue nextMonth, YearMonthValue currentMonth, Money educationCost) {
        var createResult = adminCommands.createChild("Child B (Sponsored)", educationCost, CHILD_B_ID, LEDGER_B_ID);

        adminCommands.recordEarlySupport(CHILD_B_ID, previousMonth, educationCost, UUID.nameUUIDFromBytes("LE-B-1".getBytes()));
        adminCommands.recordEarlySupport(CHILD_B_ID, currentMonth, educationCost, UUID.nameUUIDFromBytes("LE-B-2".getBytes()));

        adminCommands.addProgress(CHILD_B_ID, previousMonth, "Prepared for upcoming sponsorship.", UUID.nameUUIDFromBytes("PU-B-1".getBytes()));
        adminCommands.addProgress(CHILD_B_ID, currentMonth, "Classes underway, sponsorship planned next month.", UUID.nameUUIDFromBytes("PU-B-2".getBytes()));

        adminCommands.commitSponsorship(SPONSOR_ID, CHILD_B_ID, nextMonth, UUID.nameUUIDFromBytes("SP-1".getBytes()));
    }
}
