package com.softjourn.coin.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * Created by volodymyr on 8/30/16.
 */
@Entity
@Data
@Table(name = "eris")
public class ErisAccount {

    @Id
    @Column
    private String address;

    @Column
    private String pubKey;

    @Column
    private String privKey;

    @Column
    private String type;

    @OneToOne//(cascade=CascadeType)
    private Account account;

}
