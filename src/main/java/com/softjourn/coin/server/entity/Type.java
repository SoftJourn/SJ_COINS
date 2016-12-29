package com.softjourn.coin.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "contract_type")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Type {

    @Id
    @Column(name = "type")
    private String type;

    @Override
    public String toString() {
        return "Type{" +
                "type='" + type + '\'' +
                '}';
    }
}
