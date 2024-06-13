package com.sanlam.bank.account_demo.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId, BigDecimal amount) {
        super("Insufficient funds for withdrawal from account ID: " + accountId + " for amount: " + amount);
    }
}