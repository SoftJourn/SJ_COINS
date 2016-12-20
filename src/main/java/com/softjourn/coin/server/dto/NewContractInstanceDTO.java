package com.softjourn.coin.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewContractInstanceDTO {

    private Long contractId;

    private List<Object> parameters;

}
