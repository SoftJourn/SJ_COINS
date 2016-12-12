package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.softjourn.coin.server.dto.AccountDTO;
import com.softjourn.coin.server.dto.CheckDTO;
import com.softjourn.coin.server.dto.ResultDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.CouldNotReadFileException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInTreasuryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.util.Util.getDataFromCSV;
import static com.softjourn.coin.server.util.Util.validateMultipartFileMimeType;

@Slf4j
@Service
public class FillAccountsService {

    @Autowired
    @Qualifier("transactionResultMap")
    private Map<String, List<Future<Transaction>>> map;

    @Autowired
    private CoinService coinService;

    public ResultDTO fillAccounts(MultipartFile multipartFile) {
        // is file valid
        validateMultipartFileMimeType(multipartFile, "text/csv");
        // List of future actions
        List<Future<Transaction>> futureTransactions = new ArrayList<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            // reading file content
            MappingIterator<AccountDTO> iterator = getDataFromCSV(multipartFile.getBytes(), AccountDTO.class);
            // get data from read file
            List<AccountDTO> accountsToFill = iterator.readAll();
            // Are enough coins in trasury
            if (!isEnoughInTreasury(BigDecimal.valueOf(accountsToFill.stream()
                    .mapToDouble(value -> value.getCoins().doubleValue()).sum()))) {
                throw new NotEnoughAmountInTreasuryException("Not enough coins in treasury!");
            } else {
                // if enough - filling accounts one by one
                for (AccountDTO dto : accountsToFill) {
                    futureTransactions.add(executorService.submit(planJob(dto)));
                }
                String hash = UUID.randomUUID().toString();
                this.map.put(hash, futureTransactions);
                return new ResultDTO(hash);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new CouldNotReadFileException("File contains mistakes");
        }
    }

    public CheckDTO checkProcessing(String checkHash) {
        long done = map.get(checkHash).stream().filter(Future::isDone).count();
        long total = (long) map.get(checkHash).size();
        CheckDTO dto = new CheckDTO();
        dto.setIsDone(done);
        dto.setTotal(total);
        if (total == done) {
            dto.setTransactions(map.get(checkHash).stream().map(this::getTransaction
            ).collect(Collectors.toList()));
            return dto;
        } else {
            return dto;
        }
    }

    private Callable<Transaction> planJob(AccountDTO accountDTO) {
        return () -> this.coinService.fillAccount(accountDTO.getAccount(), accountDTO.getCoins(),
                String.format("Filling account %s by %.0f coins", accountDTO.getAccount(), accountDTO.getCoins()));
    }

    private Boolean isEnoughInTreasury(BigDecimal decimal) {
        BigDecimal treasuryAmount = this.coinService.getTreasuryAmount();
        return decimal.compareTo(treasuryAmount) < 0;
    }

    private Transaction getTransaction(Future<Transaction> futureTransaction) {
        try {
            return futureTransaction.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getLocalizedMessage());
            Transaction transaction = new Transaction();
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setComment(e.getMessage());
            return transaction;
        }
    }

}
