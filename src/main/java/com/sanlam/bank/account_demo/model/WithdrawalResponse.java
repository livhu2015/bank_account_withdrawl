package com.sanlam.bank.account_demo.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponse {
    private Boolean isValid;
    private List<BigDecimal> notes;
}
