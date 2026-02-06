package com.jjt.platform.api.common.dto;

import java.util.UUID;

public record ChildDto(UUID id,
                       String fullName,
                       String educationAmount,
                       String educationCurrency,
                       AvailabilityStatus availabilityStatus) {
}
