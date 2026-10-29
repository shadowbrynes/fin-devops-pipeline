package com.easybank.loanservice.exception;

import com.easybank.loanservice.dto.ErrorResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception exception,
                                                                  WebRequest webRequest) {
        return buildResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleLoanNotFoundException(LoanNotFoundException exception,
                                                                        WebRequest webRequest) {
        return buildResponse(exception, webRequest, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerAlreadyExistsException(CustomerAlreadyExistsException exception,
                                                                                 WebRequest webRequest) {
        return buildResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidLoanAmountException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidLoanAmountException(InvalidLoanAmountException exception,
                                                                             WebRequest webRequest) {
        return buildResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OutstandingBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleOutstandingBalanceException(OutstandingBalanceException exception,
                                                                              WebRequest webRequest) {
        return buildResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(Exception exception, WebRequest webRequest,
                                                           HttpStatus status) {
        ErrorResponseDto errorResponseDTO = new ErrorResponseDto(
                webRequest.getDescription(false),
                status,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(errorResponseDTO);
    }

}
