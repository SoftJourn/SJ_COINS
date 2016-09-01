package com.softjourn.coin.server.eris.contract;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.softjourn.coin.server.eris.ErisAccountData;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.eris.contract.types.Type;
import com.softjourn.coin.server.eris.rpc.ErisRPCRequestEntity;
import com.softjourn.coin.server.eris.rpc.Params;
import com.softjourn.coin.server.eris.rpc.RPCClient;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contract manager to create contract objects
 * for specified chain url and account
 */
public class ContractManager {

    private final ObjectReader contractUnitReader;

    private final String erisChainUrl;

    private final ErisAccountData accountData;

    public ContractManager(String erisChainUrl, ErisAccountData accountData) {
        this.erisChainUrl = erisChainUrl;
        this.accountData = accountData;

        ObjectMapper mapper = new ObjectMapper();
        contractUnitReader = mapper.readerFor(ContractUnit.class);
    }

    public ContractBuilder parseContract(File contractAbiFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(contractAbiFile))) {
            String contractJson = reader.lines().collect(Collectors.joining("\n"));
            return parseContract(contractJson);
        }
    }

    public ContractBuilder parseContract(String abiJson) throws IOException {
        try {
            HashMap<String, ContractUnit> result = new HashMap<>();
            Iterator<ContractUnit> contractUnitIterator = contractUnitReader.readValues(abiJson);
            while (contractUnitIterator.hasNext()) {
                ContractUnit contractUnit = contractUnitIterator.next();
                if (contractUnit.getType() == ContractUnitType.function)
                    result.put(contractUnit.getName(), contractUnit);
            }
            return new ContractBuilder(result);
        } catch (IOException e) {
            throw new IOException("Can't read ABI file due to exception", e);
        }
    }

    public class ContractBuilder {

        private String contractAddress;

        private RPCClient client;

        private final HashMap<String, ContractUnit> contractUnits;

        ContractBuilder(HashMap<String, ContractUnit> contractUnits) {
            this.contractUnits = contractUnits;
        }

        public ContractBuilder with(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public ContractBuilder with(RPCClient client) {
            this.client = client;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Contract build() {
            return new ContractImpl(contractAddress, client, (Map<String, ContractUnit>) contractUnits.clone());
        }
    }

    class ContractImpl implements Contract, Cloneable {

        private final String contractAddress;

        private final RPCClient client;

        private final Map<String, ContractUnit> contractUnits;

        ContractImpl(String contractAddress, RPCClient client, @NonNull Map<String, ContractUnit> contractUnits) {
            this.contractAddress = contractAddress;
            this.client = client;
            this.contractUnits = contractUnits;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Response<T> call(String function, Object... args) throws IOException {
            Variable[] retVars = contractUnits.get(function).getOutputs();
            ResponseParser parser;
            if (retVars.length == 0) {
                parser = new ResponseParser<>(null);
            } else {
                parser = new ResponseParser<>(retVars[0]);
            }

            return parser.parse(client.call(erisChainUrl, callRPCParams(function, args)));
        }

        ErisRPCRequestEntity callRPCParams(String contractUnitName, Object... args) {
            if (contractUnits.get(contractUnitName).isConstant()) {
                Map<String, Object> params = Params.constantCallParams(accountData.getAddress(), contractAddress, callRPCData(contractUnitName, args));
                return ErisRPCRequestEntity.constantCallEntity(params);
            } else {
                Map<String, Object> params = Params.transactionalCallParams(accountData.getPrivKey(), contractAddress, callRPCData(contractUnitName, args));
                return ErisRPCRequestEntity.transactionalCallEntity(params);
            }
        }

        String callRPCData(String contractUnitName, Object... args) {
            if (!contractUnits.containsKey(contractUnitName)) {
                throw new RuntimeException("ContractImpl haven't function with name " + contractUnitName);
            }

            ContractUnit unit = contractUnits.get(contractUnitName);

            return (unit.signature() + writeArgs(unit, args)).toUpperCase();


        }

        @SuppressWarnings("unchecked")
        private String writeArgs(@NonNull ContractUnit unit, Object... args) {
            if (unit.getInputs().length != args.length) {
                throw new IllegalArgumentException("Count of args in function " + unit.getName() +
                        " is " + unit.getInputs().length +
                        " but was passed " + args.length);
            }
            StringBuilder res = new StringBuilder();

            for (int i = 0; i < args.length; i++) {
                Type type = unit.getInputs()[i].getType();
                if (type.canRepresent(args[i])) {
                    res.append(type.formatInput(args[i]));
                } else {
                    throw new IllegalArgumentException("The " + (i + 1) + "-th parameter of function " + unit.getName() +
                            " is " + type.toString() + " but argument was " + args[i]);
                }
            }

            return res.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContractImpl contract = (ContractImpl) o;

            return contractUnits.equals(contract.contractUnits);

        }

        @Override
        public int hashCode() {
            return contractUnits.hashCode();
        }

    }
}
