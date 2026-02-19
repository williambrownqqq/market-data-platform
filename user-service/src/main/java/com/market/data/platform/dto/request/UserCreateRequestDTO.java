package com.market.data.platform.dto.request;

import com.market.data.platform.model.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Balance can have up to 10 digits and 2 decimal places")
    private BigDecimal balance;

    private Role role;
}