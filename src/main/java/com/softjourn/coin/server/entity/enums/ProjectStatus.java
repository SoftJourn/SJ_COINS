package com.softjourn.coin.server.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProjectStatus {

  REVIEW(1),
  ACTIVE(2),
  DECLINED(3);

  private final int value;
}
