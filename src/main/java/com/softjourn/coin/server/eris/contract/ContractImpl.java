package com.softjourn.coin.server.eris.contract;

import com.softjourn.coin.server.eris.ErisAccountData;
import com.softjourn.coin.server.eris.contract.event.EventHandler;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.eris.contract.types.Type;
import com.softjourn.coin.server.eris.rpc.ErisRPCRequestEntity;
import com.softjourn.coin.server.eris.rpc.Params;
import com.softjourn.coin.server.eris.rpc.RPCClient;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;


class ContractImpl implements Contract, Cloneable {

    private final String contractAddress;

    private final RPCClient client;

    private final Map<String, ContractUnit> contractUnits;

    private final ErisAccountData accountData;

    private final String chainUrl;

    ContractImpl(String contractAddress, RPCClient client, @NonNull Map<String, ContractUnit> contractUnits, ErisAccountData accountData, String chainUrl) {
        this.contractAddress = contractAddress;
        this.client = client;
        this.contractUnits = contractUnits;
        this.accountData = accountData;
        this.chainUrl = chainUrl;
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

        String response = client.call(chainUrl, callRPCParams(function, args));

        return parser.parse(response);
    }

    @Override
    public void subscribeToUserIn(String address, Consumer<Response> callBack) {
        EventHandler handler = new EventHandler();
        Consumer<String> mapping = s -> callBack.accept(new ResponseParser<>(null).apply(s));
        handler.subscribe(chainUrl, constructAccountInEventId(address), mapping);
    }

    private String constructAccountInEventId(String accountAddress) {
        return "Acc/" + accountAddress + "/Input";
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
