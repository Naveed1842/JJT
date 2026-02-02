package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.value.YearMonthValue;

import java.time.YearMonth;

public final class YearMonthMapper {

    private YearMonthMapper() {}

    public static String toString(YearMonthValue value) {
        return value.getValue().toString(); // YYYY-MM
    }

    public static YearMonthValue toDomain(String stored) {
        return YearMonthValue.of(YearMonth.parse(stored));
    }
}
