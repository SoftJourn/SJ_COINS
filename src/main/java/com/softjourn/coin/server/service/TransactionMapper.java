package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Transaction;

public interface TransactionMapper {

  Transaction prepareTransaction(Object o, String erisTransactionId, String comment);
}
