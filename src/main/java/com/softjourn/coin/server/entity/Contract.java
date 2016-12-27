package com.softjourn.coin.server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "contracts")
@NoArgsConstructor
@Data
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    @Lob
    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String abi;

}
