package com.softjourn.coin.server.blockchain.network;

import lombok.Data;

@Data
public class Peer {

    private String name;

    private String url;

    private String event;

    private String certificate;

}
