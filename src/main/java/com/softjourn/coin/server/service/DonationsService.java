package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.BalancesDTO;
import com.softjourn.coin.server.dto.BatchTransferDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.dto.TransferRequest;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.TransactionRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.entity.TransactionType.*;

@Slf4j
@Service
public class DonationsService {

    @Value("${treasury.account}")
    private String treasuryAccount;
    private TransactionRepository transactionRepository;
    private FabricService fabricService;
    private AccountsService accountsService;
    private Map<String, String> monitors = new HashMap<>();

    @SuppressWarnings("unused")
    @Autowired
    public DonationsService(AccountsService accountsService,
                            FabricService fabricService,
                            TransactionRepository transactionRepository,
                            TransactionMapper mapper) {
        this.fabricService = fabricService;
        this.accountsService = accountsService;
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal getAmount(String projectId) {
        InvokeResponseDTO.Balance balanceOf = fabricService.query(
                treasuryAccount,
                "balanceOf",
                new String[]{"project_", projectId},
                InvokeResponseDTO.Balance.class
        );
        return balanceOf.getPayload().getBalance();
    }

    public BigDecimal getUserAmount(String email) {
        InvokeResponseDTO.Balance balanceOf = fabricService.query(
                email,
                "balanceOf",
                new String[]{"user_", email},
                InvokeResponseDTO.Balance.class
        );
        return balanceOf.getPayload().getBalance();
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Donate to project", type = EXPENSE)
    public Transaction donateToProject(@NonNull String accountName, @NonNull String projectId, @NonNull BigDecimal
            amount, String comment) {
        synchronized (getMonitor(accountName)) {
            Account account = accountsService.getAccount(accountName);
            checkEnoughAmount(account.getEmail(), amount);

            removeIsNewStatus(accountName);

            InvokeResponseDTO.Balance move = move(account.getEmail(), projectId, amount);

            Transaction transaction = new Transaction();

            transaction.setAmount(amount);
            transaction.setAccount(account);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setDestination(null);
            transaction.setTransactionId(move.getTransactionID());
            transaction.setRemain(move.getPayload().getBalance());

            return transaction;
        }
    }

    private void checkEnoughAmount(String accountName, BigDecimal amount) {
        checkAmountIsPositive(amount);

        BigDecimal currentAmount = getUserAmount(accountName);

        if (currentAmount.compareTo(amount) < 0) {
            throw new NotEnoughAmountInAccountException();
        }
    }

    private InvokeResponseDTO.Balance move(String from, String to, BigDecimal amount) {
        return fabricService.invoke(
                from,
                "transfer",
                new String[]{"project_", to, amount.toBigInteger().toString()},
                InvokeResponseDTO.Balance.class
        );
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Move money from project to treasury.", type = TRANSFER)
    public synchronized Transaction closeProject(String projectId) {
        BigDecimal amount = getAmount(projectId);

        checkAmountIsPositive(amount);

        InvokeResponseDTO.Balance refund = fabricService.invoke(
                treasuryAccount,
                "refund",
                new String[]{projectId, treasuryAccount, amount.toBigInteger().toString()},
                InvokeResponseDTO.Balance.class
        );

        Transaction transaction = new Transaction();
        transaction.setTransactionId(refund.getTransactionID());
        transaction.setAccount(null);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRemain(refund.getPayload().getBalance());

        return transaction;
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Move money from project to treasury.", type = TRANSFER)
    public synchronized Transaction refundProject(String projectId, List<BatchTransferDTO> transfers) {
        BigDecimal amount = getAmount(projectId);

        ObjectMapper mapper = new ObjectMapper();
        String values;

        try {
            values = mapper.writeValueAsString(transfers);
        } catch (IOException e) {
            Transaction transaction = new Transaction();
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setComment("Invalid JSON");

            return transaction;
        }

        InvokeResponseDTO.Balance batchRefund = fabricService.invoke(
                treasuryAccount,
                "batchRefund",
                new String[]{projectId, values},
                InvokeResponseDTO.Balance.class
        );

        Transaction transaction = new Transaction();
        transaction.setTransactionId(batchRefund.getTransactionID());
        transaction.setAccount(null);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRemain(batchRefund.getPayload().getBalance());

        return transaction;
    }

    private Account removeIsNewStatus(String ldapId) {
        return Optional
                .ofNullable(accountsService.getAccount(ldapId))
                .filter(Account::isNew)
                .map(account -> accountsService.changeIsNewStatus(false, account))
                .map(accountsService::update)
                .orElseGet(() -> Optional
                        .ofNullable(accountsService.getAccount(ldapId))
                        .orElseThrow(() -> new AccountNotFoundException(ldapId)));
    }

    private void checkAmountIsPositive(@NonNull BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private synchronized String getMonitor(String accountId) {
        if (monitors.containsKey(accountId)) {
            return monitors.get(accountId);
        }

        monitors.put(accountId, accountId);

        return accountId;
    }

}
