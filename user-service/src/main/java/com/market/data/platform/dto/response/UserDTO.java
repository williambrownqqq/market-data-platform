package com.market.data.platform.dto.response;

import lombok.*;

import javax.management.relation.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private BigDecimal balance;
    private Role role;
    private LocalDateTime createdAt;
}