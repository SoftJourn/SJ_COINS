package com.softjourn.coin.server.eris.contract;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.softjourn.coin.server.eris.ErisAccountData;
import com.softjourn.coin.server.eris.rpc.RPCClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.eris.contract.ContractUnitType.*;

/**
 * Contract manager to create contract objects
 * for specified chain url and account
 */
public class ContractManager {

    private final ObjectReader contractUnitReader;

    private final String contractAbiString;

    public ContractManager(File contractAbiFile) throws IOException {
        this(readContract(contractAbiFile));
    }

    public ContractManager(String contractAbiString) {
        this.contractAbiString = contractAbiString;

        ObjectMapper mapper = new ObjectMapper();
        contractUnitReader = mapper.readerFor(ContractUnit.class);
    }

    public ContractBuilder contractBuilder() throws IOException {
        return parseContract(contractAbiString);
    }

    private static String readContract(File contractAbiFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(contractAbiFile))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    ContractBuilder parseContract(String abiJson) throws IOException {
        try {
            HashMap<String, ContractUnit> result = new HashMap<>();
            Iterator<ContractUnit> contractUnitIterator = contractUnitReader.readValues(abiJson);
            while (contractUnitIterator.hasNext()) {
                ContractUnit contractUnit = contractUnitIterator.next();
                if (contractUnit.getType() == function || contractUnit.getType() == event)
                    result.put(contractUnit.getName(), contractUnit);
            }
            return new ContractBuilder(result);
        } catch (IOException e) {
            throw new IOException("Can't read ABI file due to exception", e);
        }
    }

    public class ContractBuilder {

        private ErisAccountData accountData;

        private String contractAddress;

        private RPCClient client;

        private String chainUrl;

        private final HashMap<String, ContractUnit> contractUnits;

        ContractBuilder(HashMap<String, ContractUnit> contractUnits) {
            this.contractUnits = contractUnits;
        }

        public ContractBuilder withContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public ContractBuilder withRPCClient(RPCClient client) {
            this.client = client;
            return this;
        }

        public ContractBuilder withChainUrl(String chainUrl) {
            this.chainUrl = chainUrl;
            return this;
        }

        public ContractBuilder withCallerAccount(ErisAccountData accountData) {
            this.accountData = accountData;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Contract build() {
            return new ContractImpl(contractAddress, client, (Map<String, ContractUnit>) contractUnits.clone(), accountData, chainUrl);
        }
    }

}
