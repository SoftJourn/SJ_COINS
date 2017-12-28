package com.softjourn.coin.server.blockchain.network;

import lombok.Data;

@Data
public class Chaincode {

    private String name;

    private String version;

    private String pathToFile;

    private String sourceLocation;

}
