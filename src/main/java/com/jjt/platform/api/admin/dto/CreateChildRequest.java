package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CreateChildRequest(String rollNumber,
                                 String fullName,
                                 String city,
                                 String campusName,
                                 String schoolName,
                                 String educationAmount,
                                 String educationCurrency,
                                 UUID childId,
                                 UUID ledgerId) {
}
