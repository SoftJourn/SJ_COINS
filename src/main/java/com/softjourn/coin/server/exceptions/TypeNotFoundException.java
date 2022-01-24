package com.softjourn.coin.server.exceptions;

public class TypeNotFoundException extends RuntimeException {

  public TypeNotFoundException(String message) {
    super(message);
  }

  public TypeNotFoundException(Throwable cause) {
    super(cause);
  }
}
