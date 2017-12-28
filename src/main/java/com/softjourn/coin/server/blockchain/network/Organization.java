package com.softjourn.coin.server.blockchain.network;

import lombok.Data;

import java.util.List;

@Data
public class Organization {

    private String name;

    private String msp;

    private Peer peer;

    private User user;

}
