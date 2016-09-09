package com.softjourn.coin.server.service;

import com.softjourn.eris.ErisAccountData;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.ContractManager;
import com.softjourn.eris.rpc.HTTPRPCClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
public class ErisContractService {

    @Value("${eris.chain.url}")
    private String erisChainUrl;

    @Value("${eris.contract.file}")
    private String erisContractFile;

    @Value("${eris.contract.address}")
    private String erisContractAddress;

    private ResourceLoader resourceLoader;

    private ContractManager.ContractBuilder builder;

    @Autowired
    public ErisContractService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    void initContract() {
        try {
            File initFile = resourceLoader.getResource("classpath:" + erisContractFile).getFile();
            builder = new ContractManager(initFile)
                    .contractBuilder()
                    .withChainUrl(erisChainUrl)
                    .withContractAddress(erisContractAddress)
                    .withRPCClient(new HTTPRPCClient());

        } catch (IOException e) {
            throw new RuntimeException("Can't start application. Can't read contract file " + erisContractFile, e);
        }
    }

    public Contract getForAccount(ErisAccountData accountData) {
        return builder.withCallerAccount(accountData).build();
    }
}
