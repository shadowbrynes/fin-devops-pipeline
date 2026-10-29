package com.easybank.loanservice.mapper;


import com.easybank.loanservice.dto.LoanDto;
import com.easybank.loanservice.dto.repayResponseDto;
import com.easybank.loanservice.entity.Loan;

import java.math.BigDecimal;


public class LoanMapper {

    public static LoanDto mapToLoanDto(Loan loan, LoanDto loanDto) {
        loanDto.setLoanAmount(loan.getLoanAmount());
        loanDto.setLoanType(loan.getLoanType());
        loanDto.setLoanNumber(loan.getLoanNumber());
        loanDto.setMobileNumber(loan.getMobileNumber());
        loanDto.setAmountPaid(loan.getAmountPaid());
        loanDto.setOutstandingAmount(loan.getOutstandingAmount());
        return loanDto;
    }

    public static repayResponseDto mapToRepayResponseDto(Loan loan, repayResponseDto repayResponseDto) {
        repayResponseDto.setLoanAmount(loan.getLoanAmount());
        repayResponseDto.setLoanType(loan.getLoanType());
        repayResponseDto.setLoanNumber(loan.getLoanNumber());
        repayResponseDto.setMobileNumber(loan.getMobileNumber());
        repayResponseDto.setAmountPaid(loan.getAmountPaid());
        repayResponseDto.setOutstandingAmount(loan.getOutstandingAmount());
        return repayResponseDto;
    }




    public static Loan mapToLoan(LoanDto loanDto, Loan loan) {
        loan.setLoanAmount(loanDto.getLoanAmount());
        loan.setLoanType(loanDto.getLoanType());
        loan.setMobileNumber(loanDto.getMobileNumber());
        loan.setAmountPaid(BigDecimal.ZERO);
        loan.setOutstandingAmount(loanDto.getLoanAmount());

        return loan;
    }

}
