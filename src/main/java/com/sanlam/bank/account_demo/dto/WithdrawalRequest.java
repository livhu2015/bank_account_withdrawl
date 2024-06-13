package com.sanlam.bank.account_demo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class WithdrawalRequest implements Serializable {

    private Long accountId;
    private BigDecimal amount;
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
