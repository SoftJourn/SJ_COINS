package com.softjourn.coin.server.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "contracts")
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "type")
    private Type type;

    @OneToMany(mappedBy = "contract")
    @JsonManagedReference
    private List<Instance> instances;

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", abi='" + abi + '\'' +
                ", type=" + type +
                ", instances=" + instances +
                '}';
    }
}
