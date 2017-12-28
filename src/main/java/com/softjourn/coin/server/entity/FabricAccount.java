package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.service.FilterIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@Entity
@Data
@Table(name = "eris")
public class FabricAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String certificate;

    @NotNull
    private String pubKey;

    @NotNull
    @FilterIgnore
    private String privKey;

    @OneToOne
    @FilterIgnore
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FabricAccount that = (FabricAccount) o;

        if (!id.equals(that.id)) return false;
        if (!certificate.equals(that.certificate)) return false;
        if (!pubKey.equals(that.pubKey)) return false;
        return privKey.equals(that.privKey);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + certificate.hashCode();
        result = 31 * result + pubKey.hashCode();
        result = 31 * result + privKey.hashCode();
        return result;
    }
}
