package com.easybank.loanservice.controller;

import com.easybank.loanservice.constants.LoanConstants;
import com.easybank.loanservice.dto.LoanDto;
import com.easybank.loanservice.dto.RepayLoanDto;
import com.easybank.loanservice.dto.repayResponseDto;
import com.easybank.loanservice.dto.ResponseDto;
import com.easybank.loanservice.service.ILoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("api/loan/")

@Tag(
        name ="CRUD REST APIs for Loan service for FinApp application ",
        description = "Rest APIs to create, repay, fetch and delete loans for FinApp banking app."
)
public class LoanController {

    @Autowired
    private ILoanService loanService;

 @Operation(
            summary = "Create Loan REST API",
            description = "REST API to create new Loan inside FinApp"
    )

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(name = "Create loan sample", value = """
                    {
                      "mobileNumber": "08012345678",
                      "loanType": "PERSONAL",
                      "loanAmount": 50000.00
                    }
                    """))
    )
    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createLoan(@RequestBody LoanDto loanDto) {
        loanService.createLoan(loanDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(LoanConstants.MESSAGE_201));
    }

    @Operation(
            summary = "Fetch All Loans REST API",
            description = "REST API to fetch all loans for a given mobile number"
    )
    @GetMapping("fetch-all-loans")
    public ResponseEntity<List<LoanDto>> fetchLoans(@Parameter(description = "Customer mobile number", example = "08012345678") @RequestParam String mobileNumber) {
        List<LoanDto> loanDtos = loanService.fetchLoans(mobileNumber);
        return ResponseEntity.status(HttpStatus.OK).body(loanDtos);
    }

    @Operation(
            summary = "Fetch Loan REST API",
            description = "REST API to fetch a specific loan by its number"
    )
    @GetMapping("fetch-loan")
    public  ResponseEntity<LoanDto> fetchLoan(@Parameter(description = "Loan number", example = "1234567890") @RequestParam String loanNumber){
        LoanDto loanDto = loanService.fetchLoan(loanNumber);
        return  ResponseEntity.status(HttpStatus.OK).body(loanDto);
    }

    @Operation(
            summary = "Delete Loan REST API",
            description = "REST API to delete a specific loan by its number"
    )
    @DeleteMapping("delete")
    public ResponseEntity<ResponseDto> deleteLoan(@Parameter(description = "Loan number", example = "1234567890") @RequestParam String loanNumber) {
        boolean isDeleted = loanService.deleteLoan(loanNumber);
        if(isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(LoanConstants.MESSAGE_200));
        }else{
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(LoanConstants.MESSAGE_417_DELETE));
        }
    }

    @Operation(
            summary = "Repay Loan REST API",
            description = "REST API to repay a specific loan by its number"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(name = "Repay loan sample", value = """
                    {
                      "loanNumber": "1234567890",
                      "repaymentAmount": 10000.00
                    }
                    """))
    )
    @PutMapping("repay-loan")
    public ResponseEntity<repayResponseDto > repayLoan(@RequestBody  RepayLoanDto repayLoanDto){
        repayResponseDto outstandingAmount = loanService.repayLoan(repayLoanDto);
        return ResponseEntity.ok(outstandingAmount);
    }

}
