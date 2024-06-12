package com.sanlam.bank.account_demo.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@RestController
@RequestMapping("/bank")
public class BankAccountController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private SnsClient snsClient;

    public BankAccountController() {
        this.snsClient = SnsClient.builder().region(Region.EU_WEST_1).build();
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam("accountId") Long accountId, @RequestParam("amount") BigDecimal amount) {
        // Check current balance
        String sqlSelect = "SELECT balance FROM accounts WHERE id = ?";
        BigDecimal currentBalance = jdbcTemplate.queryForObject(sqlSelect, new Object[]{accountId}, BigDecimal.class);

        if (currentBalance != null && currentBalance.compareTo(amount) >= 0) {
            // Update balance
            String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sqlUpdate, amount, accountId);

            if (rowsAffected > 0) {
                // After a successful withdrawal, publish a withdrawal event to SNS
                WithdrawalEvent event = new WithdrawalEvent(amount, accountId, "SUCCESSFUL");
                String eventJson = event.toJson(); // Convert event to JSON
                String snsTopicArn = "arn:aws:sns:YOUR_REGION:YOUR_ACCOUNT_ID:YOUR_TOPIC_NAME";

                PublishRequest publishRequest = PublishRequest.builder()
                        .message(eventJson)
                        .topicArn(snsTopicArn)
                        .build();

                PublishResponse publishResponse = snsClient.publish(publishRequest);
                return "Withdrawal successful";
            } else {
                return "Withdrawal failed";
            }
        } else {
            // Insufficient funds
            return "Insufficient funds for withdrawal";
        }
    }
}

// WithdrawalEvent class
class WithdrawalEvent {
    private BigDecimal amount;
    private Long accountId;
    private String status;

    public WithdrawalEvent(BigDecimal amount, Long accountId, String status) {
        this.amount = amount;
        this.accountId = accountId;
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getStatus() {
        return status;
    }

    public String toJson() {
        return String.format("{\"amount\":\"%s\",\"accountId\":%d,\"status\":\"%s\"}", amount, accountId, status);
    }
}
