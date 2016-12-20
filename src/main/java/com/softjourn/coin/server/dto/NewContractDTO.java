package com.softjourn.coin.server.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class NewContractDTO {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String code;

    @NotNull
    @NotBlank
    private String abi;

    private List<Object> parameters;

}
