package com.market.data.platform.controller;

import com.market.data.platform.dto.request.AmountRequestDTO;
import com.market.data.platform.dto.response.BalanceTransactionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.market.data.platform.service.BalanceTransactionService;

import java.math.BigDecimal;

@Tag(name = "User transaction management", description = "Endpoints for managing user transactions")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class BalanceTransactionController {

    private final BalanceTransactionService transactionService;

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get user balance")
    public BigDecimal getBalance(@PathVariable Long id) {
        return transactionService.getBalance(id);
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money")
    public BalanceTransactionDTO deposit(
            @PathVariable Long id,
            @RequestBody @Valid AmountRequestDTO request) {
        return transactionService.deposit(id, request.getAmount());
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money")
    public BalanceTransactionDTO withdraw(
            @PathVariable Long id,
            @RequestBody @Valid AmountRequestDTO request) {
        return transactionService.withdraw(id, request.getAmount());
    }

}
