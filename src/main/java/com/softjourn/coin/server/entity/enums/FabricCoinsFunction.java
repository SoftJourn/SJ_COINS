package com.softjourn.coin.server.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FabricCoinsFunction {

  BALANCE_OF("balanceOf"),
  BATCH_TRANSFER("batchTransfer"),
  BATCH_BALANCE_OF("batchBalanceOf"),
  TRANSFER("transfer");

  private final String name;
}
