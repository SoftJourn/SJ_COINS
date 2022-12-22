package com.softjourn.coin.server.service;

import com.softjourn.coin.server.config.ApplicationProperties;
import com.softjourn.coin.server.dto.BalancesDTO;
import com.softjourn.coin.server.dto.EnrollResponseDTO;
import com.softjourn.coin.server.dto.MerchantDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.exceptions.AccountEnrollException;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.AccountWasDeletedException;
import com.softjourn.coin.server.exceptions.NotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.common.auth.OAuthHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AccountsService {

  private static final String DEFAULT_IMAGE_NAME = "/account/default";

  private final CoinService coinService;
  private final AccountRepository accountRepository;
  private final OAuthHelper oAuthHelper;
  private final ApplicationProperties applicationProperties;
  private final FabricService fabricService;

  @Autowired
  public AccountsService(
      AccountRepository accountRepository,
      @Lazy CoinService coinService,
      OAuthHelper oAuthHelper,
      FabricService fabricService,
      ApplicationProperties applicationProperties) {
    this.accountRepository = accountRepository;
    this.coinService = coinService;
    this.oAuthHelper = oAuthHelper;
    this.fabricService = fabricService;
    this.applicationProperties = applicationProperties;
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
    Sort sort = Sort.by(
        new Sort.Order(Sort.Direction.DESC, "isNew"),
        new Sort.Order(Sort.Direction.ASC, "fullName"));
    return accountRepository.getAccountsByType(accountType, sort);
  }

  public Account getAccount(String ldapId) {
    return accountRepository.findOneUndeleted(ldapId).orElseGet(() -> createAccount(ldapId));
  }

  public List<Account> getAmounts(List<Account> accounts) {
    List<BalancesDTO> amounts = coinService.getAmounts(accounts);
    accounts.forEach(account -> amounts.forEach(amount -> {
      if (account.getEmail().equals(amount.getUserId())) {
        account.setAmount(amount.getBalance());
      }
    }));
    return accounts;
  }

  public Account add(String ldapId) {
    Account account = accountRepository.findOne(ldapId);
    if (account == null) {
      return createAccount(ldapId);
    } else if (account.isDeleted()) {
      throw new AccountWasDeletedException(
          "Account \"" + ldapId + "\" was deleted. Contact administrators.");
    } else return account;
  }

  @Transactional
  public Account addMerchant(MerchantDTO merchantDTO, AccountType accountType) {
    Account newMerchantAccount = new Account(
        merchantDTO.getUniqueId(), merchantDTO.getName(), BigDecimal.ZERO);
    newMerchantAccount.setFullName(merchantDTO.getName());
    newMerchantAccount.setAccountType(accountType);
    newMerchantAccount.setNew(true);

    EnrollResponseDTO body = fabricService.enroll(newMerchantAccount.getEmail()).getBody();
    if (body.getSuccess()) {
      return accountRepository.save(newMerchantAccount);
    } else {
      throw new AccountEnrollException(
          "Failure try to enroll account with email " + newMerchantAccount.getEmail());
    }
  }

  @Transactional
  public boolean delete(String ldapId) {
    Account account = accountRepository.findOne(ldapId);
    BigDecimal accountAmount = coinService.getAmount(account.getEmail());

    if (accountAmount.compareTo(BigDecimal.ZERO) > 0) {
      String comment = String.format(
          "Withdrawal of all the coins to treasury before delete account %s",
          ldapId);

      coinService.moveToTreasury(account.getLdapId(), accountAmount, comment);
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
    String url = applicationProperties.getImage().getStorage().getPath() + uri;
    File storedFile = new File(url);
    try {
      FileUtils.deleteDirectory(storedFile.getParentFile());
      Files.createDirectories(storedFile.getParentFile().toPath());
      //noinspection ResultOfMethodCallIgnored
      storedFile.createNewFile();
      FileOutputStream out = new FileOutputStream(storedFile);
      out.write(file.getBytes());
    } catch (Exception e) {
      throw new IllegalArgumentException("Can not create file with " + url + " path", e);
    }
  }

  /**
   * @param accountName ldap id
   * @throws AccountNotFoundException in case of account does not exists
   */
  private Account checkAccountExists(String accountName) {
    return accountRepository.findOneUndeleted(accountName)
        .orElseThrow(() -> new AccountNotFoundException(accountName));
  }

  public byte[] getImage(String uri) {
    File file = new File(applicationProperties.getImage().getStorage().getPath() + uri);

    try (InputStream in = new FileInputStream(file)) {
      return IOUtils.toByteArray(in);
    } catch (FileNotFoundException e) {
      throw new NotFoundException("There is no image with this passed uri");
    } catch (IOException e) {
      // Can't read file. Should never happened
      log.error("Method getImage uri. File can't be read", e);
      throw new RuntimeException("File can't be read");
    }
  }

  public byte[] getDefaultImage() {
    return this.getImage(applicationProperties.getImage().getAccount().getDefaultUrl());
  }

  public void reset() {
    List<Account> accounts = accountRepository.findAll();

    // enroll
    for (Account account : accounts) {
      Account accountIfExistInLdapBase = getAccountIfExistInLdapBase(account.getLdapId());
      if (accountIfExistInLdapBase != null && accountIfExistInLdapBase.getEmail() != null) {
        account.setEmail(accountIfExistInLdapBase.getEmail());
        fabricService.enroll(account.getEmail());
        accountRepository.save(account);
      } else {
        accountRepository.delete(account);
      }
    }
  }

  Account getAccountIfExistInLdapBase(String ldapId) {
    try {
      return oAuthHelper.getForEntityWithToken(applicationProperties
              .getAuth().getServer().getUrl() + "/v1/users/" + ldapId, Account.class)
          .getBody();
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

  @Transactional
  public Account createAccount(String ldapId) {
    return Optional.ofNullable(getAccountIfExistInLdapBase(ldapId))
        .map(this::buildAccount)
        .map(accountRepository::save)
        .orElseThrow(() -> new AccountNotFoundException(ldapId));
  }

  private Account buildAccount(Account account) {
    account.setAmount(new BigDecimal(0));
    account.setImage(DEFAULT_IMAGE_NAME);
    account.setAccountType(AccountType.REGULAR);
    account.setNew(true);
    return account;
  }

  private Account checkAndChangeIsNewStatus(Boolean isNew, Account account) {
    if (account.isNew() != isNew) {
      account.setNew(isNew);
    }

    return account;
  }
}
