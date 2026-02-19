package com.market.data.platform.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private String symbol;
    private String orderType;
    private Integer quantity;
    private BigDecimal price;
    private String status;
    private LocalDateTime timestamp;

    @Builder.Default
    private String eventType = "NEW_ORDER";
}