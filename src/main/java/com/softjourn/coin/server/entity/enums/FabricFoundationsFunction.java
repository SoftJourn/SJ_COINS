package com.softjourn.coin.server.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FabricFoundationsFunction {

  CREATE("createFoundation"),
  GET_ALL("getFoundations"),
  GET_ONE("getFoundationByName"),
  DONATE("donate"),
  CLOSE("closeFoundation"),
  WITHDRAW("withdraw");

  private final String name;
}
