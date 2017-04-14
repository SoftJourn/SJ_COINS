package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.dto.ErrorDetail;
import com.softjourn.coin.server.exceptions.*;
import com.softjourn.eris.contract.ContractDeploymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // 409 CONFLICT

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

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NotEnoughAmountInTreasuryException.class)
    public ErrorDetail handleNotEnoughAmountInTreasuryException(NotEnoughAmountInTreasuryException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40905, e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ChequeIsUsedException.class)
    public ErrorDetail handleChequeIsUsedException(ChequeIsUsedException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40906, e.getMessage());
    }

    // 404 NOT FOUND

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Account not found.")
    @ExceptionHandler(AccountNotFoundException.class)
    public void handleNotFound(Exception e) {
        log.warn("Request for not existed account. " + e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ErrorDetail handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40401, String.format("Endpoint %s not found", e.getRequestURL()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ContractDeploymentException.class)
    public ErrorDetail handleContractDeploymentException(ContractDeploymentException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40904, e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ErisContractInstanceNotFound.class)
    public ErrorDetail handleErisContractInstanceNotFound(ErisContractInstanceNotFound e) {
        log.warn(e.getLocalizedMessage());
        return (buildErrorDetails(e, 40405, e.getLocalizedMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ContractNotFoundException.class)
    public ErrorDetail handleContractNotFoundException(ContractNotFoundException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40406, e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TypeNotFoundException.class)
    public ErrorDetail handleTypeNotFoundException(TypeNotFoundException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40407, e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorDetail handleNotFoundException(Exception e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40408, "Record does not exists");
    }

    // 400 BAD REQUEST

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CouldNotReadFileException.class)
    public ErrorDetail handleCouldNotReadFileException(CouldNotReadFileException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40001, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongMimeTypeException.class)
    public ErrorDetail handleWrongMimeTypeException(WrongMimeTypeException e) {
        log.warn(e.getLocalizedMessage());
        return buildErrorDetails(e, 40002, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorDetail handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(e.getLocalizedMessage(), e);
        return buildErrorDetails(e, 40003, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorDetail handle(MethodArgumentNotValidException e) {
        log.info(e.getMessage());
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .filter(Objects::nonNull)
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("");

        return buildErrorDetails(e, null, message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ErisContractNotAllowedToCreate.class)
    public ErrorDetail handle(ErisContractNotAllowedToCreate e) {
        log.info(e.getMessage());
        return buildErrorDetails(e, null, e.getMessage());
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
