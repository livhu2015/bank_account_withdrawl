package com.sanlam.bank.account_demo.service;

import com.sanlam.bank.account_demo.dto.NotificationRequest;
import com.sanlam.bank.account_demo.dto.WithdrawalRequest;
import com.sanlam.bank.account_demo.exception.AccountNotFoundException;
import com.sanlam.bank.account_demo.exception.InsufficientFundsException;
import com.sanlam.bank.account_demo.exception.InvalidAmountException;
import com.sanlam.bank.account_demo.exception.SnsPublishException;
import com.sanlam.bank.account_demo.model.Account;
import com.sanlam.bank.account_demo.model.WithdrawalResponse;
import com.sanlam.bank.account_demo.repository.AccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BankAccountService {
    private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BankNotificationService bankNotificationService;

    @Transactional
    public WithdrawalResponse withdraw(@Valid WithdrawalRequest request) {
        Long accountId = request.getAccountId();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid withdrawal amount {} for account {}", amount, accountId);
            throw new InvalidAmountException(accountId, amount, "Withdrawal amount must be greater than 0");
        }

        if (amount.remainder(BigDecimal.TEN).compareTo(BigDecimal.ZERO) != 0) {
            logger.warn("Invalid withdrawal amount {} for account {}. Amount must be a multiple of 10", amount, accountId);
            throw new InvalidAmountException(accountId, amount, "Withdrawal amount must be a multiple of 10");
        }

        logger.info("Withdrawal requested for account {} with amount {}", accountId, amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setAccountId(accountId);
        notificationRequest.setAmount(amount);

        try {
            if (amount.compareTo(BigDecimal.ZERO) > 0 && account.getBalance().compareTo(amount) >= 0) {

                account.setBalance(account.getBalance().subtract(amount));
                Account savedAccount = accountRepository.save(account);
                logger.info("Updated account balance for account {}: {}", accountId, savedAccount.getBalance());

                List<BigDecimal> notes = getNotes(amount);

                BigDecimal sumOfNotes = notes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean isValid = sumOfNotes.compareTo(amount) == 0;

                if (isValid) {
                    notificationRequest.setStatus("SUCCESSFUL");
                    sendNotification(notificationRequest);
                }

                return new WithdrawalResponse(isValid, notes);
            } else {
                notificationRequest.setStatus("Insufficient");
                sendNotification(notificationRequest);

                logger.warn("Insufficient funds for withdrawal for account {} with amount {}", accountId, amount);
                logger.warn("Available balance:  {}", account.getBalance());
                throw new InsufficientFundsException(accountId, amount);
            }
        } catch (Exception e) {
            logger.error("Failed to process withdrawal for account {}", accountId);
            throw e;
        }
    }
    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    public BigDecimal checkBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return account.getBalance();
    }

    public void sendNotification(@NotNull NotificationRequest notificationRequest) {
        Long accountId = notificationRequest.getAccountId();
        BigDecimal amount = notificationRequest.getAmount();
        try {
            bankNotificationService.publishWithdrawalEvent(accountId, amount, notificationRequest.getStatus());
            logger.info("Withdrawal event published successfully for account {} with amount {}", accountId, amount);
        } catch (SnsPublishException e) {
            logger.error("SNS publish failed for account {}. Rolling back transaction.", accountId, e);
            throw new RuntimeException("Withdrawal failed due to SNS publish error", e);
        }
    }

    private List<BigDecimal> getNotes(BigDecimal amount) {
        List<BigDecimal> notes = new ArrayList<>();
        BigDecimal[] denominations = {
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10)
        };

        for (BigDecimal denomination : denominations) {
            while (amount.compareTo(denomination) >= 0) {
                notes.add(denomination);
                amount = amount.subtract(denomination);
            }
        }
        return notes;
    }
}
