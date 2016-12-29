package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateResponseDTO {

    private Long contractId;
    private String name;
    private String type;
    private String address;

}
