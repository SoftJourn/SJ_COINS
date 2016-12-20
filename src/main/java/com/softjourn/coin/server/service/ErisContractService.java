package com.softjourn.coin.server.service;

import com.softjourn.eris.ErisAccountData;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.ContractDeploymentException;
import com.softjourn.eris.contract.ContractManager;
import com.softjourn.eris.contract.event.EventHandler;
import com.softjourn.eris.rpc.HTTPRPCClient;
import com.softjourn.eris.rpc.RPCClient;
import com.softjourn.eris.rpc.WebSocketRPCClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class ErisContractService {

    private ContractManager manager;
    private ErisAccountData erisAccount;
    private RPCClient client;
    private EventHandler eventHandler;

    private ContractManager.ContractBuilder tokenConractBuilder;

    private ContractManager.ContractBuilder offlineVaultContractBuilder;

    @Autowired
    public ErisContractService(ResourceLoader resourceLoader,
                               @Value("${eris.chain.url}") String erisChainUrl,
                               @Value("${eris.token.contract.file}") String tokenContractFile,
                               @Value("${eris.token.contract.address}") String tokenContractAddress,
                               @Value("${eris.offline.contract.file}") String offlineContractFile,
                               @Value("${eris.offline.contract.address}") String offlineContractAddress,
                               @Value("${eris.treasury.account.address}") String treasuryAccountAddress,
                               @Value("${eris.treasury.account.key.public}") String treasuryAccountPubKey,
                               @Value("${eris.treasury.account.key.private}") String treasuryAccountPrivKey) {
        try {
            RPCClient rpcClient = new WebSocketRPCClient(erisChainUrl);
            eventHandler = new EventHandler(erisChainUrl);

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

            erisAccount = new ErisAccountData() {
                @Override
                public String getAddress() {
                    return treasuryAccountAddress;
                }

                @Override
                public String getPubKey() {
                    return treasuryAccountPubKey;
                }

                @Override
                public String getPrivKey() {
                    return treasuryAccountPrivKey;
                }
            };
            manager = new ContractManager();
            client = new HTTPRPCClient(erisChainUrl);

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

    public Contract getContract(String abi, String contractAddress) throws IOException {
        return new ContractManager().contractBuilder(abi)
                .withCallerAccount(erisAccount)
                .withContractAddress(contractAddress)
                .withRPCClient(client)
                .withEventHandler(eventHandler)
                .build();
    }

    public Contract deploy(String code, String abi, List<Object> parameters) {
        try {
            parameters = convert(parameters);
            return manager.contractBuilder(abi)
                    .withCallerAccount(erisAccount)
                    .withRPCClient(client)
                    .withEventHandler(eventHandler)
                    .withSolidityByteCode(code)
                    .buildAndDeploy(parameters.toArray());
        } catch (IOException e) {
            throw new ContractDeploymentException(e);
        }
    }

    private List<Object> convert(List<Object> objects) {
        List<Object> converted = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Integer) {
                converted.add(BigInteger.valueOf((Integer) object));
            } else {
                converted.add(object);
            }
        }
        return converted;
    }

}
