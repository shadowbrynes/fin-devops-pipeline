package com.easybank.loanservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepayLoanDto {
    private BigDecimal repaymentAmount;
    private String loanNumber;
}
