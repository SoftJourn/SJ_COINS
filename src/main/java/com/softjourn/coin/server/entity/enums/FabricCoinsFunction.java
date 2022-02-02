package com.softjourn.coin.server.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FabricCoinsFunction implements ChaincodeFunction {

  BALANCE_OF("balanceOf"),
  BATCH_TRANSFER("batchTransfer"),
  BATCH_BALANCE_OF("batchBalanceOf"),
  TRANSFER("transfer"),
  REFUND("refund"),
  BATCH_REFUND("batchRefund");

  private final String name;
}
