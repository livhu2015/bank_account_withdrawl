package com.sanlam.bank.account_demo.controller;

import com.sanlam.bank.account_demo.dto.WithdrawalRequest;
import com.sanlam.bank.account_demo.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/withdraw")
    public String withdraw(WithdrawalRequest request) {
        return bankAccountService.withdraw(request);
    }
}
