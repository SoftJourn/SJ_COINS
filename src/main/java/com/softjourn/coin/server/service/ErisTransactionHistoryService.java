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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ErisTransactionService created for managing transactions from blockchain
 * Created by vromanchuk on 23.01.17.
 */
@Service
@Slf4j
public class ErisTransactionHistoryService {

    private final String DEPLOY_FUNCTION_NAME = "DEPLOY";
    private final ErisTransactionRepository erisTransactionRepository;
    private final ContractService contractService;

    @Autowired
    public ErisTransactionHistoryService(@Qualifier("erisTransactionRepository") ErisTransactionRepository erisTransactionRepository
            , @Qualifier("contractServiceImpl") ContractService contractService) {
        this.erisTransactionRepository = erisTransactionRepository;
        this.contractService = contractService;
    }

    public List<TransactionStoring> getTransactionStoring(Block block) {
        Header header = block.getHeader();
        return block.getData().getErisTransactions().stream()
                .map(transaction -> getTransactionStoring(transaction, header.getHeight(), header.getDateTime(), header.getChainId()))
                .collect(Collectors.toList());
    }

    public TransactionStoring storeTransaction(TransactionStoring transaction) {
        try {
            return erisTransactionRepository.save(transaction);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public void storeTransaction(Stream<TransactionStoring> transaction) {
        transaction.forEach(this::storeTransaction);
    }

    public void storeTransaction(List<TransactionStoring> transaction) {
        storeTransaction(transaction.stream());
    }

    public ContractUnit getContractUnit(ErisTransaction transaction) {
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

    public Map<String, String> getCallingData(ErisTransaction transaction, ContractUnit unit) {
        return transaction.parseCallingData(unit);
    }

    public TransactionStoring getTransactionStoring(ErisTransaction transaction, Long blockNumber
            , LocalDateTime time, String chainId) {
        TransactionStoring transactionStoring = null;
        if (!transaction.getIsDeploy()) {
            try {
                ContractUnit unit = this.getContractUnit(transaction);
                if (unit != null) {
                    transactionStoring = new TransactionStoring(blockNumber, time, unit.getName(), chainId
                            , new ErisTransactionDAO(transaction)
                            , this.getCallingData(transaction, unit));
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {

            transactionStoring = new TransactionStoring();
            transactionStoring.setTransaction(transaction);
            transactionStoring.setBlockNumber(blockNumber);
            transactionStoring.setTime(time);
            transactionStoring.setChainId(chainId);
            transactionStoring.setFunctionName(DEPLOY_FUNCTION_NAME);
        }
        return transactionStoring;
    }

    public List<TransactionStoring> getTransactionStoring(List<Block> blocks) {
        return blocks.stream().map(this::getTransactionStoring)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Long getHeightLastStored() {
        if (erisTransactionRepository.count() < 1)
            return 0L;
        TransactionStoring transactionStoring = erisTransactionRepository.findFirstByOrderByBlockNumberDesc();
        if (transactionStoring == null)
            return 0L;
        else
            return transactionStoring.getBlockNumber();
    }
}
