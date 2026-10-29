package com.easybank.loanservice.service;

import com.easybank.loanservice.dto.LoanDto;
import com.easybank.loanservice.dto.RepayLoanDto;
import com.easybank.loanservice.dto.repayResponseDto;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ILoanService {
    void createLoan(LoanDto loanDto);
    List<LoanDto> fetchLoans(String mobileNumber);
    boolean deleteLoan(String loanNumber);
    LoanDto fetchLoan(String loanNumber);
    @Transactional
    repayResponseDto repayLoan(RepayLoanDto repayLoanDto);
}
