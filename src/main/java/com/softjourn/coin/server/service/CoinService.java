package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.eris.contract.response.Response;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CoinService {

    private static final String ADD_MONEY = "mint";
    private static final String SEND_MONEY = "send";
    private static final String GET_MONEY = "queryBalance";

    private AccountsService accountsService;

    private ErisContractService contractService;

    private Map<String, String> monitors = new HashMap<>();

    @Autowired
    public CoinService(AccountsService accountsService, ErisContractService contractService) {
        this.accountsService = accountsService;
        this.contractService = contractService;
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
                processResponse(response);
            } catch (IOException e) {
                throw new ErisProcessingException("Can't add money for account " + destinationName, e);
            }

            return null;
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

        move(donor, acceptor.getAddress(), amount);

        return null;
    }



    public BigDecimal getAmount(String ldapId) {
        try {
            ErisAccount account = getErisAccount(ldapId);
            Response<BigDecimal> response = contractService.getForAccount(account).call(GET_MONEY, account.getAddress());
            processResponse(response);
            return response.getReturnValue().getVal();
        } catch (Exception e) {
            throw new ErisProcessingException("Can't query balance for account " + ldapId, e);
        }
    }

    @SaveTransaction
    public Transaction spent(@NonNull String sellerAddress, @NonNull String accountName, @NonNull BigDecimal amount, String comment) {
        synchronized (getMonitor(accountName)) {
            checkAmountIsPositive(amount);

            ErisAccount account = getErisAccount(accountName);

            BigDecimal currentAmount = getAmount(accountName);

            if (currentAmount.compareTo(amount) < 0) {
                throw new NotEnoughAmountInAccountException();
            }

            move(account, sellerAddress, amount);

            return null;
        }
    }

    private void move(ErisAccount account, String address, BigDecimal amount) {
        try {
            Response response = contractService
                    .getForAccount(account)
                    .call(SEND_MONEY, address, amount);
            processResponse(response);
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

    private void processResponse(Response response) {
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
