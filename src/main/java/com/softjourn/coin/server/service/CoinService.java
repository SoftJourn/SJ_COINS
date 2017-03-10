package com.softjourn.coin.server.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.CashDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.*;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.coin.server.util.QRCodeUtil;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.TxParams;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CoinService {

    private static final String SEND_MONEY = "transfer";
    private static final String DISTRIBUTE_MONEY = "distribute";
    private static final String GET_MONEY = "balanceOf";
    private static final String WITHDRAW_MONEY = "withdraw";
    private static final String DEPOSIT = "deposite";
    private static final String APPROVE_TRANSFER = "approve";

    private AccountsService accountsService;

    private ErisContractService contractService;

    private Map<String, String> monitors = new HashMap<>();

    @Value("${eris.treasury.account.address}")
    private String treasuryAccountAddress;
    @Value("${eris.treasury.account.key.public}")
    private String treasuryAccountPubKey;
    @Value("${eris.treasury.account.key.private}")
    private String treasuryAccountPrivKey;

    @Value("${eris.offline.contract.address}")
    private String offlineVaultAddress;

    @Value("${eris.token.contract.address}")
    private String tokenContractAddress;

    private ErisAccount treasuryErisAccount;

    private TransactionRepository transactionRepository;


    @SuppressWarnings("unused")
    @Autowired
    public CoinService(AccountsService accountsService, ErisContractService contractService, ErisAccountRepository erisAccountRepository, TransactionRepository transactionRepository) {
        this.accountsService = accountsService;
        this.contractService = contractService;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    private void setUp() {
        treasuryErisAccount = new ErisAccount();
        treasuryErisAccount.setAddress(treasuryAccountAddress);
        treasuryErisAccount.setPubKey(treasuryAccountPubKey);
        treasuryErisAccount.setPrivKey(treasuryAccountPrivKey);
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Moving money to user.")
    public Transaction fillAccount(@NonNull String destinationName,
                                   @NonNull BigDecimal amount,
                                   String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            Account account = removeIsNewStatus(destinationName);

            ErisAccount erisAccount = getErisAccount(account);

            Response response = moveByEris(treasuryErisAccount, erisAccount.getAddress(), amount);

            return mapToTransaction(response);
        }

    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Distributing money.")
    public Transaction distribute(BigDecimal amount, String comment) {
        try {
            List<Account> accounts = accountsService.getAll(AccountType.REGULAR);

            removeIsNewStatus(accounts);

            List<String> accountsAddresses = accounts.stream()
                    .map(Account::getErisAccount)
                    .map(ErisAccount::getAddress)
                    .collect(Collectors.toList());

            Response response = contractService
                    .getTokenContractForAccount(treasuryErisAccount)
                    .call(DISTRIBUTE_MONEY, accountsAddresses, amount.toBigInteger());

            processResponseError(response);

            Optional.ofNullable(response)
                    .flatMap(r -> Optional.ofNullable(r.getReturnValues()))
                    .flatMap(v -> Optional.ofNullable(v.get(0)))
                    .map(v -> (Boolean) v)
                    .flatMap(v -> v ? Optional.of(1) : Optional.empty())
                    .orElseThrow(NotEnoughAmountInAccountException::new);

            return mapToTransaction(response);

        } catch (Exception e) {
            throw new ErisProcessingException("Can't distribute money.", e);
        }
    }

    @SuppressWarnings("unused")
    @SaveTransaction
    public synchronized Transaction move(@NonNull String accountName,
                                         @NonNull String destinationName,
                                         @NonNull BigDecimal amount,
                                         String comment) {
        checkAmountIsPositive(amount);

        Account donorAccount = removeIsNewStatus(accountName);

        ErisAccount donor = getErisAccount(donorAccount);

        Account acceptorAccount = removeIsNewStatus(destinationName);

        ErisAccount acceptor = getErisAccount(acceptorAccount);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        Response response = moveByEris(donor, acceptor.getAddress(), amount);

        return mapToTransaction(response);
    }


    public BigDecimal getAmount(String ldapId) {
        try {
            ErisAccount account = getErisAccount(ldapId);
            return getAmountForErisAccount(account);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't query balance for account " + ldapId, e);
        }
    }

    private BigDecimal getAmountForErisAccount(ErisAccount erisAccount) {
        try {
            Response response = contractService.getTokenContractForAccount(erisAccount).call(GET_MONEY, erisAccount.getAddress());
            processResponseError(response);
            return Optional.ofNullable(response)
                    .flatMap(r -> Optional.ofNullable(r.getReturnValues()))
                    .flatMap(v -> Optional.ofNullable(v.get(0)))
                    .map(v -> (BigInteger) v)
                    .map(BigDecimal::new)
                    .orElseThrow(() -> new ErisProcessingException("Wrong server response."));
        } catch (Exception e) {
            throw new ErisProcessingException("Can't query balance for account " + erisAccount.getAddress(), e);
        }
    }

    public BigDecimal getTreasuryAmount() {
        return getAmountForErisAccount(treasuryErisAccount);
    }

    public BigDecimal getAmountByAccountType(AccountType accountType) {
        return Optional.ofNullable(accountsService.getAll(accountType))
                .map(accounts -> accounts.stream()
                        .filter(account -> Objects.nonNull(account.getErisAccount()))
                        .peek(account -> account.setAmount(getAmountForErisAccount(account.getErisAccount())))
                        .map(Account::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }

    @SuppressWarnings("unused")
    @SaveTransaction
    public Transaction buy(@NonNull String destinationName, @NonNull String accountName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(accountName)) {
            checkEnoughAmount(accountName, amount);

            Account account = removeIsNewStatus(accountName);

            ErisAccount erisAccount = getErisAccount(account);
            Account merchantAccount = removeIsNewStatus(destinationName);
            ErisAccount sellerAccount = getErisAccount(merchantAccount);

            Response response = moveByEris(erisAccount, sellerAccount.getAddress(), amount);

            return mapToTransaction(response);
        }
    }

    @SaveTransaction
    public Transaction rollback(Long txId) {
        Transaction transaction = transactionRepository.findOne(txId);
        Account user = transaction.getAccount();
        Account merchant = transaction.getDestination();
        BigDecimal amount = transaction.getAmount();
        Response response = moveByEris(merchant.getErisAccount(), user.getErisAccount().getAddress(), amount);
        Transaction rollbackTx = mapToTransaction(response);
        rollbackTx.setAccount(merchant);
        rollbackTx.setDestination(user);
        rollbackTx.setAmount(amount);
        rollbackTx.setComment("Rollback buying body. ID: " + txId);
        return rollbackTx;
    }

    @SuppressWarnings("unused")
    @SaveTransaction
    public byte[] withdraw(@NonNull String accountName, @NonNull BigDecimal amount, String comment, boolean image) {
        try {
            checkEnoughAmount(accountName, amount);

            ErisAccount account = getErisAccount(accountName);

            Response approveResponse = contractService
                    .getTokenContractForAccount(account)
                    .call(APPROVE_TRANSFER, offlineVaultAddress, amount.toBigInteger());

            processResponseError(approveResponse);

            Response response = contractService
                    .getOfflineContractForAccount(account)
                    .call(WITHDRAW_MONEY, tokenContractAddress, amount.toBigInteger());

            processResponseError(response);

            return Optional.ofNullable(response)
                    .flatMap(r -> Optional.ofNullable(r.getReturnValues()))
                    .flatMap(l -> Optional.ofNullable(l.get(0)))
                    .map(v -> (byte[]) v)
                    .map(v -> withdrawResultMapping(v, tokenContractAddress, offlineVaultAddress, amount.toBigInteger()))
                    .map(v -> image ? QRCodeUtil.genQRCode(v) : v.getBytes())
                    .orElseThrow(() -> new ErisProcessingException("Wrong server response."));

        } catch (Exception e) {
            throw new ErisProcessingException("Can't withdraw money.", e);
        }

    }

    @SaveTransaction
    public Transaction deposit(CashDTO cashDTO, String destinationName, String comment, BigDecimal amount) {
        try {
            ErisAccount account = getErisAccount(destinationName);

            byte[] hash = Hex.decodeHex(cashDTO.getChequeHash().toCharArray());

            Response response = contractService
                    .getOfflineContractForAccount(account)
                    .call(DEPOSIT, hash, cashDTO.getTokenContractAddress());

            processResponseError(response);

            if (!(Boolean) response.getReturnValues().get(0)) {
                throw new ChequeIsUsedException();
            }

            return mapToTransaction(response);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't deposite money." + e.getMessage(), e);
        }
    }

    private String withdrawResultMapping(byte[] cheque, String tokenAddress, String offlineAddress, BigInteger amount) {
        try {
            String hexCheque = Hex.encodeHexString(cheque);
            CashDTO cashDTO = new CashDTO(tokenAddress, offlineAddress, hexCheque, amount);
            return new ObjectMapper().writeValueAsString(cashDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't convert CashDTO object to JSON.");
        }
    }

    private void checkEnoughAmount(String accountName, BigDecimal amount) {
        checkAmountIsPositive(amount);

        BigDecimal currentAmount = getAmount(accountName);

        if (currentAmount.compareTo(amount) < 0) {
            throw new NotEnoughAmountInAccountException();
        }
    }

    private Response moveByEris(ErisAccount account, String address, BigDecimal amount) {
        try {
            Response response = contractService
                    .getTokenContractForAccount(account)
                    .call(SEND_MONEY, address, amount.toBigInteger());
            processResponseError(response);
            return Optional.ofNullable(response)
                    .flatMap(r -> Optional.ofNullable(r.getReturnValues()))
                    .flatMap(v -> Optional.ofNullable(v.get(0)))
                    .map(v -> (Boolean) v)
                    .flatMap(v -> v ? Optional.of(response) : Optional.empty())
                    .orElseThrow(NotEnoughAmountInAccountException::new);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't move money for account " + address, e);
        }
    }

    private Transaction mapToTransaction(Response response) {
        return Optional.ofNullable(response)
                .map(Response::getTxParams)
                .map(TxParams::getTxId)
                .map(Transaction::new)
                .orElseGet(Transaction::new);
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Move money from merchant account to treasury.")
    public synchronized Transaction moveToTreasury(String accountName, BigDecimal amount, String comment) {
        checkAmountIsPositive(amount);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        Account account = removeIsNewStatus(accountName);

        ErisAccount erisAccount = getErisAccount(account);

        Response response = moveByEris(erisAccount, treasuryAccountAddress, amount);

        return mapToTransaction(response);
    }

    private ErisAccount getErisAccount(String ldapId) {
        return Optional
                .ofNullable(accountsService.getAccount(ldapId))
                .map(Account::getErisAccount)
                .orElseThrow(ErisAccountNotFoundException::new);

    }

    private ErisAccount getErisAccount(Account account) {
        return Optional
                .ofNullable(account)
                .map(Account::getErisAccount)
                .orElseThrow(ErisAccountNotFoundException::new);
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

    private List<Account> removeIsNewStatus(List<Account> inAccounts) {
        return Optional
                .ofNullable(inAccounts)
                .map(accounts -> accountsService.changeIsNewStatus(false, accounts))
                .orElseThrow(() -> new AccountNotFoundException(""));
    }

    private void processResponseError(Response response) {
        if (response.getError() != null) throw new ErisProcessingException(response.getError().getMessage());
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


}
