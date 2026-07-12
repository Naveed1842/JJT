package com.jjt.platform.core.domain;

import com.jjt.platform.core.domain.value.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private static final Currency PKR = Currency.getInstance("PKR");

    @Test
    void of_normalizesToTwoDecimalPlaces() {
        Money m = Money.of(new BigDecimal("2000"), PKR);
        assertEquals(new BigDecimal("2000.00"), m.getAmount());
    }

    @Test
    void of_stringFactory_parsesCorrectly() {
        Money m = Money.of("1500.00", "PKR");
        assertEquals(new BigDecimal("1500.00"), m.getAmount());
        assertEquals(PKR, m.getCurrency());
    }

    @Test
    void add_sumsSameCurrency() {
        Money a = Money.of("1000.00", "PKR");
        Money b = Money.of("500.00", "PKR");
        assertEquals(Money.of("1500.00", "PKR"), a.add(b));
    }

    @Test
    void add_rejectsDifferentCurrencies() {
        Money pkr = Money.of("1000.00", "PKR");
        Money usd = Money.of("10.00", "USD");
        assertThrows(IllegalArgumentException.class, () -> pkr.add(usd));
    }

    @Test
    void isPositiveOrZero_trueForZero() {
        assertTrue(Money.of("0.00", "PKR").isPositiveOrZero());
    }

    @Test
    void isPositiveOrZero_falseForNegative() {
        assertFalse(Money.of(new BigDecimal("-1.00"), PKR).isPositiveOrZero());
    }

    @Test
    void equality_sameCurrencyAndAmount() {
        assertEquals(Money.of("2000.00", "PKR"), Money.of("2000.00", "PKR"));
    }

    @Test
    void equality_differentAmountNotEqual() {
        assertNotEquals(Money.of("2000.00", "PKR"), Money.of("1999.00", "PKR"));
    }

    @Test
    void equality_differentCurrencyNotEqual() {
        assertNotEquals(Money.of("2000.00", "PKR"), Money.of("2000.00", "USD"));
    }

    @Test
    void of_rejectsMoreThanTwoDecimalPlaces() {
        assertThrows(ArithmeticException.class,
                () -> Money.of(new BigDecimal("1.123"), PKR));
    }

    @Test
    void of_rejectsNullAmount() {
        assertThrows(NullPointerException.class, () -> Money.of(null, PKR));
    }
}
