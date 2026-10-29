package com.easybank.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class OutstandingBalanceException extends RuntimeException {

    public OutstandingBalanceException(String message) {
        super(message);
    }

}