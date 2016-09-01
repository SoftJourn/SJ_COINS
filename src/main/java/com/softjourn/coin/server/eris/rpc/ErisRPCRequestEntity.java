package com.softjourn.coin.server.eris.rpc;

import lombok.Getter;

import java.util.Map;

/**
 * RPC entity to call eris contract
 */
@Getter
public class ErisRPCRequestEntity {

    private static final String CONSTANT_CALL_METHOD = "erisdb.call";

    //TODO this value should be changed when Eris will start supporting signing
    // see https://docs.erisindustries.com/documentation/eris-db-api/#unsafe
    private static final String TRANSACTIONAL_CALL_METHOD = "erisdb.transactAndHold";

    private String jsonrpc = "2.0";

    private String method;

    private Map<String, Object> params;


    private ErisRPCRequestEntity(Map<String, Object> params, String method) {
        this.params = params;
        this.method = method;
    }

    /**
     * Creates entity to call method that not requires transaction
     * @param params params to pass to contract method
     * @return RequestEntity
     */
    public static ErisRPCRequestEntity constantCallEntity(Map<String, Object> params) {
        return new ErisRPCRequestEntity(params, CONSTANT_CALL_METHOD);
    }

    /**
     * Creates entity to call method that requires transaction
     * @param params params to pass to contract method
     * @return RequestEntity
     */
    public static ErisRPCRequestEntity transactionalCallEntity(Map<String, Object> params) {
        return new ErisRPCRequestEntity(params, TRANSACTIONAL_CALL_METHOD);
    }

}
