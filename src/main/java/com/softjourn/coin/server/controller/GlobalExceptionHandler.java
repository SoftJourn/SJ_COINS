package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.ErrorDetail;
import com.softjourn.coin.server.exceptions.AccountEnrollException;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.ChequeIsUsedException;
import com.softjourn.coin.server.exceptions.CouldNotReadFileException;
import com.softjourn.coin.server.exceptions.FabricRequestInvokeException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInTreasuryException;
import com.softjourn.coin.server.exceptions.NotFoundException;
import com.softjourn.coin.server.exceptions.TypeNotFoundException;
import com.softjourn.coin.server.exceptions.WrongMimeTypeException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

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

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(NotEnoughAmountInTreasuryException.class)
  public ErrorDetail handleNotEnoughAmountInTreasuryException(
      NotEnoughAmountInTreasuryException e
  ) {
    log.warn(e.getLocalizedMessage());
    return buildErrorDetails(e, 40905, e.getMessage());
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ChequeIsUsedException.class)
  public ErrorDetail handleChequeIsUsedException(ChequeIsUsedException e) {
    log.warn(e.getLocalizedMessage());
    return buildErrorDetails(e, 40906, e.getMessage());
  }

  @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Invoke request failed")
  @ExceptionHandler(FabricRequestInvokeException.class)
  public ErrorDetail handleFabricRequestInvokeException(FabricRequestInvokeException e) {
    log.info("Request for invoke. " + e.getLocalizedMessage());
    return buildErrorDetails(e, 40907, e.getMessage());
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(AccountEnrollException.class)
  public ErrorDetail handleContractInstanceNotFound(AccountEnrollException e) {
    log.warn(e.getLocalizedMessage());
    return buildErrorDetails(e, 40908, e.getLocalizedMessage());
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
    return buildErrorDetails(
        e,
        40401,
        String.format("Endpoint %s not found", e.getRequestURL()));
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

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(ReflectiveOperationException.class)
  public ErrorDetail handle(ReflectiveOperationException e) {
    log.info(e.getMessage());

    return buildErrorDetails(e, null, e.getLocalizedMessage());
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
