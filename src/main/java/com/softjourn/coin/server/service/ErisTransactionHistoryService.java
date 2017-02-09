package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ContractNotFoundException;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.transaction.pojo.Block;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.Header;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;


@Slf4j
public abstract class ErisTransactionHistoryService {

    public final String DEPLOY_FUNCTION_NAME = "DEPLOY";
    public static final int TX_TYPE_CALL = 2;
    protected final ErisTransactionRepository erisTransactionRepository;
    protected final ContractService contractService;

    protected ErisTransactionHistoryService(ErisTransactionRepository erisTransactionRepository, ContractService contractService) {
        this.erisTransactionRepository = erisTransactionRepository;
        this.contractService = contractService;
    }

    public abstract Stream<TransactionStoring> getTransactionStoring(Block block);

    public TransactionStoring storeTransaction(TransactionStoring transaction) {
        try {
            return erisTransactionRepository.save(transaction);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    ContractUnit getContractUnit(ErisTransaction transaction) {
        String contractAddress = transaction.getContractAddress();
        try {
            Contract contract = contractService.getContractsByAddress(contractAddress);
            return transaction.getContractUnit(contract.getAbi());
        } catch (IOException e) {
            log.warn("Abi isn't correct", e);
        } catch (ContractNotFoundException e) {
            log.warn("Unable to get contract", e);
        }
        return null;
    }

    Map<String, String> getCallingData(ErisTransaction transaction, ContractUnit unit) {
        return transaction.parseCallingData(unit);
    }

    TransactionStoring getDeployTx(ErisTransaction transaction, Header header) {
        TransactionStoring transactionStoring = new TransactionStoring();
        transactionStoring.setTransaction(transaction);
        transactionStoring.setBlockNumber(header.getHeight());
        transactionStoring.setTime(header.getDateTime());
        transactionStoring.setChainId(header.getChainId());
        transactionStoring.setFunctionName(DEPLOY_FUNCTION_NAME);
        return transactionStoring;
    }

    TransactionStoring getTxIfContactPresent(ErisTransaction transaction, Header header, Function<ContractUnit, TransactionStoring> txConstructor) {
        try {
            ContractUnit unit = this.getContractUnit(transaction);
            if (unit != null) {
                return txConstructor.apply(unit);
            }
        } catch (RuntimeException e) {
            log.warn("Can't get transactions from block " + header.getHeight(), e);
        }
        return null;
    }

    Stream<TransactionStoring> getTransactionStoring(List<Block> blocks) {
        return blocks.stream()
                .flatMap(this::getTransactionStoring);
    }

    Long getHeightLastStored() {
        if (erisTransactionRepository.count() < 1) {
            return 0L;
        }
        TransactionStoring transactionStoring = erisTransactionRepository.findFirstByOrderByBlockNumberDesc();
        if (transactionStoring == null) {
            return 0L;
        } else {
            return transactionStoring.getBlockNumber();
        }
    }

    private String getTxJson(String chainId, String contractAddress, String txData, long fee, long gasLimit, String txInput) {
        return "{\"chain_id\":\"" + chainId + "\","
                + "\"tx\":[" + TX_TYPE_CALL
                + ",{\"address\":\"" + contractAddress
                + "\",\"data\":\"" + txData
                + "\"," + "\"fee\":"
                + fee + ",\"gas_limit\":"
                + gasLimit + ",\"input\":"
                + txInput + "" + "}]}";
    }

    private String getTxJson(String chainId, ErisTransactionDAO transaction){

        String txInputJson = getTxInputJson(transaction);
        return getTxJson(chainId,transaction.getContractAddress(),transaction.getFunctionNameHash() + transaction.getCallingData()
                ,transaction.getFee(),transaction.getGasLimit(),txInputJson);
    }

    private String getTxInputJson(String userAddress, long amount, long sequence) {
        return "{\"address\":\"" + userAddress + "\",\"amount\":" + amount + ",\"sequence\":" + sequence + "}";
    }

    private String getTxInputJson(ErisTransactionDAO transaction){
        return getTxInputJson(transaction.getCallerAddress(),transaction.getAmount(),transaction.getSequence());
    }

    public String getTxId(String chainId, ErisTransactionDAO transaction){
        String txJson = getTxJson(chainId,transaction);
        return getTxId(txJson);
    }

    protected abstract String getTxId(String txJson);
}
