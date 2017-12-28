package com.softjourn.coin.server.config;

import com.softjourn.coin.server.blockchain.network.ChainNetwork;
import com.softjourn.coin.server.blockchain.network.Channel;
import com.softjourn.coin.server.blockchain.network.Orderer;
import com.softjourn.coin.server.blockchain.network.Peer;
import com.softjourn.coin.server.chainImpl.ChainUser;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class FabricChannelConfiguration {

    private ChainNetwork chainNetwork;

    private HFClient client;

    @Autowired
    public FabricChannelConfiguration(ChainNetwork chainNetwork,
                                      HFClient client) {
        this.chainNetwork = chainNetwork;
        this.client = client;
    }

    @Bean
    public org.hyperledger.fabric.sdk.Channel channel()
            throws IOException, InvalidArgumentException, TransactionException, ProposalException {
        boolean checkChannel = false;
        String channelName = this.chainNetwork.getChaincode().getName();
        User user = this.chainNetwork.getOrganization().getUser().toChainUser();
        this.client.setUserContext(user);
        Peer peer = this.chainNetwork.getOrganization().getPeer();
        Set<String> channels = this.client.queryChannels(client.newPeer(peer.getName(), peer.getUrl(),
                prepareProperties(peer.getCertificate())));
        for (String channel : channels) {
            if (channel.equals(channelName)) {
                checkChannel = true;
            }
        }
        if (checkChannel) {
            return getChannel();
        } else {
            return createChannelWithPeers();
        }
    }

    private org.hyperledger.fabric.sdk.Channel createChannelWithPeers()
            throws IOException, InvalidArgumentException, TransactionException, ProposalException {
        org.hyperledger.fabric.sdk.Channel channel = createChannel();
        Peer peer = this.chainNetwork.getOrganization().getPeer();
        channel.joinPeer(this.client.newPeer(peer.getName(), peer.getUrl(), prepareProperties(peer.getCertificate())));
        channel.initialize();
        return channel;
    }

    private org.hyperledger.fabric.sdk.Channel createChannel()
            throws IOException, InvalidArgumentException, TransactionException {
        Channel networkChannel = this.chainNetwork.getChannel();
        Orderer orderer = this.chainNetwork.getOrderer();
        ChainUser chainUser = this.chainNetwork.getOrganization().getUser().toChainUser();
        ChannelConfiguration configuration = new ChannelConfiguration(new File(networkChannel.getConfigFile()));
        byte[] channelConfigurationSignature = client.getChannelConfigurationSignature(configuration, chainUser);
        return client.newChannel(networkChannel.getName(),
                client.newOrderer(orderer.getName(), orderer.getUrl(), prepareProperties(orderer.getCertificate())),
                configuration,
                channelConfigurationSignature);
    }

    private org.hyperledger.fabric.sdk.Channel getChannel()
            throws InvalidArgumentException, TransactionException {
        Channel chainChannel = this.chainNetwork.getChannel();
        ChainUser chainUser = this.chainNetwork.getOrganization().getUser().toChainUser();
        Peer peer = this.chainNetwork.getOrganization().getPeer();
        Orderer orderer = this.chainNetwork.getOrderer();
        this.client.setUserContext(chainUser);
        org.hyperledger.fabric.sdk.Channel channel = client.newChannel(chainChannel.getName());
        channel.addPeer(this.client.newPeer(peer.getName(), peer.getUrl(), prepareProperties(peer.getCertificate())));
        channel.addOrderer(client.newOrderer(orderer.getName(), orderer.getUrl(), prepareProperties(orderer.getCertificate())));
        channel.initialize();
        return channel;
    }

    private static Properties prepareProperties(String certificate) {
        File cert = Paths.get(certificate).toFile();
        Properties ret = new Properties();
        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("trustServerCertificate", "true");
        ret.setProperty("negotiationType", "TLS");
        ret.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
        ret.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});
        ret.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[]{true});
        return ret;
    }

}
