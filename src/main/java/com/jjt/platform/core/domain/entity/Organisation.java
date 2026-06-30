package com.jjt.platform.core.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Organisation is the top-level tenant boundary for all business data.
 * JJT is seeded as the first (and currently only) organisation.
 * Every child, sponsor, sponsorship, and ledger entry belongs to exactly one organisation.
 */
public final class Organisation {

    private final UUID id;
    private final String name;
    private final String slug;
    private final String baseCurrency;
    private final int paymentDueDay;
    private final BigDecimal minFundReserve;

    public Organisation(UUID id, String name, String slug, String baseCurrency,
                        int paymentDueDay, BigDecimal minFundReserve) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.slug = Objects.requireNonNull(slug, "slug must not be null");
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency must not be null");
        if (paymentDueDay < 1 || paymentDueDay > 28) {
            throw new IllegalArgumentException("paymentDueDay must be between 1 and 28");
        }
        this.paymentDueDay = paymentDueDay;
        this.minFundReserve = Objects.requireNonNull(minFundReserve, "minFundReserve must not be null");
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getBaseCurrency() { return baseCurrency; }
    public int getPaymentDueDay() { return paymentDueDay; }
    public BigDecimal getMinFundReserve() { return minFundReserve; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Organisation) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
