package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.eris.contract.response.Response;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoinService {

    private static final String ADD_MONEY = "mint";
    private static final String SEND_MONEY = "send";
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
    public Transaction fillAccount(@NonNull String user,
                                   @NonNull String destinationName,
                                   @NonNull BigDecimal amount,
                                   String comment) {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            ErisAccount erisAccount = getErisAccount(destinationName);
            ErisAccount rootErisAccount = getErisAccount(user);

            if (rootErisAccount.getType() != ErisAccountType.ROOT) {
                throw new ErisProcessingException("Only ROOT user can create money.");
            }

            try {
                Response response = contractService
                        .getForAccount(rootErisAccount)
                        .call(ADD_MONEY, erisAccount.getAddress(), amount);
                processResponseError(response);
            } catch (IOException e) {
                throw new ErisProcessingException("Can't add money for account " + destinationName, e);
            }

            return null;
        }

    }

    private synchronized Transaction refill(String accountName) {
        ErisAccount sellerAccount = accountsService.getAccount(accountName).getErisAccount();
        BigDecimal amount = getAmountForErisAccount(sellerAccount);
        if (amount.signum() > 0) {
            moveByEris(sellerAccount, treasuryAccountAddress, amount, "Move coins from seller " + accountName + "to treasury.");
        }
        return null;
    }


    public void distribute(BigDecimal amount, String sellerName) {
        refill(sellerName);
        BigDecimal amountToDistribute = amount.add(getAllAccountsMoney().negate());

        if (amountToDistribute.signum() > 0) {
            distributeRest(amountToDistribute);
        }
    }

    private void distributeRest(BigDecimal amount) {
        List<Account> accounts = accountsService.getAll();
        int accountCount = accounts.size();
        int mean = amount.intValue() / accountCount;
        int rest = amount.intValue() % accountCount;
        Set<Account> luckyAccounts = accounts.stream()
                .sorted((a1, a2) -> new Random().nextBoolean() ? 1 : -1)
                .limit(rest)
                .collect(Collectors.toSet());
        Set<Account> unluckyAccounts = accounts.stream()
                .filter(a -> ! luckyAccounts.contains(a))
                .collect(Collectors.toSet());
        addForAll(luckyAccounts, new BigDecimal(mean + 1));
        addForAll(unluckyAccounts, new BigDecimal(mean));
    }

    private BigDecimal getAllAccountsMoney() {
        return accountsService.getAll().stream()
                .map(account -> getAmount(account.getLdapId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addForAll(Collection<Account> accounts, BigDecimal amount) {
        accounts.forEach(a -> moveByEris(treasuryErisAccount, a.getErisAccount().getAddress(), amount, "Add coins for " + a.getFullName()));
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
            Response<BigDecimal> response = contractService.getForAccount(erisAccount).call(GET_MONEY, erisAccount.getAddress());
            processResponseError(response);
            return response.getReturnValue().getVal();
        } catch (Exception e) {
            throw new ErisProcessingException("Can't query balance for account " + erisAccount.getAddress(), e);
        }
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
                    .call(SEND_MONEY, address, amount);
            if (! (Boolean) response.getReturnValue().getVal()) throw new NotEnoughAmountInAccountException();
            processResponseError(response);
        } catch (Exception e) {
            throw new ErisProcessingException("Can't move money for account " + address, e);
        }
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
