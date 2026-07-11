package com.jjt.platform.api.common.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ChildDto(UUID id,
                       String rollNumber,
                       String fullName,
                       String city,
                       String campusName,
                       String schoolName,
                       String educationAmount,
                       String educationCurrency,
                       AvailabilityStatus availabilityStatus,
                       LocalDate enrolledAt) {
}
