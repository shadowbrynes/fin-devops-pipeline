package com.easybank.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidLoanAmountException extends RuntimeException {

    public InvalidLoanAmountException(String message) {
        super(message);
    }

}
