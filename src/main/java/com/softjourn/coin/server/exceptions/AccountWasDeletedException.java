package com.softjourn.coin.server.exceptions;

public class AccountWasDeletedException extends RuntimeException {

  public AccountWasDeletedException(String message) {
    super(message);
  }
}
