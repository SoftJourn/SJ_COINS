package com.softjourn.coin.server.config;

import com.softjourn.coin.server.blockchain.network.ChainNetwork;
import com.softjourn.coin.server.blockchain.network.Chaincode;
import com.softjourn.coin.server.blockchain.network.Peer;
import com.softjourn.coin.server.blockchain.network.User;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class FabricChaincodeInstallation implements ApplicationListener<ApplicationReadyEvent> {

    private HFClient client;

    private Channel channel;

    private ChainNetwork chainNetwork;

    @Autowired
    public FabricChaincodeInstallation(HFClient client, Channel channel, ChainNetwork chainNetwork) {
        this.client = client;
        this.channel = channel;
        this.chainNetwork = chainNetwork;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Chaincode chaincode = this.chainNetwork.getChaincode();
        User user = this.chainNetwork.getOrganization().getUser();
        Peer peer = this.chainNetwork.getOrganization().getPeer();

        try {
            if (!chaincodeExists(chaincode, peer)) {
                installChaincode(chaincode, user);
            }
        } catch (InvalidArgumentException | ProposalException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Boolean chaincodeExists(Chaincode chaincode, Peer peer) throws InvalidArgumentException, ProposalException {
        List<Query.ChaincodeInfo> chaincodeInfos = client.queryInstalledChaincodes(client.newPeer(peer.getName(),
                peer.getUrl()));
        for (Query.ChaincodeInfo chaincodeInfo : chaincodeInfos) {
            if (chaincodeInfo.getName().equals(chaincode.getName())
                    && chaincodeInfo.getVersion().equals(chaincode.getVersion())) {
                return true;
            }
        }
        return false;
    }

    private void installChaincode(Chaincode chaincode, User user) throws ExecutionException, InterruptedException, InvalidArgumentException, ProposalException {
        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        InstallProposalRequest installProposalRequest = this.client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeSourceLocation(new File(chaincode.getSourceLocation()));
        installProposalRequest.setChaincodeVersion(chaincode.getVersion());

        ArrayList<ProposalResponse> responses = new ArrayList<>(client.sendInstallProposal(installProposalRequest, channel.getPeers()));

        boolean installationSuccess = isSuccess(responses);

        if (installationSuccess) {
            InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            instantiateProposalRequest.setChaincodeVersion(chaincode.getVersion());
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs(new String[]{user.getName(), "SJ_COINS"});
            instantiateProposalRequest.setTransientMap(prepareInstantiateProposal());

            ArrayList<ProposalResponse> proposalResponses = new ArrayList<>(channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers()));

            boolean instantiationSuccess = isSuccess(proposalResponses);

            if (instantiationSuccess) {
                CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture =
                        channel.sendTransaction(proposalResponses, channel.getOrderers());

                try {
                    transactionEventCompletableFuture.get(5l, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                }
            }
        }
    }

    private boolean isSuccess(ArrayList<ProposalResponse> responses) {
        boolean success = true;
        for (ProposalResponse response : responses) {
            if (response.getStatus().getStatus() != 200) {
                success = false;
            }
        }
        return success;
    }

    private static Map<String, byte[]> prepareInstantiateProposal() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        return tm;
    }
}
