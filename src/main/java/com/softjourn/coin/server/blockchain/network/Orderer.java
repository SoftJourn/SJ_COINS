package com.softjourn.coin.server.blockchain.network;

import lombok.Data;

@Data
public class Orderer {

    private String name;

    private String url;

    private String certificate;

}
