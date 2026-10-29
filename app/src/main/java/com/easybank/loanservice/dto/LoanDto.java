package com.easybank.loanservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanDto {

    private String mobileNumber;

    private String loanType;

    private BigDecimal loanAmount;

    private String loanNumber;

    private BigDecimal amountPaid;

    private BigDecimal outstandingAmount;

}


