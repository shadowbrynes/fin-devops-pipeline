package com.easybank.loanservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class repayResponseDto {

    private String mobileNumber;

    private String loanType;

    private BigDecimal loanAmount;

    private String loanNumber;

    private BigDecimal amountPaid;

    private BigDecimal outstandingAmount;

    private String status;

    private String message;
}


