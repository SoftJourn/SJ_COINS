package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateDTO {

    private String certificate;

    private String publicKey;

    private String privateKey;

}
