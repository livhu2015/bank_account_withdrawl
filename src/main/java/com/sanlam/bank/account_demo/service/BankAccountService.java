package com.sanlam.bank.account_demo.service;

import java.math.BigDecimal;
import com.sanlam.bank.account_demo.dto.WithdrawalRequest;
import com.sanlam.bank.account_demo.model.WithdrawalEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BankAccountService {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SnsClient snsClient;

    private String snsTopicArn = "arn:aws:sns:YOUR_REGION:YOUR_ACCOUNT_ID:YOUR_TOPIC_NAME";

    @Transactional
    public String withdraw(WithdrawalRequest request) {
        Long accountId = request.getAccountId();
        BigDecimal amount = request.getAmount();

        String sqlSelect = "SELECT balance FROM accounts WHERE id = ?";
        BigDecimal currentBalance = jdbcTemplate.queryForObject(sqlSelect, new Object[]{accountId}, BigDecimal.class);

        if (currentBalance != null && currentBalance.compareTo(amount) >= 0) {
            String sqlUpdate = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sqlUpdate, amount, accountId);

            if (rowsAffected > 0) {
                publishWithdrawalEvent(amount, accountId, "SUCCESSFUL");
                return "Withdrawal successful";
            } else {
                return "Withdrawal failed";
            }
        } else {
            return "Insufficient funds for withdrawal";
        }
    }

    private void publishWithdrawalEvent(BigDecimal amount, Long accountId, String status) {
        WithdrawalEvent event = new WithdrawalEvent(amount, accountId, status);
        String eventJson = event.toJson();

        PublishRequest publishRequest = PublishRequest.builder()
                .message(eventJson)
                .topicArn(snsTopicArn)
                .build();

        PublishResponse publishResponse = snsClient.publish(publishRequest);
        logger.info("Published withdrawal event: {}", publishResponse.messageId());
    }
}
