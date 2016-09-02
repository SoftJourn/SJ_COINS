package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.eris.ErisAccountData;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Data
@Table(name = "eris",uniqueConstraints=
@UniqueConstraint(columnNames = {"type", "account_ldap_id"}))
public class ErisAccount implements ErisAccountData {

    @Id
    @NotNull
    @Column
    private String address;

    @Column
    @NotNull
    private String pubKey;

    @Column
    @NotNull
    private String privKey;

    @Column
    @NotNull
    private ErisAccountType type;

    @OneToOne//(cascade=CascadeType)
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ErisAccount eris = (ErisAccount) o;

        return address.equals(eris.getAddress())&&
                pubKey.equals(eris.getPubKey())&&
                privKey.equals(eris.getPrivKey())&&
                type.equals(eris.getType());
    }

    @Override
    public int hashCode(){
        int hash=17;
        hash= hash*31 + address.hashCode();
        hash= hash*31 + pubKey.hashCode();
        hash= hash*31 + privKey.hashCode();
        hash+= type.hashCode();
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
