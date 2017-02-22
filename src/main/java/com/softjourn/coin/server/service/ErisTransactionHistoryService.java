package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.repository.ErisTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ErisTransactionHistoryService {

    private final ErisTransactionRepository erisTransactionRepository;

    protected ErisTransactionHistoryService(ErisTransactionRepository erisTransactionRepository) {
        this.erisTransactionRepository = erisTransactionRepository;
    }


    public TransactionStoring storeTransaction(TransactionStoring transaction) {
        try {
            return erisTransactionRepository.save(transaction);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    Long getHeightLastStored() {
        if (erisTransactionRepository.count() < 1) {
            return 0L;
        }
        TransactionStoring transactionStoring = erisTransactionRepository.findFirstByOrderByBlockNumberDesc();
        if (transactionStoring == null) {
            return 0L;
        } else {
            return transactionStoring.getBlockNumber()==null?0L:transactionStoring.getBlockNumber();
        }
    }

}
