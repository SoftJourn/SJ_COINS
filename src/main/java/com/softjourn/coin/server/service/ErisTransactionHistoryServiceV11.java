package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.common.functions.OptionalUtil;
import com.softjourn.eris.contract.Util;
import com.softjourn.eris.transaction.pojo.Block;
import com.softjourn.eris.transaction.pojo.BlockData;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.Header;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * ErisTransactionService created for managing transactions from blockchain
 * Created by vromanchuk on 23.01.17.
 */
@Profile({"test", "prod"})
@Service
@Slf4j
public class ErisTransactionHistoryServiceV11 extends ErisTransactionHistoryService {

    @Autowired
    public ErisTransactionHistoryServiceV11(ErisTransactionRepository erisTransactionRepository, ContractService contractService) {
        super(erisTransactionRepository, contractService);
    }

    @Override
    public Stream<TransactionStoring> getTransactionStoring(Block block) {
        Header header = block.getHeader();
        int txCount = Optional.of(block)
                .flatMap(OptionalUtil.nullable(Block::getData))
                .flatMap(OptionalUtil.nullable(BlockData::getErisTransactions))
                .map(List::size)
                .orElse(0);

        return block.getData().getErisTransactions().stream()
                .map(transaction -> getTransactionStoring(transaction, header, txCount))
                .filter(Objects::nonNull);
    }

    @Override
    protected String getTxId(String txJson) {
        return Util.tendermintTransactionV11RipeMd160Hash(txJson.getBytes());
    }

    private TransactionStoring getTransactionStoring(ErisTransaction transaction, Header header, int txCount) {
        return txCount > 1 ? getTxStoringMultiTxInBlock(transaction, header) : getTxStoringOneTxInBlock(transaction, header);
    }

    private TransactionStoring getTxStoringOneTxInBlock(ErisTransaction transaction, Header header) {
        if (!transaction.getIsDeploy()) {
            return getCallTxOneTxInBlock(transaction, header);
        } else {
            return getDeployTx(transaction, header);
        }
    }

    private TransactionStoring getTxStoringMultiTxInBlock(ErisTransaction transaction, Header header) {
        if (!transaction.getIsDeploy()) {
            return getCallTxMultiTxInBlock(transaction, header);
        } else {
            return getDeployTx(transaction, header);
        }
    }

    private TransactionStoring getCallTxMultiTxInBlock(ErisTransaction transaction, Header header) {
        return getTxIfContactPresent(transaction, header, (unit) -> {
            ErisTransactionDAO erisTransactionDAO = new ErisTransactionDAO(transaction);
            Map<String, String> callingData = getCallingData(transaction, unit);
            return new TransactionStoring(header, unit.getName(), erisTransactionDAO, callingData, getTxId(header.getChainId(), erisTransactionDAO));
        });
    }

    private TransactionStoring getCallTxOneTxInBlock(ErisTransaction transaction, Header header) {
        return getTxIfContactPresent(transaction, header, (unit) -> new TransactionStoring(header, unit.getName(), new ErisTransactionDAO(transaction), this.getCallingData(transaction, unit), header.getDataHash()));
    }

}
