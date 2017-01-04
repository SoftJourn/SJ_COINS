package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.AccountFillDTO;
import com.softjourn.coin.server.dto.CheckDTO;
import com.softjourn.coin.server.dto.ResultDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.CouldNotReadFileException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInTreasuryException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.util.Util.dataToCSV;
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

    @Autowired
    private AccountsService accountsService;

    public ResultDTO fillAccounts(MultipartFile multipartFile) {
        // is file valid
        validateMultipartFileMimeType(multipartFile, "text/csv|application/vnd.ms-excel");
        // List of future actions
        List<Future<Transaction>> futureTransactions = new ArrayList<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            // reading file content
            MappingIterator<AccountFillDTO> iterator = getDataFromCSV(multipartFile.getBytes(), AccountFillDTO.class);
            // get data from read file
            List<AccountFillDTO> accountsToFill = iterator.readAll();
            // Check if all values are positive
            this.checkAmountIsPositive(accountsToFill);
            // Are enough coins in treasury
            if (!isEnoughInTreasury(BigDecimal.valueOf(accountsToFill.stream()
                    .mapToDouble(value -> value.getCoins().doubleValue()).sum()))) {
                throw new NotEnoughAmountInTreasuryException("Not enough coins in treasury!");
            } else {
                // if enough - filling accounts one by one
                for (AccountFillDTO dto : accountsToFill) {
                    checkAmountIsPositive(dto.getCoins());
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
            dto.setTransactions(map.get(checkHash).stream().map(this::getTransaction).collect(Collectors.toList()));
            this.cleanTransactionResultMap(checkHash);
            return dto;
        } else {
            return dto;
        }
    }

    public void getAccountDTOTemplate(Writer writer) throws IOException {
        List<Account> accounts = this.accountsService.getAll(AccountType.REGULAR);
        List<AccountFillDTO> collect = accounts.stream().map((Account a) -> {
            AccountFillDTO dto = new AccountFillDTO();
            dto.setAccount(a.getLdapId());
            dto.setFullName(a.getFullName());
            dto.setCoins(new BigDecimal(0));
            return dto;
        }).sorted(Comparator.comparing(AccountFillDTO::getAccount)).collect(Collectors.toList());
        dataToCSV(writer, collect, AccountFillDTO.class);
    }

    @SaveTransaction(comment = "Move money from treasury to account.")
    private Callable<Transaction> planJob(AccountFillDTO accountDTO) {
        return () -> this.coinService.fillAccount(accountDTO.getAccount(), accountDTO.getCoins(),
                String.format("Filling account %s by %.0f coins", accountDTO.getAccount(), accountDTO.getCoins()));
    }

    private Boolean isEnoughInTreasury(BigDecimal decimal) {
        BigDecimal treasuryAmount = this.coinService.getTreasuryAmount();
        return decimal.compareTo(treasuryAmount) < 0;
    }

    private void checkAmountIsPositive(List<AccountFillDTO> accountDTOs) {
        accountDTOs.forEach(accountDTO -> this.checkAmountIsPositive(accountDTO.getCoins()));
    }

    private void checkAmountIsPositive(@NonNull BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private Transaction getTransaction(Future<Transaction> futureTransaction) {
        try {
            return futureTransaction.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getLocalizedMessage());
            Transaction transaction = new Transaction();
            transaction.setError(e.getLocalizedMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            return transaction;
        }
    }

    private synchronized void cleanTransactionResultMap(String checkHash) {
        Timer timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                map.remove(checkHash);
            }
        };
        timer.schedule(action, 5000);
    }

}
