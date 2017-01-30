package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewContractInstanceDTO {

    @NotNull(message = "Contract id should not be null!")
    private Long contractId;

    @NotNull(message = "Contract name should not be null!")
    @NotEmpty(message = "Contract name should not be empty!")
    @NotBlank(message = "Contract name should not be blank!")
    private String name;

    @NotNull(message = "Contract parameters should not be null!")
    private List<Object> parameters;

}
