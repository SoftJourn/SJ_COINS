package com.softjourn.coin.server.service;


import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.AccountWasDeletedException;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.common.auth.OAuthHelper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.softjourn.coin.server.entity.AccountType.REGULAR;

@Service
public class AccountsService {

    private static final String DEFAULT_IMAGE_NAME = "images/default.png";

    private AccountRepository accountRepository;

    private ErisAccountsService erisAccountsService;

    private ErisAccountRepository erisAccountRepository;

    private CoinService coinService;

    private String authServerUrl;
    private OAuthHelper oAuthHelper;

    @Autowired
    public AccountsService(AccountRepository accountRepository,
                           ErisAccountRepository erisAccountRepository,
                           @Lazy CoinService coinService,
                           ErisAccountsService erisAccountsService,
                           @Value("${auth.server.url}") String authServerUrl,
                           OAuthHelper oAuthHelper) {
        this.accountRepository = accountRepository;
        this.erisAccountRepository = erisAccountRepository;
        this.coinService = coinService;
        this.erisAccountsService = erisAccountsService;
        this.authServerUrl = authServerUrl;
        this.oAuthHelper = oAuthHelper;
    }

    Account getAccountIfExistInLdapBase(String ldapId) {
        try {
            return oAuthHelper
                    .getForEntityWithToken(authServerUrl + "/api/v1/users/" + ldapId, Account.class).getBody();
        } catch (RestClientException rce) {
            return null;
        }
    }

    public List<Account> getAll() {
        return accountRepository.findAllUndeleted();
    }

    /**
     * Get all accounts of particular type
     *
     * @param accountType type of account
     * @return list of accounts
     */
    public List<Account> getAll(AccountType accountType) {
        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.DESC, "isNew"),
                new Sort.Order(Sort.Direction.ASC, "fullName"));

        return accountRepository.getAccountsByType(accountType, sort);
    }

    public Account getAccount(String ldapId) {
        return Optional
                .ofNullable(accountRepository.findOneUndeleted(ldapId))
                .orElseGet(() -> createAccount(ldapId));
    }

    public Account add(String ldapId) {
        Account account = accountRepository.findOne(ldapId);
        if (account == null) {
            return createAccount(ldapId);
        } else if (account.isDeleted()) {
            throw new AccountWasDeletedException("Account \"" + ldapId + "\" was deleted. Contact administrators.");
        } else return account;
    }

    public Account update(Account account) {
        return accountRepository.save(account);
    }

    public Account changeIsNewStatus(Boolean isNew, @NonNull Account account) {
        account.setNew(isNew);

        return accountRepository.save(account);
    }

    @Transactional
    public List<Account> changeIsNewStatus(Boolean isNew, @NonNull Iterable<Account> accounts) {
        List<String> accountsIds = StreamSupport
                .stream(accounts.spliterator(), false)
                .filter(account -> account.isNew() != isNew)
                .map(Account::getLdapId)
                .collect(Collectors.toList());

        if (Objects.nonNull(accountsIds) && !accountsIds.isEmpty()) {
            accountRepository.changeIsNewStatus(isNew, accountsIds);
        }

        return StreamSupport
                .stream(accounts.spliterator(), false)
                .peek(account -> checkAndChangeIsNewStatus(isNew, account))
                .collect(Collectors.toList());
    }

    private Account checkAndChangeIsNewStatus(Boolean isNew, Account account) {
        if (account.isNew() != isNew) {
            account.setNew(isNew);
        }

        return account;
    }

    @Transactional
    Account createAccount(String ldapId) {
        return Optional.ofNullable(getAccountIfExistInLdapBase(ldapId))
                .map(a -> buildAccount(a, getNewErisAccount()))
                .map(a -> accountRepository.save(a))
                .orElseThrow(() -> new AccountNotFoundException(ldapId));
    }

    private Account buildAccount(Account account, ErisAccount erisAccount) {
        account.setAmount(new BigDecimal(0));
        account.setImage(DEFAULT_IMAGE_NAME);
        account.setAccountType(AccountType.REGULAR);
        account.setNew(true);
        account.setAccountType(REGULAR);
        account.setErisAccount(erisAccount);
        erisAccount.setAccount(account);
        return account;
    }

    private ErisAccount getNewErisAccount() {
        ErisAccount erisAccount = erisAccountsService.bindFreeAccount();
        if (erisAccount == null) throw new ErisProcessingException("Can't create new ErisAccount");
        return erisAccount;
    }

    @Transactional
    public Account addMerchant(MerchantDTO merchantDTO, AccountType accountType) {
        Account newMerchantAccount = new Account(merchantDTO.getUniqueId(), BigDecimal.ZERO);
        newMerchantAccount.setFullName(merchantDTO.getName());
        newMerchantAccount.setAccountType(accountType);
        newMerchantAccount.setNew(true);
        ErisAccount erisAccount = erisAccountsService.bindFreeAccount();

        if (erisAccount == null) {
            throw new ErisAccountNotFoundException();
        }

        newMerchantAccount = accountRepository.save(newMerchantAccount);
        erisAccount.setAccount(newMerchantAccount);
        erisAccountRepository.save(erisAccount);

        return newMerchantAccount;
    }

    @Transactional
    public boolean delete(String ldapId) {
        BigDecimal accountAmount = coinService.getAmount(ldapId);

        if (accountAmount.compareTo(BigDecimal.ZERO) > 0) {
            String comment = String.format(
                    "Withdrawal of all the coins to treasury before delete account %s",
                    ldapId);

            coinService.moveToTreasury(ldapId, accountAmount, comment);
        }

        return accountRepository.updateIsDeletedByLdapId(ldapId, true) == 1;
    }
}
