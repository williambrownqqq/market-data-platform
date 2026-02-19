package com.market.data.platform.dto.response;

import lombok.Data;

import javax.management.relation.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private BigDecimal balance;
    private Role role;
    private LocalDateTime createdAt;
}


