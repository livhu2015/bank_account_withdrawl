package com.sanlam.bank.account_demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanlam.bank.account_demo.exception.SnsPublishException;
import com.sanlam.bank.account_demo.model.WithdrawalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.math.BigDecimal;

@Service
public class BankNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BankNotificationService.class);

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.account-id}")
    private String awsAccountId;

    @Value("${aws.topic-name}")
    private String awsTopicName;

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public BankNotificationService(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    public void publishWithdrawalEvent(Long accountId, BigDecimal amount, String status) {
        logger.info("Publishing withdrawal event to SNS ...");
        try {
            WithdrawalEvent event = new WithdrawalEvent(amount, accountId, status);
            String eventJson = objectMapper.writeValueAsString(event);
            String snsTopicArn = "arn:aws:sns:" + awsRegion + ":" + awsAccountId + ":" + awsTopicName;

            PublishRequest publishRequest = PublishRequest.builder()
                    .message(eventJson)
                    .topicArn(snsTopicArn)
                    .build();

            PublishResponse publishResponse = snsClient.publish(publishRequest);

            logger.info("Published withdrawal event to SNS: {}", publishResponse.messageId());

        } catch (SnsPublishException e) {
            logger.error("Failed to publish withdrawal event to SNS: Invalid SNS Topic ARN", e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to publish withdrawal event to SNS");
            throw new RuntimeException("Failed to publish withdrawal event to SNS", e);
        }
    }
}
