package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.contract.Util;
import com.softjourn.eris.transaction.pojo.Block;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.Header;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * ErisTransactionService created for managing transactions from blockchain
 * Created by vromanchuk on 23.01.17.
 */
@Profile("dev")
@Service
@Slf4j
public class ErisTransactionHistoryServiceV12 extends ErisTransactionHistoryService {

    @Autowired
    public ErisTransactionHistoryServiceV12(ErisTransactionRepository erisTransactionRepository, ContractService contractService) {
        super(erisTransactionRepository, contractService);
    }

    @Override
    public Stream<TransactionStoring> getTransactionStoring(Block block) {
        Header header = block.getHeader();
        return block.getData().getErisTransactions().stream()
                .map(transaction -> getTransactionStoring(transaction, header))
                .filter(Objects::nonNull);
    }

    @Override
    protected String getTxId(String txJson) {
        return Util.tendermintTransactionV12RipeMd160Hash(txJson.getBytes());
    }

    private TransactionStoring getTransactionStoring(ErisTransaction transaction, Header header) {
        if (!transaction.getIsDeploy()) {
            return getTxIfContactPresent(transaction, header, (unit) -> {
                ErisTransactionDAO erisTransactionDAO = new ErisTransactionDAO(transaction);
                Map<String, String> callingData = getCallingData(transaction, unit);
                String txId = getTxId(header.getChainId(), erisTransactionDAO);
                return new TransactionStoring(header, unit.getName(), erisTransactionDAO, callingData, txId);
            });
        } else {
            return getDeployTx(transaction, header);
        }
    }

}
