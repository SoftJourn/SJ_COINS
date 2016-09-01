package com.softjourn.coin.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * Created by volodymyr on 8/30/16.
 */
@Entity
@Data
@Table(name = "eris",uniqueConstraints=
@UniqueConstraint(columnNames = {"type", "account_ldap_id"}))
public class ErisAccount {

    @Id
    @Column
    private String address;

    @Column
    private String pubKey;

    @Column
    private String privKey;

    @Column
    private ErisType type;

    @OneToOne//(cascade=CascadeType)
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErisAccount account = (ErisAccount) o;

        if(address.equals(account.getAddress())&&
                pubKey.equals(account.getPubKey())&&
                privKey.equals(account.getPrivKey())&&
                type.equals(account.getType())&&
                account.equals(account.getAccount()))
            return true;

        return false;
    }
    @Override
    public int hashCode(){
        int hash=17;
        hash= hash*31 + address.hashCode();
        hash= hash*13 + (account == null ? 0 : account.hashCode());
        return hash;
    }
    @Override
    public String toString(){
        return "Account: "+(account==null?"null":account.getLdapId())+
                ",Address: "+address+
                ",Public key: "+pubKey+
                ",Private key: "+privKey+
                ",Type: "+type;

    }

}
