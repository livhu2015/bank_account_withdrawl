package com.sanlam.bank.account_demo.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {

    private final Long accountId;
    private final BigDecimal amount;

    public InvalidAmountException(Long accountId, BigDecimal amount, String message) {
        super(message);
        this.accountId = accountId;
        this.amount = amount;
    }
}
