package com.sanlam.bank.account_demo.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Account not found for ID: " + accountId);
    }
}