package com.softjourn.coin.server.blockchain.network;

import lombok.Data;

@Data
public class User {

    private String name;

    private String role;

    private String certificate;

    private String privateKey;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", certificate='" + certificate + '\'' +
                ", privateKey='" + privateKey + '\'' +
                '}';
    }
}
