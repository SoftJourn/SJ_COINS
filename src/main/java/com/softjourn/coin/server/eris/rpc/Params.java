package com.softjourn.coin.server.eris.rpc;

import java.util.HashMap;
import java.util.Map;


/**
 * Class to create call params for calling Eris contract methods
 */
public class Params {

    private static final int DEFAULT_GAS_LIMIT = 1000;

    private HashMap<String, Object> params;

    /**
     * Creates call params that should be used for calling contract methods
     * that don't require transaction(i.e. doesn't change state of chain)
     * @param from caller account address
     * @param contractAddress contract address
     * @param data call data (Keccak256 hash of function name and properly formatted arguments)
     * @return map with required parameters
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> constantCallParams(String from, String contractAddress, String data) {
        Params container = new Params();
        container.params.put("from", from);
        container.params.put("address", contractAddress);
        container.params.put("data", data);
        return (Map<String, Object>) container.params.clone();
    }


    /**
     * Creates call params that should be used for transactional calls
     * @param privateKey user's private key
     * @param contractAddress contract address
     * @param data call data (Keccak256 hash of function name and properly formatted arguments)
     * @return map with required parameters
     * TODO this method should be changed when Eris will start supporting signing see https://docs.erisindustries.com/documentation/eris-db-api/#unsafe
     *
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> transactionalCallParams(String privateKey, String contractAddress, String data) {
        Params container = new Params();
        container.params.put("priv_key", privateKey);
        container.params.put("address", contractAddress);
        container.params.put("data", data);
        container.params.put("gas_limit", DEFAULT_GAS_LIMIT);
        return (Map<String, Object>) container.params.clone();
    }

    private Params() {
        this.params = new HashMap<>();
    }

}
