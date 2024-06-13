package com.sanlam.bank.account_demo.dto;

import lombok.Data;

@Data
public class NotificationRequest extends WithdrawalRequest{
    private String status;
}
