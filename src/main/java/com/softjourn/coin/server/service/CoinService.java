package com.softjourn.coin.server.service;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.BalancesDTO;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.entity.TransactionType.*;

@Slf4j
@Service
public class CoinService {

    private AccountsService accountsService;

    private Map<String, String> monitors = new HashMap<>();

    @Value("${treasury.account}")
    private String treasuryAccount;

    private TransactionRepository transactionRepository;

    private FabricService fabricService;


    @SuppressWarnings("unused")
    @Autowired
    public CoinService(AccountsService accountsService,
                       FabricService fabricService,
                       TransactionRepository transactionRepository,
                       TransactionMapper mapper) {
        this.fabricService = fabricService;
        this.accountsService = accountsService;
        this.transactionRepository = transactionRepository;
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Moving money to user.", type = SINGLE_REPLENISHMENT)
    public Transaction fillAccount(@NonNull String destinationName,
                                   @NonNull BigDecimal amount,
                                   String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            Account account = removeIsNewStatus(destinationName);

            log.info(account.getEmail());
            InvokeResponseDTO transfer = fabricService.invoke(treasuryAccount, "transfer",
                    new String[]{"user_", account.getEmail(), amount.toBigInteger().toString()}, InvokeResponseDTO.class);

            Transaction transaction = new Transaction();
            if (transfer.getTransactionID() != null) {
                transaction.setDestination(account);
                log.info(transfer.getTransactionID());
                transaction.setTransactionId(transfer.getTransactionID());
                transaction.setAmount(amount);
                transaction.setStatus(TransactionStatus.SUCCESS);
            } else {
                System.out.println();
            }

            return transaction;
        }

    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Distributing money.", type = REGULAR_REPLENISHMENT)
    public Transaction distribute(BigDecimal amount, String comment) {
        List<Account> accounts = accountsService.getAll(AccountType.REGULAR);

        removeIsNewStatus(accounts);

        List<TransferRequest> transferRequests = accounts.stream()
                .map(account -> new TransferRequest(account.getEmail(), amount))
                .collect(Collectors.toList());

        InvokeResponseDTO distribute = fabricService.invoke(treasuryAccount, "batchTransfer",
                transferRequests, InvokeResponseDTO.class);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(distribute.getTransactionID());
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);

        return transaction;
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Transfer money", type = TRANSFER)
    public synchronized Transaction move(@NonNull String accountName,
                                         @NonNull String destinationName,
                                         @NonNull BigDecimal amount,
                                         String comment) {
        checkAmountIsPositive(amount);

        Account donorAccount = removeIsNewStatus(accountName);

        Account acceptorAccount = removeIsNewStatus(destinationName);

        if (!isEnoughAmount(donorAccount.getEmail(), amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        InvokeResponseDTO.Balance move = move(donorAccount.getEmail(), acceptorAccount.getEmail(), amount);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAccount(donorAccount);
        transaction.setDestination(acceptorAccount);
        transaction.setTransactionId(move.getTransactionID());
        transaction.setRemain(move.getPayload().getBalance());

        return transaction;
    }


    public BigDecimal getAmount(String email) {
        InvokeResponseDTO.Balance balanceOf = fabricService.query(email, "balanceOf", new String[]{"user_", email},
            InvokeResponseDTO.Balance.class);
        return balanceOf.getPayload().getBalance();
    }

    public List<BalancesDTO> getAmounts(List<Account> accounts) {
        List<String> emails = accounts.stream()
            .map(Account::getEmail)
            .collect(Collectors.toList());

        InvokeResponseDTO.Balances balanceOf = fabricService.query(treasuryAccount, "batchBalanceOf",
                emails, InvokeResponseDTO.Balances.class);
        return balanceOf.getPayload();
    }

    public BigDecimal getTreasuryAmount() {
        return getAmount(treasuryAccount);
    }

    public BigDecimal getAmountByAccountType(AccountType accountType) {
        return Optional.ofNullable(accountsService.getAll(accountType))
                .map(accounts -> accounts.stream()
                        .peek(account -> account.setAmount(getAmount(account.getEmail())))
                        .map(Account::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Buying", type = EXPENSE)
    public Transaction buy(@NonNull String destinationName, @NonNull String accountName, @NonNull BigDecimal
            amount, String comment) {
        synchronized (getMonitor(accountName)) {
            Account account = accountsService.getAccount(accountName);
            checkEnoughAmount(account.getEmail(), amount);

            removeIsNewStatus(accountName);

            Account merchantAccount = removeIsNewStatus(destinationName);
            InvokeResponseDTO.Balance move = move(account.getEmail(), merchantAccount.getEmail(), amount);

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setAccount(account);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setDestination(merchantAccount);
            transaction.setTransactionId(move.getTransactionID());
            transaction.setRemain(move.getPayload().getBalance());
            return transaction;
        }
    }

    @SaveTransaction(comment = "Rollback previous transaction.", type = ROLLBACK)
    public Transaction rollback(Long txId) {
        Transaction transaction = transactionRepository.findOne(txId);
        Account user = transaction.getAccount();
        Account merchant = transaction.getDestination();
        BigDecimal amount = transaction.getAmount();
        InvokeResponseDTO.Balance move = move(merchant.getEmail(), user.getEmail(), amount);
        Transaction rollbackTx = new Transaction();
        rollbackTx.setTransactionId(move.getTransactionID());
        rollbackTx.setAccount(merchant);
        rollbackTx.setDestination(user);
        rollbackTx.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        rollbackTx.setComment("Rollback buying transaction. ID: " + txId);
        transaction.setRemain(move.getPayload().getBalance());
        return rollbackTx;
    }

    private void checkEnoughAmount(String accountName, BigDecimal amount) {
        checkAmountIsPositive(amount);

        BigDecimal currentAmount = getAmount(accountName);

        if (currentAmount.compareTo(amount) < 0) {
            throw new NotEnoughAmountInAccountException();
        }
    }

    private InvokeResponseDTO.Balance move(String from, String to, BigDecimal amount) {
        return fabricService.invoke(from, "transfer", new String[]{"user_", to,
                amount.toBigInteger().toString()}, InvokeResponseDTO.Balance.class);
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Move money from merchant account to treasury.", type = TRANSFER)
    public synchronized Transaction moveToTreasury(String accountName, BigDecimal amount, String comment) {
        checkAmountIsPositive(amount);

        Account account = removeIsNewStatus(accountName);

        if (!isEnoughAmount(account.getEmail(), amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        InvokeResponseDTO.Balance move = move(account.getEmail(), treasuryAccount, amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(move.getTransactionID());
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setRemain(move.getPayload().getBalance());

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

    private void removeIsNewStatus(List<Account> inAccounts) {
        Optional
                .ofNullable(inAccounts)
                .map(accounts -> accountsService.changeIsNewStatus(false, accounts))
                .orElseThrow(() -> new AccountNotFoundException(""));
    }

    private boolean isEnoughAmount(@NonNull String from, BigDecimal amount) {
        return getAmount(from).compareTo(amount) >= 0;
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

    public String getTreasuryAccount() {
        return treasuryAccount;
    }

}
