package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.ErisCallingData;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.exceptions.ErisContractInstanceNotFound;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import com.softjourn.eris.transaction.type.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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

    public static List<TransactionStoring> getTransactionStoringFromBlock(Block block) {
        return block.getData().getErisTransactions().stream()
                .map(ErisTransactionDAO::new)
                .map(dao -> new TransactionStoring(block.getHeader().getHeight(), block.getHeader().getDateTime(), dao))
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

    public ErisCallingData getCallingData(TransactionStoring transactionStoring) {
        try {
            ErisTransactionDAO transactionDAO = transactionStoring.getTransaction();
            String contractAddress = transactionDAO.getContractAddress();
            System.out.println(contractAddress);
            Contract contract = contractService.getContractsByAddress(contractAddress);
            System.out.println(transactionDAO.parseCallingData(contract.getAbi()));
        } catch (IOException e) {
            throw new ErisProcessingException("Abi isn't correct", e);
        } catch (NullPointerException e) {
            throw new ErisContractInstanceNotFound(e);
        }


        return null;
    }
}
