package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisContractInstanceNotFound;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.transaction.type.Block;
import com.softjourn.eris.transaction.type.ErisTransaction;
import com.softjourn.eris.transaction.type.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ErisTransactionService created for managing transactions from blockchain
 * Created by vromanchuk on 23.01.17.
 */
@Service
public class ErisTransactionService {

    private final ErisTransactionRepository erisTransactionRepository;
    private final ContractService contractService;

    @Autowired
    public ErisTransactionService(@Qualifier("erisTransactionRepository") ErisTransactionRepository erisTransactionRepository
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
        return erisTransactionRepository.save(transaction);
    }

    public List<TransactionStoring> storeTransaction(List<TransactionStoring> transaction) {
        return transaction.stream()
                .map(this::storeTransaction)
                .collect(Collectors.toList());
    }

    public ContractUnit getContractUnit(ErisTransaction transaction) {
        String contractAddress = transaction.getContractAddress();
        Contract contract = contractService.getContractsByAddress(contractAddress);
        if (contract == null)
            throw new ErisContractInstanceNotFound("Address " + contractAddress);
        try {
            return transaction.getContractUnit(contract.getAbi());
        } catch (IOException e) {
            throw new ErisProcessingException("Abi isn't correct", e);
        }
    }

    public Map<String, String> getCallingData(ErisTransaction transaction, ContractUnit unit) {
        return transaction.parseCallingData(unit);
    }

    public TransactionStoring getTransactionStoring(ErisTransaction transaction, BigInteger blockNumber
            , LocalDateTime time, String chainId) {

        TransactionStoring transactionStoring = new TransactionStoring();
        transactionStoring.setTransaction(transaction);
        ContractUnit unit = this.getContractUnit(transaction);
        transactionStoring.setCallingValue(this.getCallingData(transaction, unit));
        transactionStoring.setFunctionName(unit.getName());
        transactionStoring.setBlockNumber(blockNumber);
        transactionStoring.setTime(time);
        transactionStoring.setChainId(chainId);
        return transactionStoring;
    }

    public List<TransactionStoring> getTransactionStoring(List<Block> blocks) {
        return blocks.stream().map(this::getTransactionStoring)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
