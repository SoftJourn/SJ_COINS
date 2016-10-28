package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
        log.info("Request for transaction with too big amount. " + e.getLocalizedMessage());
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "No free eris account")
    @ExceptionHandler(ErisAccountNotFoundException.class)
    public void handleErisAccountNotFound(Exception e) {
        log.info("Request for assign free eris account. " + e.getLocalizedMessage());
    }
}
