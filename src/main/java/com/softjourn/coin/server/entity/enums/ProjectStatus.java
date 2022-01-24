package com.softjourn.coin.server.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProjectStatus {

  DRAFT(1),
  REVIEW(2),
  ACTIVE(4),
  CLOSED(8),
  REJECTED(16);

  private final int value;
}
