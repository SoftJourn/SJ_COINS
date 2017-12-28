package com.softjourn.coin.server.chainImpl;

import lombok.Builder;
import org.hyperledger.fabric.sdk.Enrollment;

import java.security.PrivateKey;

@Builder
public class ChainEnrolment implements Enrollment {

    private PrivateKey privateKey;
    private String certificate;

    public ChainEnrolment(PrivateKey privateKey, String certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    @Override
    public PrivateKey getKey() {
        return this.privateKey;
    }

    @Override
    public String getCert() {
        return this.certificate;
    }
}
