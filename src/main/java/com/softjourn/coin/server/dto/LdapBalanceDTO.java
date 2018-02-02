package com.softjourn.coin.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LdapBalanceDTO {

    private String ldap;
    private BigDecimal balance;

}
