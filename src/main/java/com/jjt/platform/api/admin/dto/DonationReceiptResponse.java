package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DonationReceiptResponse(
        String receiptNumber,
        String donorName,
        String donorEmail,
        String donorPhone,
        BigDecimal amount,
        String currency,
        LocalDate donationDate,
        String donationType,
        String organisationName,
        LocalDate issuedDate
) {}
