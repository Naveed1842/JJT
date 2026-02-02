package com.jjt.platform.core.domain.value;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Value object wrapper around {@link YearMonth} to enforce non-null usage in the domain.
 */
public final class YearMonthValue {

    private final YearMonth value;

    private YearMonthValue(YearMonth value) {
        this.value = Objects.requireNonNull(value, "yearMonth must not be null");
    }

    public static YearMonthValue of(int year, int month) {
        return new YearMonthValue(YearMonth.of(year, month));
    }

    public static YearMonthValue of(YearMonth yearMonth) {
        return new YearMonthValue(yearMonth);
    }

    public YearMonth getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YearMonthValue that = (YearMonthValue) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
