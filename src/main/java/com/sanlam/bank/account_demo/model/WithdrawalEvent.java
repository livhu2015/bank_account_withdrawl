package com.sanlam.bank.account_demo.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WithdrawalEvent {
    private BigDecimal amount;
    private Long accountId;
    private String status;
}
