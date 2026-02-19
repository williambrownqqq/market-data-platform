package com.market.data.platform.service;

import com.market.data.platform.dto.response.BalanceTransactionDTO;

import java.math.BigDecimal;

public interface BalanceTransactionService  {

    public BalanceTransactionDTO deposit(Long userId, BigDecimal amount);

    public BalanceTransactionDTO withdraw(Long userId, BigDecimal amount);

    public BigDecimal getBalance(Long userId);

}
