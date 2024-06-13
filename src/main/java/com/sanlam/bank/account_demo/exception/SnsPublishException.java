package com.sanlam.bank.account_demo.exception;

import software.amazon.awssdk.services.sns.model.SnsException;

public class SnsPublishException extends RuntimeException {
    public SnsPublishException(Long accountId) {
        super("Unable to publish message with account ID: " + accountId);
    }
}
