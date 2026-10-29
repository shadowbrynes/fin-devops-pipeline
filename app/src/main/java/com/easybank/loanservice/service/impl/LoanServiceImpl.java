package com.easybank.loanservice.service.impl;

import com.easybank.loanservice.dto.LoanDto;
import com.easybank.loanservice.dto.RepayLoanDto;
import com.easybank.loanservice.dto.repayResponseDto;
import com.easybank.loanservice.entity.Loan;
import com.easybank.loanservice.exception.InvalidLoanAmountException;
import com.easybank.loanservice.exception.LoanNotFoundException;
import com.easybank.loanservice.exception.OutstandingBalanceException;
import com.easybank.loanservice.mapper.LoanMapper;
import com.easybank.loanservice.repository.LoanRepository;
import com.easybank.loanservice.service.ILoanService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class LoanServiceImpl implements ILoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Override
    public void createLoan(LoanDto loanDto) {
        Loan newLoan = LoanMapper.mapToLoan(loanDto, new Loan());
        newLoan.setLoanNumber(generateLoanNumber());
        Loan savedLoan = loanRepository.save(newLoan);
        loanRepository.save(savedLoan);
    }

    private String generateLoanNumber() {
        long randomLoanNumber = 10000 + new Random().nextInt(9000);
        return "easyloan-" + randomLoanNumber;
    } 

    @Override
    public List<LoanDto> fetchLoans(String mobileNumber) {
        List<Loan> loans = loanRepository.findByMobileNumber(mobileNumber);

        List<LoanDto> loanDtos = new ArrayList<>();

        for (Loan loan : loans) {
            LoanDto loanDto = LoanMapper.mapToLoanDto(loan,new LoanDto());
            loanDtos.add(loanDto);
        }

        return loanDtos;
    }

    @Override
    public LoanDto fetchLoan ( @RequestParam String loanNumber){
        Loan loan = loanRepository.findByLoanNumber(loanNumber);
        if (loan == null) {
            throw new LoanNotFoundException("Loan not found for loan number: " + loanNumber);
        }
        return LoanMapper.mapToLoanDto(loan,new LoanDto());
    }

    @Override
@Transactional
public repayResponseDto repayLoan(RepayLoanDto repayLoanDto) {

    BigDecimal repaymentAmount = repayLoanDto.getRepaymentAmount();
    String loanNumber = repayLoanDto.getLoanNumber();

    Loan loan = loanRepository.findByLoanNumber(loanNumber);

    if (loan == null) {
        throw new LoanNotFoundException(
            "Loan not found for loan number: " + loanNumber
        );
    }

    BigDecimal currentAmountPaid =
        loan.getAmountPaid() != null
            ? loan.getAmountPaid()
            : BigDecimal.ZERO;

    BigDecimal outstandingAmount =
        loan.getOutstandingAmount() != null
            ? loan.getOutstandingAmount()
            : loan.getTotalLoan();

    if (outstandingAmount.compareTo(BigDecimal.ZERO) == 0) {
        throw new InvalidLoanAmountException(
            "Loan " + loanNumber + " has already been cleared"
        );
    }

    if (repaymentAmount == null ||
        repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {

        throw new InvalidLoanAmountException(
            "Repayment amount must be greater than zero"
        );
    }

    if (repaymentAmount.compareTo(outstandingAmount) > 0) {
        throw new InvalidLoanAmountException(
            "Repayment amount (" + repaymentAmount +
            ") exceeds outstanding balance (" +
            outstandingAmount + ")"
        );
    }

    BigDecimal newAmountPaid =
        currentAmountPaid.add(repaymentAmount);

    BigDecimal newOutstandingAmount =
        outstandingAmount.subtract(repaymentAmount);

    loan.setAmountPaid(newAmountPaid);
    loan.setOutstandingAmount(newOutstandingAmount);

    Loan savedLoan = loanRepository.save(loan);

    repayResponseDto loanDto = LoanMapper.mapToRepayResponseDto(
        savedLoan,
        new repayResponseDto()
    );

    if (newOutstandingAmount.compareTo(BigDecimal.ZERO) == 0) {

        loanDto.setStatus("CLEARED");

        loanDto.setMessage(
            "Payment of " + repaymentAmount +
            " received successfully. Loan " +
            loanNumber + " is now fully CLEARED!"
        );

    } else {

        loanDto.setStatus("ACTIVE");

        loanDto.setMessage(
            "Payment of " + repaymentAmount +
            " received successfully. Remaining balance: " +
            newOutstandingAmount
        );
    }

    return loanDto;
};

    @Override

    public boolean deleteLoan(String loanNumber) {
        Loan loan = loanRepository.findByLoanNumber (loanNumber);
        if( loan != null && loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0){
            throw new OutstandingBalanceException("You have outstanding balance of " + loan.getOutstandingAmount());
        };
        if(loan != null ){
            loanRepository.deleteByLoanNumber(loanNumber);
            return true;
        }
        return false;
    }

}
