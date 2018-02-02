package com.softjourn.coin.server.dto;

import lombok.Data;

@Data
public class EnrollResponseDTO {

    private Boolean success;

    private String secret;

    private String massage;

    private String token;

}
