package com.softjourn.coin.server.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Data
public class Account {

    @Id
    @Column(name = "ldap_id")
    private String ldapId;

    @Column
    private BigDecimal amount;

    public Account(String ldapId, BigDecimal amount) {
        this.amount = amount;
        this.ldapId = ldapId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return ldapId.equals(account.ldapId);

    }

    @Override
    public int hashCode() {
        return ldapId.hashCode();
    }
}
