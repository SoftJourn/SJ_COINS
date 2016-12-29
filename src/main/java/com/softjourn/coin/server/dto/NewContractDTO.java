package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewContractDTO {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String type;

    @NotNull
    @NotBlank
    private String code;

    @NotNull
    @NotBlank
    private String abi;

    private List<Object> parameters;

}
