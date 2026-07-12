package com.jjt.platform.core.domain;

import com.jjt.platform.core.domain.entity.CommitmentType;
import com.jjt.platform.core.domain.entity.Sponsorship;
import com.jjt.platform.core.domain.entity.SponsorshipStatus;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.value.YearMonthValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SponsorshipTest {

    private static final UUID SPONSOR_ID = UUID.randomUUID();
    private static final UUID CHILD_ID   = UUID.randomUUID();
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final YearMonthValue FUTURE_MONTH =
            YearMonthValue.of(YearMonth.now().plusMonths(1));

    @Test
    void createPending_setsStatusToPending() {
        Sponsorship s = Sponsorship.createPending(
                UUID.randomUUID(), SPONSOR_ID, CHILD_ID,
                FUTURE_MONTH, Instant.now(), CommitmentType.MONTHLY, CREATOR_ID);

        assertEquals(SponsorshipStatus.PENDING, s.getStatus());
    }

    @Test
    void createPending_rejectsCurrentMonth() {
        YearMonthValue currentMonth = YearMonthValue.of(YearMonth.now());

        assertThrows(DomainException.class, () ->
                Sponsorship.createPending(UUID.randomUUID(), SPONSOR_ID, CHILD_ID,
                        currentMonth, Instant.now(), CommitmentType.MONTHLY, CREATOR_ID));
    }

    @Test
    void createPending_rejectsPastMonth() {
        YearMonthValue pastMonth = YearMonthValue.of(YearMonth.now().minusMonths(3));

        assertThrows(DomainException.class, () ->
                Sponsorship.createPending(UUID.randomUUID(), SPONSOR_ID, CHILD_ID,
                        pastMonth, Instant.now(), CommitmentType.MONTHLY, CREATOR_ID));
    }

    @Test
    void withStatus_returnsNewInstanceWithUpdatedStatus() {
        Sponsorship pending = Sponsorship.createPending(
                UUID.randomUUID(), SPONSOR_ID, CHILD_ID,
                FUTURE_MONTH, Instant.now(), CommitmentType.MONTHLY, CREATOR_ID);

        Sponsorship active = pending.withStatus(SponsorshipStatus.ACTIVE);

        assertEquals(SponsorshipStatus.ACTIVE, active.getStatus());
        assertEquals(SponsorshipStatus.PENDING, pending.getStatus()); // original unchanged
        assertEquals(pending.getId(), active.getId()); // same identity
    }

    @Test
    void restore_allowsPastStartMonth() {
        YearMonthValue pastMonth = YearMonthValue.of(YearMonth.now().minusYears(1));

        Sponsorship restored = Sponsorship.restore(
                UUID.randomUUID(), SPONSOR_ID, CHILD_ID,
                pastMonth, SponsorshipStatus.ACTIVE,
                Instant.now(), null, CommitmentType.MONTHLY, null);

        assertEquals(SponsorshipStatus.ACTIVE, restored.getStatus());
        assertEquals(pastMonth, restored.getStartMonth());
    }

    @Test
    void equalityIsBasedOnId() {
        UUID id = UUID.randomUUID();
        Sponsorship a = Sponsorship.restore(id, SPONSOR_ID, CHILD_ID,
                FUTURE_MONTH, SponsorshipStatus.PENDING,
                Instant.now(), null, CommitmentType.MONTHLY, null);
        Sponsorship b = Sponsorship.restore(id, UUID.randomUUID(), UUID.randomUUID(),
                FUTURE_MONTH, SponsorshipStatus.ACTIVE,
                Instant.now(), null, CommitmentType.YEARLY, null);

        assertEquals(a, b);
    }
}
