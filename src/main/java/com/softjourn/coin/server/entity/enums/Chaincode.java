package com.softjourn.coin.server.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Chaincode {

  COINS("coins"),
  FOUNDATION("foundation");

  private final String name;
}
