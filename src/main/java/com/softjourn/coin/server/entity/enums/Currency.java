package com.softjourn.coin.server.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Currency {

  COINS("coins");

  private final String value;
}
