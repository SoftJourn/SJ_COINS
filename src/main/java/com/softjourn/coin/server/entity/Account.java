package com.softjourn.coin.server.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.service.FilterIgnore;
import com.softjourn.coin.server.util.JsonViews;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Data
public class Account {

    @Id
    @JsonView(JsonViews.COINS_MANAGER.class)
    private String ldapId;

    @Transient
    @JsonView({JsonViews.REGULAR.class, JsonViews.COINS_MANAGER.class})
    @FilterIgnore
    private BigDecimal amount;

    @JsonView(JsonViews.COINS_MANAGER.class)
    private String fullName;

    private String email;

    @JsonView(JsonViews.REGULAR.class)
    @FilterIgnore
    private String image;

    @Enumerated(EnumType.STRING)
    @FilterIgnore
    private AccountType accountType;

    @JsonView({JsonViews.COINS_MANAGER.class})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isNew;

    private boolean deleted;

    public Account(String ldapId, String email, BigDecimal amount) {
        this.amount = amount;
        this.email = email;
        this.ldapId = ldapId;
    }

    @JsonView({JsonViews.REGULAR.class, JsonViews.ADMIN.class})
    public String getName() {
        if (fullName == null) return "";
        String[] splitted = fullName.split("\\s");
        return splitted.length > 0 ? splitted[0] : "";
    }

    @JsonView(JsonViews.REGULAR.class)
    public String getSurname() {
        if (fullName == null) return "";
        String[] splitted = fullName.split("\\s");
        return splitted.length > 1 ? splitted[1] : "";
    }

    @JsonProperty(value = "ldapName", access = JsonProperty.Access.WRITE_ONLY)
    public void setLdapName(String ldapName) {
        ldapId = ldapName;
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
