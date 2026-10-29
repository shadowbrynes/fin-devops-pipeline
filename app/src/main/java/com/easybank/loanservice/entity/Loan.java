package com.easybank.loanservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Loan")
@Data
public class Loan extends  BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    private String mobileNumber;

    private String loanNumber;

    private String loanType;

    private BigDecimal loanAmount;

    private BigDecimal totalLoan;

    private BigDecimal amountPaid;

    private BigDecimal outstandingAmount;

}
