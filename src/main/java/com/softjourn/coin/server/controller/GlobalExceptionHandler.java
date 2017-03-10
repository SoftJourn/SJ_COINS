package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.dto.ErrorDetail;
import com.softjourn.coin.server.exceptions.*;
import com.softjourn.eris.contract.ContractDeploymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Account not found.")
    @ExceptionHandler(AccountNotFoundException.class)
    public void handleNotFound(Exception e) {
        log.warn("Request for not existed account. " + e.getLocalizedMessage());
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Not enough amount of coins oin account.")
    @ExceptionHandler(NotEnoughAmountInAccountException.class)
    public void handleNotEnoughAmount(Exception e) {
        log.info("Request for body with too big amount. " + e.getLocalizedMessage());
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "No free eris account")
    @ExceptionHandler(ErisAccountNotFoundException.class)
    public void handleErisAccountNotFound(Exception e) {
        log.info("Request for assign free eris account. " + e.getLocalizedMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDetail> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(e, 40401, String.format("Endpoint %s not found", e.getRequestURL())));
    }

    @ExceptionHandler(ContractDeploymentException.class)
    public ResponseEntity<ErrorDetail> handleContractDeploymentException(ContractDeploymentException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorDetails(e, 40904, e.getLocalizedMessage()));
    }

    @ExceptionHandler(ErisContractInstanceNotFound.class)
    public ResponseEntity<ErrorDetail> handleErisContractInstanceNotFound(ErisContractInstanceNotFound e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(e, 40405, e.getLocalizedMessage()));
    }

    @ExceptionHandler(ContractNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleContractNotFoundException(ContractNotFoundException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(e, 40406, e.getLocalizedMessage()));
    }

    @ExceptionHandler(TypeNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleTypeNotFoundException(TypeNotFoundException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorDetails(e, 40407, e.getLocalizedMessage()));
    }

    @ExceptionHandler(NotEnoughAmountInTreasuryException.class)
    public ResponseEntity<ErrorDetail> handleNotEnoughAmountInTreasuryException(NotEnoughAmountInTreasuryException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorDetails(e, 40905, e.getMessage()));
    }

    @ExceptionHandler(ChequeIsUsedException.class)
    public ResponseEntity<ErrorDetail> handleChequeIsUsedException(ChequeIsUsedException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorDetails(e, 40906, e.getMessage()));
    }

    @ExceptionHandler(CouldNotReadFileException.class)
    public ResponseEntity<ErrorDetail> handleCouldNotReadFileException(CouldNotReadFileException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(e, 40001, e.getMessage()));
    }

    @ExceptionHandler(WrongMimeTypeException.class)
    public ResponseEntity<ErrorDetail> handleWrongMimeTypeException(WrongMimeTypeException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(e, 40002, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetail> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(e.getLocalizedMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(e, 40003, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handle(MethodArgumentNotValidException e) {
        log.info(e.getMessage());
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .filter(Objects::nonNull)
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorDetails(e, null, message));
    }

    private ErrorDetail buildErrorDetails(Exception e, Integer code, String message) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setTitle("Error");
        errorDetail.setDetail(message);
        errorDetail.setCode(code);
        errorDetail.setDeveloperMessage(e.getClass().getName());
        return errorDetail;
    }

}
