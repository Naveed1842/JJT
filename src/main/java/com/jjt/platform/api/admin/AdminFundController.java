package com.jjt.platform.api.admin;

import com.jjt.platform.api.admin.dto.CreditFundRequest;
import com.jjt.platform.api.admin.dto.FundAccountResponse;
import com.jjt.platform.api.admin.dto.FundBalanceResponse;
import com.jjt.platform.api.admin.dto.FundTransactionResponse;
import com.jjt.platform.api.admin.service.AdminFundService;
import com.jjt.platform.api.admin.service.AdminFundService.FundAccountWithBalance;
import com.jjt.platform.config.security.JwtUserDetails;
import com.jjt.platform.core.domain.entity.FundTransaction;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/funds")
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")
public class AdminFundController {

    private final AdminFundService fundService;

    public AdminFundController(AdminFundService fundService) {
        this.fundService = fundService;
    }

    @GetMapping
    public List<FundAccountResponse> listFunds() {
        return fundService.listFundAccounts().stream()
                .map(this::toFundAccountResponse)
                .toList();
    }

    @GetMapping("/{fundId}/balance")
    public FundBalanceResponse getBalance(@PathVariable("fundId") UUID fundId) {
        FundAccountWithBalance fab = fundService.getFundAccountWithBalance(fundId);
        return new FundBalanceResponse(
                fab.account().getId(),
                fab.account().getName(),
                fab.account().getCurrency(),
                fab.balance(),
                fab.account().getMinReserve(),
                fab.isBelowMinReserve()
        );
    }

    @GetMapping("/{fundId}/transactions")
    public Page<FundTransactionResponse> listTransactions(
            @PathVariable("fundId") UUID fundId,
            @PageableDefault(size = 20) Pageable pageable) {
        return fundService.listTransactions(fundId, pageable)
                .map(this::toTransactionResponse);
    }

    @PostMapping("/{fundId}/credit")
    public ResponseEntity<FundTransactionResponse> credit(
            @PathVariable("fundId") UUID fundId,
            @Valid @RequestBody CreditFundRequest request,
            @AuthenticationPrincipal JwtUserDetails principal) {
        FundTransaction txn = fundService.credit(
                fundId,
                request.amount(),
                request.currency(),
                request.description(),
                request.externalReference(),
                principal.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toTransactionResponse(txn));
    }

    private FundAccountResponse toFundAccountResponse(FundAccountWithBalance fab) {
        return new FundAccountResponse(
                fab.account().getId(),
                fab.account().getName(),
                fab.account().getCurrency(),
                fab.balance(),
                fab.account().getMinReserve(),
                fab.isBelowMinReserve()
        );
    }

    private FundTransactionResponse toTransactionResponse(FundTransaction txn) {
        return new FundTransactionResponse(
                txn.getId(),
                txn.getFundAccountId(),
                txn.getTransactionType().name(),
                txn.getAmount(),
                txn.getCurrency(),
                txn.getReason(),
                txn.getDescription(),
                txn.getExternalReference(),
                txn.getLedgerEntryId(),
                txn.getCreatedBy(),
                txn.getCreatedAt()
        );
    }
}
