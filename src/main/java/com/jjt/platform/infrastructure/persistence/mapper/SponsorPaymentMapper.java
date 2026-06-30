package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.SponsorPayment;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.infrastructure.persistence.entity.SponsorPaymentEntity;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class SponsorPaymentMapper {

    private SponsorPaymentMapper() {}

    public static SponsorPayment toDomain(SponsorPaymentEntity e) {
        Objects.requireNonNull(e, "entity");
        Money receivedMoney = null;
        if (e.getReceivedAmount() != null && e.getReceivedCurrency() != null) {
            receivedMoney = Money.of(e.getReceivedAmount(), Currency.getInstance(e.getReceivedCurrency()));
        }
        return SponsorPayment.restore(
                e.getId(),
                e.getSponsorshipId(),
                e.getSponsorId(),
                e.getChildId(),
                YearMonthMapper.toDomain(e.getPaymentMonth()),
                e.getStatus(),
                MoneyMapper.toDomain(e.getExpectedAmount(), e.getExpectedCurrency()),
                receivedMoney,
                e.getBankReference(),
                e.getReceivedDate(),
                e.getWaiverReason(),
                e.getFundTransactionId(),
                e.getLedgerEntryId(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getUpdatedBy(),
                e.getUpdatedAt()
        );
    }

    public static SponsorPaymentEntity toEntity(SponsorPayment sp) {
        Objects.requireNonNull(sp, "sponsorPayment");
        BigDecimal receivedAmount = sp.getReceivedAmount() != null ? sp.getReceivedAmount().getAmount() : null;
        String receivedCurrency = sp.getReceivedAmount() != null ? sp.getReceivedAmount().getCurrency().getCurrencyCode() : null;
        return new SponsorPaymentEntity(
                sp.getId(),
                sp.getSponsorshipId(),
                sp.getSponsorId(),
                sp.getChildId(),
                YearMonthMapper.toString(sp.getPaymentMonth()),
                sp.getStatus(),
                sp.getExpectedAmount().getAmount(),
                sp.getExpectedAmount().getCurrency().getCurrencyCode(),
                receivedAmount,
                receivedCurrency,
                sp.getBankReference(),
                sp.getReceivedDate(),
                sp.getWaiverReason(),
                sp.getFundTransactionId(),
                sp.getLedgerEntryId(),
                sp.getCreatedBy(),
                sp.getCreatedAt(),
                sp.getUpdatedBy(),
                sp.getUpdatedAt()
        );
    }
}
