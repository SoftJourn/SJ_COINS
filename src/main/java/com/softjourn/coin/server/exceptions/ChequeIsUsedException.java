package com.softjourn.coin.server.exceptions;

public class ChequeIsUsedException extends RuntimeException {

  public ChequeIsUsedException() {
    super("Such cheque was not issued or already cashed out.");
  }
}
