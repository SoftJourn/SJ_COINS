package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.entity.*;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.eris.contract.response.Response;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoinService {

    private static final String ADD_MONEY = "mint";
    private static final String SEND_MONEY = "send";
    private static final String DISTRIBUTE_MONEY = "distribute";
    private static final String GET_MONEY = "queryBalance";

    private AccountsService accountsService;

    private ErisContractService contractService;

    private ErisAccountRepository erisAccountRepository;

    private Map<String, String> monitors = new HashMap<>();

    @Value("${eris.treasury.account.address}")
    private String treasuryAccountAddress;
    @Value("${eris.treasury.account.key.public}")
    private String treasuryAccountPubKey;
    @Value("${eris.treasury.account.key.private}")
    private String treasuryAccountPrivKey;

    private ErisAccount treasuryErisAccount;


    @Autowired
    public CoinService(AccountsService accountsService, ErisContractService contractService, ErisAccountRepository erisAccountRepository) {
        this.accountsService = accountsService;
        this.contractService = contractService;
        this.erisAccountRepository = erisAccountRepository;
    }

    @PostConstruct
    private void setUp() {
        treasuryErisAccount = new ErisAccount();
        treasuryErisAccount.setAddress(treasuryAccountAddress);
        treasuryErisAccount.setPubKey(treasuryAccountPubKey);
        treasuryErisAccount.setPrivKey(treasuryAccountPrivKey);
    }

    @SaveTransaction
    public Transaction fillAccount(@NonNull String destinationName,
                                   @NonNull BigDecimal amount,
                                   String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            ErisAccount erisAccount = getErisAccount(destinationName);

            moveByEris(treasuryErisAccount, erisAccount.getAddress(), amount, comment);

            return null;
        }

    }

    @SaveTransaction
    public void distribute(BigDecimal amount, String comment) {
        try {
            List<String> accountsAddresses = accountsService.getAll(AccountType.REGULAR).stream()
                    .map(Account::getErisAccount)
                    .map(ErisAccount::getAddress)
                    .collect(Collectors.toList());

            Response response = contractService
                    .getForAccount(treasuryErisAccount)
                    .call(DISTRIBUTE_MONEY, accountsAddresses, amount.toBigInteger());

            if (response.getError() != null) {
                throw new ErisProcessingException("Can't distribute money. " + response.getError().getMessage());
            }

            if (! (Boolean) response.getReturnValue().getVal()) {
                throw new NotEnoughAmountInAccountException();
            }

            processResponseError(response);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't distribute money.", e);
        }
    }

    @SaveTransaction
    public synchronized Transaction move(@NonNull String accountName,
                                         @NonNull String destinationName,
                                         @NonNull BigDecimal amount,
                                         String comment) {
        checkAmountIsPositive(amount);

        ErisAccount donor = getErisAccount(accountName);
        ErisAccount acceptor = getErisAccount(destinationName);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        moveByEris(donor, acceptor.getAddress(), amount, comment);

        return null;
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
            Response<BigInteger> response = contractService.getForAccount(erisAccount).call(GET_MONEY, erisAccount.getAddress());
            processResponseError(response);
            return new BigDecimal(response.getReturnValue().getVal());
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

    @SaveTransaction
    public Transaction buy(@NonNull String destinationName, @NonNull String accountName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(accountName)) {
            checkAmountIsPositive(amount);

            ErisAccount account = getErisAccount(accountName);

            BigDecimal currentAmount = getAmount(accountName);

            if (currentAmount.compareTo(amount) < 0) {
                throw new NotEnoughAmountInAccountException();
            }

            ErisAccount sellerAccount = getErisAccount(destinationName);

            moveByEris(account, sellerAccount.getAddress(), amount, comment);

            return null;
        }
    }

    @SaveTransaction
    public void moveByEris(ErisAccount account, String address, BigDecimal amount, String comment) {
        try {
            Response response = contractService
                    .getForAccount(account)
                    .call(SEND_MONEY, address, amount.toBigInteger());
            if (! (Boolean) response.getReturnValue().getVal()) throw new NotEnoughAmountInAccountException();
            processResponseError(response);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't move money for account " + address, e);
        }
    }

    @SaveTransaction
    public synchronized Transaction moveToTreasury(String accountName, AmountDTO amountDTO) {
        checkAmountIsPositive(amountDTO.getAmount());

        if (!isEnoughAmount(accountName, amountDTO.getAmount())) {
            throw new NotEnoughAmountInAccountException();
        }

        ErisAccount erisAccount = getErisAccount(accountName);

        moveByEris(erisAccount,
                treasuryAccountAddress,
                amountDTO.getAmount(),
                String.format(amountDTO.getComment(), accountName));

        return null;
    }

    private ErisAccount getErisAccount(String ldapId) {
        return Optional
                .ofNullable(accountsService.getAccount(ldapId))
                .map(Account::getErisAccount)
                .orElseThrow(() -> new ErisProcessingException("Eris account for user " + ldapId +
                        "is not set. You can't pass coins for this account"));

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
