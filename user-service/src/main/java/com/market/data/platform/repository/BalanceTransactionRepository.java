package com.market.data.platform.repository;

import com.market.data.platform.model.BalanceTransaction;
import com.market.data.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    BalanceTransaction findByUserId(Long userId);

    Long user(User user);
}