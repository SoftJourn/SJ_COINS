package com.softjourn.coin.server.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LdapBalanceDTO {

  private String ldap;
  private BigDecimal balance;
}
