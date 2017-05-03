package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.eris.contract.response.Response;

public interface TransactionMapper {

    Transaction prepareTransaction(Object o, String erisTransactionId, String comment);

    Transaction mapToTransaction(Response response);

}
