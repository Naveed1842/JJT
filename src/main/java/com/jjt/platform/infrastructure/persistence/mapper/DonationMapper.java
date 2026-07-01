package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Donation;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.infrastructure.persistence.entity.DonationEntity;

import java.util.Currency;

public final class DonationMapper {

    private DonationMapper() {}

    public static Donation toDomain(DonationEntity entity) {
        return new Donation(
                entity.getId(),
                entity.getOrganisationId(),
                entity.getDonorId(),
                entity.getDonationType(),
                Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                entity.getDonationDate(),
                entity.getReceiptNumber(),
                entity.getNotes(),
                entity.getFundAccountId(),
                entity.getFundTransactionId(),
                entity.getStatus(),
                entity.getRecurringScheduleId(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }

    public static DonationEntity toEntity(Donation donation) {
        return new DonationEntity(
                donation.getId(),
                donation.getOrganisationId(),
                donation.getDonorId(),
                donation.getDonationType(),
                donation.getAmount().getAmount(),
                donation.getAmount().getCurrency().getCurrencyCode(),
                donation.getDonationDate(),
                donation.getReceiptNumber(),
                donation.getNotes(),
                donation.getFundAccountId(),
                donation.getFundTransactionId(),
                donation.getStatus(),
                donation.getRecurringScheduleId(),
                donation.getCreatedBy(),
                donation.getCreatedAt(),
                donation.getUpdatedBy(),
                donation.getUpdatedAt()
        );
    }
}
