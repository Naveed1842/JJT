package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.value.Money;

import java.math.BigDecimal;
import java.util.Currency;

public final class MoneyMapper {

    private MoneyMapper() {}

    public static Money toDomain(BigDecimal amount, String currency) {
        return Money.of(amount, Currency.getInstance(currency));
    }

    public static BigDecimal amount(Money money) {
        return money.getAmount();
    }

    public static String currency(Money money) {
        return money.getCurrency().getCurrencyCode();
    }
}
