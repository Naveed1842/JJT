package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.RecurringDonationSchedule;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.infrastructure.persistence.entity.RecurringDonationScheduleEntity;

import java.util.Currency;

public final class RecurringDonationScheduleMapper {

    private RecurringDonationScheduleMapper() {}

    public static RecurringDonationSchedule toDomain(RecurringDonationScheduleEntity entity) {
        return new RecurringDonationSchedule(
                entity.getId(),
                entity.getOrganisationId(),
                entity.getDonorId(),
                entity.getDonationType(),
                Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                entity.getFrequency(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNextDueDate(),
                entity.getStatus(),
                entity.getFundAccountId(),
                entity.getNotes(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    public static RecurringDonationScheduleEntity toEntity(RecurringDonationSchedule schedule) {
        return new RecurringDonationScheduleEntity(
                schedule.getId(),
                schedule.getOrganisationId(),
                schedule.getDonorId(),
                schedule.getDonationType(),
                schedule.getAmount().getAmount(),
                schedule.getAmount().getCurrency().getCurrencyCode(),
                schedule.getFrequency(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getNextDueDate(),
                schedule.getStatus(),
                schedule.getFundAccountId(),
                schedule.getNotes(),
                schedule.getCreatedBy(),
                schedule.getCreatedAt()
        );
    }
}
