package com.sanlam.bank.account_demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequest {
    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Withdrawal amount must be greater than 0")
    private BigDecimal amount;
}
