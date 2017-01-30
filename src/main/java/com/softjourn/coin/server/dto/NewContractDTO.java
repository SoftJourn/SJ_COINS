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
public class NewContractDTO {

    @NotNull(message = "Contract name should not be null!")
    @NotBlank(message = "Contract name should not be blank!")
    private String name;

    @NotNull(message = "Contract type should not be null!")
    @NotBlank(message = "Contract type should not be blank!")
    @NotEmpty(message = "Contract type should not be empty!")
    private String type;

    @NotNull(message = "Contract code should not be null!")
    @NotBlank(message = "Contract code should not be blank!")
    @NotEmpty(message = "Contract code should not be empty!")
    private String code;

    @NotNull(message = "Contract abi should not be null!")
    @NotBlank(message = "Contract abi should not be blank!")
    @NotEmpty(message = "Contract abi should not be empty!")
    private String abi;

    @NotNull(message = "Contract parameters should not be null!")
    private List<Object> parameters;

}
