package com.jjt.platform.api.admin.service;

import com.jjt.platform.core.domain.entity.FundAccount;
import com.jjt.platform.core.domain.entity.FundTransaction;
import com.jjt.platform.core.domain.entity.FundTransactionType;
import com.jjt.platform.core.domain.exceptions.DomainException;
import com.jjt.platform.core.domain.exceptions.InsufficientFundsException;
import com.jjt.platform.infrastructure.persistence.entity.FundAccountEntity;
import com.jjt.platform.infrastructure.persistence.entity.FundTransactionEntity;
import com.jjt.platform.infrastructure.persistence.mapper.FundAccountMapper;
import com.jjt.platform.infrastructure.persistence.mapper.FundTransactionMapper;
import com.jjt.platform.infrastructure.persistence.repository.FundAccountJpaRepository;
import com.jjt.platform.infrastructure.persistence.repository.FundTransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminFundService {

    private final FundAccountJpaRepository fundAccountRepo;
    private final FundTransactionJpaRepository fundTransactionRepo;

    public AdminFundService(FundAccountJpaRepository fundAccountRepo,
                            FundTransactionJpaRepository fundTransactionRepo) {
        this.fundAccountRepo = fundAccountRepo;
        this.fundTransactionRepo = fundTransactionRepo;
    }

    @Transactional(readOnly = true)
    public List<FundAccountWithBalance> listFundAccounts() {
        return fundAccountRepo.findAll().stream()
                .map(entity -> {
                    BigDecimal balance = fundTransactionRepo.computeBalance(entity.getId());
                    return new FundAccountWithBalance(FundAccountMapper.toDomain(entity), balance);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public FundAccountWithBalance getFundAccountWithBalance(UUID fundAccountId) {
        FundAccountEntity entity = fundAccountRepo.findById(fundAccountId)
                .orElseThrow(() -> new DomainException("Fund account not found"));
        BigDecimal balance = fundTransactionRepo.computeBalance(fundAccountId);
        return new FundAccountWithBalance(FundAccountMapper.toDomain(entity), balance);
    }

    @Transactional(readOnly = true)
    public Optional<FundAccountEntity> findDefaultFundAccount() {
        List<FundAccountEntity> all = fundAccountRepo.findAll();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /**
     * Performs a balance check before debiting. Throws {@link InsufficientFundsException}
     * if balance minus debitAmount would fall below the fund's minReserve.
     * Call this before saving the related ledger entry so the overall transaction rolls back cleanly.
     */
    @Transactional(readOnly = true)
    public void assertSufficientFunds(UUID fundAccountId, BigDecimal debitAmount) {
        FundAccountEntity account = fundAccountRepo.findById(fundAccountId)
                .orElseThrow(() -> new DomainException("Fund account not found"));
        BigDecimal balance = fundTransactionRepo.computeBalance(fundAccountId);
        BigDecimal balanceAfter = balance.subtract(debitAmount);
        if (balanceAfter.compareTo(account.getMinReserve()) < 0) {
            throw new InsufficientFundsException(balance, debitAmount, account.getMinReserve(), account.getCurrency());
        }
    }

    /**
     * Creates a DEBIT transaction linked to an early-support ledger entry.
     * Must be called within the same @Transactional context as the ledger entry save.
     */
    @Transactional
    public FundTransaction debitForEarlySupport(UUID fundAccountId, UUID ledgerEntryId,
                                                BigDecimal amount, String currency, UUID createdBy) {
        FundTransactionEntity entity = new FundTransactionEntity(
                UUID.randomUUID(),
                fundAccountId,
                FundTransactionType.DEBIT,
                amount,
                currency,
                "EARLY_SUPPORT",
                "Auto-debit for early support ledger entry " + ledgerEntryId,
                null,
                ledgerEntryId,
                createdBy,
                Instant.now()
        );
        return FundTransactionMapper.toDomain(fundTransactionRepo.save(entity));
    }

    /**
     * Records a manual CREDIT to the fund (e.g. a cash donation received by admin).
     */
    @Transactional
    public FundTransaction credit(UUID fundAccountId, BigDecimal amount, String currency,
                                  String description, String externalReference, UUID createdBy) {
        if (!fundAccountRepo.existsById(fundAccountId)) {
            throw new DomainException("Fund account not found");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Credit amount must be positive");
        }
        FundTransactionEntity entity = new FundTransactionEntity(
                UUID.randomUUID(),
                fundAccountId,
                FundTransactionType.CREDIT,
                amount,
                currency,
                "MANUAL_CREDIT",
                description,
                externalReference,
                null,
                createdBy,
                Instant.now()
        );
        return FundTransactionMapper.toDomain(fundTransactionRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<FundTransaction> listTransactions(UUID fundAccountId, Pageable pageable) {
        if (!fundAccountRepo.existsById(fundAccountId)) {
            throw new DomainException("Fund account not found");
        }
        return fundTransactionRepo
                .findByFundAccountIdOrderByCreatedAtDesc(fundAccountId, pageable)
                .map(FundTransactionMapper::toDomain);
    }

    public record FundAccountWithBalance(FundAccount account, BigDecimal balance) {
        public boolean isBelowMinReserve() {
            return balance.compareTo(account.getMinReserve()) < 0;
        }
    }
}
