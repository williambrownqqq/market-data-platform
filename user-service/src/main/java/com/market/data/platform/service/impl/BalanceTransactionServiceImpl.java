package com.market.data.platform.service.impl;

import com.market.data.platform.dto.response.BalanceTransactionDTO;
import com.market.data.platform.mapper.BalanceTransactionMapper;
import com.market.data.platform.model.BalanceTransaction;
import com.market.data.platform.model.TransactionType;
import com.market.data.platform.model.User;
import com.market.data.platform.repository.BalanceTransactionRepository;
import com.market.data.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.market.data.platform.service.BalanceTransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BalanceTransactionServiceImpl implements BalanceTransactionService {

    private final BalanceTransactionRepository  balanceTransactionRepository;
    private final UserRepository userRepository;
    private final BalanceTransactionMapper  balanceTransactionMapper;

    @Transactional
    public BalanceTransactionDTO deposit(Long userId, BigDecimal amount) {

        User user = userRepository.getById(userId);

        user.setBalance(user.getBalance().add(amount));

        BalanceTransaction transaction = BalanceTransaction.builder()
                .user(user)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        balanceTransactionRepository.save(transaction);

        return balanceTransactionMapper.toDto(transaction);
    }

    @Transactional
    public BalanceTransactionDTO withdraw(Long userId, BigDecimal amount) {

        User user = userRepository.getById(userId);

        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(amount));

        BalanceTransaction transaction = BalanceTransaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAW)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        balanceTransactionRepository.save(transaction);

        return balanceTransactionMapper.toDto(transaction);
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        return balanceTransactionRepository.findByUserId(userId).getAmount();
    }

}
