package com.softjourn.coin.server.service;

import com.softjourn.eris.ErisAccountData;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.ContractManager;
import com.softjourn.eris.contract.event.EventHandler;
import com.softjourn.eris.rpc.RPCClient;
import com.softjourn.eris.rpc.WebSocketRPCClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ErisContractService {

    private ContractManager.ContractBuilder tokenConractBuilder;

    private ContractManager.ContractBuilder offlineVaultContractBuilder;

    @Autowired
    public ErisContractService(ResourceLoader resourceLoader,
                               @Value("${eris.chain.url}") String erisChainUrl,
                               @Value("${eris.token.contract.file}") String tokenContractFile,
                               @Value("${eris.token.contract.address}") String tokenContractAddress,
                               @Value("${eris.offline.contract.file}") String offlineContractFile,
                               @Value("${eris.offline.contract.address}") String offlineContractAddress) {
        try {
            RPCClient rpcClient = new WebSocketRPCClient(erisChainUrl);
            EventHandler eventHandler = new EventHandler(erisChainUrl);

            tokenConractBuilder = new ContractManager()
                    .contractBuilder(resourceLoader.getResource("classpath:" + tokenContractFile).getFile())
                    .withContractAddress(tokenContractAddress)
                    .withEventHandler(eventHandler)
                    .withRPCClient(rpcClient);

            offlineVaultContractBuilder = new ContractManager()
                    .contractBuilder(resourceLoader.getResource("classpath:" + offlineContractFile).getFile())
                    .withContractAddress(offlineContractAddress)
                    .withEventHandler(eventHandler)
                    .withRPCClient(rpcClient);

        } catch (IOException e) {
            throw new RuntimeException("Can't start application. Can't read contract file " + tokenContractFile, e);
        }
    }

    public Contract getTokenContractForAccount(ErisAccountData accountData) {
        return tokenConractBuilder.withCallerAccount(accountData).build();
    }

    public Contract getOfflineContractForAccount(ErisAccountData accountData) {
        return offlineVaultContractBuilder.withCallerAccount(accountData).build();
    }
}
