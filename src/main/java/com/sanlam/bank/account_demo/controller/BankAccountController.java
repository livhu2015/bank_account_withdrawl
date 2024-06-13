package com.sanlam.bank.account_demo.controller;

import com.sanlam.bank.account_demo.dto.WithdrawalRequest;
import com.sanlam.bank.account_demo.model.Account;
import com.sanlam.bank.account_demo.model.WithdrawalResponse;
import com.sanlam.bank.account_demo.service.BankAccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/bank")
@Validated
public class BankAccountController {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        logger.info("Depositing amount {} into account with id: {}", amount, id);
        Account updatedAccount = bankAccountService.deposit(id, amount);
        logger.info("Account after deposit: {}", updatedAccount);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/account/withdraw")
    public ResponseEntity<WithdrawalResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        logger.info("Withdrawal request received for account id={}, amount={}", request.getAccountId(), request.getAmount());
        WithdrawalResponse result = bankAccountService.withdraw(request);
        logger.info("Withdrawal result: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> checkBalance(@PathVariable Long accountId) {
        logger.info("Checking balance for account id: {}", accountId);
        BigDecimal balance = bankAccountService.checkBalance(accountId);
        logger.info("Balance for account id {}: {}", accountId, balance);
        return ResponseEntity.ok(balance);
    }
}
