package com.softjourn.coin.server.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FabricFoundationsFunction {

  CREATE("createFoundation"),
  GET_ALL("getFoundations"),
  GET_ONE("getFoundation"),
  DONATE("donate"),
  CLOSE("closeFoundation"),
  SET_ALLOWANCE("setAllowance"),
  WITHDRAW("withdraw");

  private final String name;
}
