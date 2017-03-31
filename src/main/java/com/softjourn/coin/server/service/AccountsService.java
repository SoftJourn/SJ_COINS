package com.softjourn.coin.server.service;


import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.exceptions.*;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.common.auth.OAuthHelper;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.softjourn.coin.server.entity.AccountType.REGULAR;

@Service
@Slf4j
public class AccountsService {

    private static final String DEFAULT_IMAGE_NAME = "/account/default";

    private ErisAccountsService erisAccountsService;
    private CoinService coinService;

    private ErisAccountRepository erisAccountRepository;
    private AccountRepository accountRepository;

    private String authServerUrl;
    private OAuthHelper oAuthHelper;

    private String imageStoragePath;
    private String defaultAccountImagePath;

    @Autowired
    public AccountsService(AccountRepository accountRepository,
                           ErisAccountRepository erisAccountRepository,
                           @Lazy CoinService coinService,
                           ErisAccountsService erisAccountsService,
                           @Value("${auth.server.url}") String authServerUrl,
                           OAuthHelper oAuthHelper,
                           @Value("${image.storage.path}") String imageStoragePath,
                           @Value("${image.account.default}") String defaultAccountImagePath) {
        this.accountRepository = accountRepository;
        this.erisAccountRepository = erisAccountRepository;
        this.coinService = coinService;
        this.erisAccountsService = erisAccountsService;
        this.authServerUrl = authServerUrl;
        this.oAuthHelper = oAuthHelper;
        this.imageStoragePath = imageStoragePath;
        this.defaultAccountImagePath = defaultAccountImagePath;
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

    public void loadAccountImage(MultipartFile file, String accountName) {
        Account account = checkAccountExists(accountName);
        String uri = String.format("/account/%s/%s", account.getLdapId(), file.getOriginalFilename());
        this.storeFile(file, uri);
        account.setImage(uri);
        this.update(account);
    }

    private void storeFile(MultipartFile file, String uri) {
        String url = this.imageStoragePath + uri;
        File storedFile = new File(url);
        try {
            FileUtils.deleteDirectory(storedFile.getParentFile());
            Files.createDirectories(storedFile.getParentFile().toPath());
            //noinspection ResultOfMethodCallIgnored
            storedFile.createNewFile();
            FileOutputStream out = new FileOutputStream(storedFile);
            out.write(file.getBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not create file with "+url +" path", e);
        }
    }

    /**
     * @param accountName ldap id
     * @throws AccountNotFoundException in case of account does not exists
     */
    private Account checkAccountExists(String accountName) {
        return Optional
            .ofNullable(accountRepository.findOneUndeleted(accountName))
            .orElseThrow(() -> new AccountNotFoundException(accountName));
    }

    public byte[] getImage(String uri) {
        String fullPath = this.imageStoragePath + uri;
        File file = new File(fullPath);
        InputStream in;
        try {
            in = new FileInputStream(file);
            return IOUtils.toByteArray(in);
        } catch (FileNotFoundException e) {
            throw new NotFoundException("There is no image with this passed uri");
        } catch (IOException e) {
            // Can't read file. Should never happened
            log.error("Method getImage uri. File can't be read", e);
            throw new InternalException("File can't be read");
        }

    }

    public byte[] getDefaultImage() {
        return this.getImage(this.defaultAccountImagePath);
    }

    Account getAccountIfExistInLdapBase(String ldapId) {
        try {
            return oAuthHelper
                .getForEntityWithToken(authServerUrl + "/v1/users/" + ldapId, Account.class).getBody();
        } catch (RestClientException rce) {
            return null;
        }
    }

    Account update(Account account) {
        return accountRepository.save(account);
    }

    Account changeIsNewStatus(Boolean isNew, @NonNull Account account) {
        account.setNew(isNew);

        return accountRepository.save(account);
    }

    @Transactional
    List<Account> changeIsNewStatus(Boolean isNew, @NonNull Iterable<Account> accounts) {
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

    private Account checkAndChangeIsNewStatus(Boolean isNew, Account account) {
        if (account.isNew() != isNew) {
            account.setNew(isNew);
        }

        return account;
    }
}
