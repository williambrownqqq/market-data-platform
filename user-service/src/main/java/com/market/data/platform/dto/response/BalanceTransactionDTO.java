package com.market.data.platform.dto.response;

import com.market.data.platform.model.TransactionType;
import com.market.data.platform.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceTransactionDTO {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime createdAt;
}
