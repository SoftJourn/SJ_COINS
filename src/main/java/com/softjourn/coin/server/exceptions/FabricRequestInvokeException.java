package com.softjourn.coin.server.exceptions;

public class FabricRequestInvokeException extends RuntimeException {

  public FabricRequestInvokeException(String message) {
    super(message);
  }

  public FabricRequestInvokeException(String message, Throwable cause) {
    super(message, cause);
  }

  public FabricRequestInvokeException(Throwable cause) {
    super(cause);
  }
}
