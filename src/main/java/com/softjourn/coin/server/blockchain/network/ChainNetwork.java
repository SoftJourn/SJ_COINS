package com.softjourn.coin.server.blockchain.network;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("network")
public class ChainNetwork {

    private Orderer orderer;

    private Organization organization;

    private Channel channel;

    private Chaincode chaincode;

    private String endorsmentPolicy;

}
