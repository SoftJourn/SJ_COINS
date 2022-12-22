package com.softjourn.coin.server.service;

import static com.softjourn.coin.server.entity.TransactionType.EXPENSE;
import static com.softjourn.coin.server.entity.TransactionType.REGULAR_REPLENISHMENT;
import static com.softjourn.coin.server.entity.TransactionType.ROLLBACK;
import static com.softjourn.coin.server.entity.TransactionType.SINGLE_REPLENISHMENT;
import static com.softjourn.coin.server.entity.TransactionType.TRANSFER;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.config.ApplicationProperties;
import com.softjourn.coin.server.dto.BalancesDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.dto.TransferRequest;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.FabricCoinsFunction;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class CoinService {

  private static final String USER_PREFIX = "user_";
  private static final Boolean EXPIRABLE = Boolean.TRUE;
  private static final Boolean NON_EXPIRABLE = Boolean.FALSE;

  private final AccountsService accountsService;
  private final FabricService fabricService;
  private final TransactionRepository transactionRepository;
  private final ApplicationProperties applicationProperties;
  private final Map<String, String> monitors = new HashMap<>();

  /**
   * Fill account.
   *
   * @param destinationName Destination account name.
   * @param amount Amount.
   * @param comment Comment for transaction.
   * @return New transaction.
   */
  @SuppressWarnings("unused")
  @SaveTransaction(comment = "Moving money to user.", type = SINGLE_REPLENISHMENT)
  public Transaction fillAccount(
      @NonNull String destinationName, @NonNull BigDecimal amount, String comment
  ) {
    synchronized (getMonitor(destinationName)) {
      checkAmountIsPositive(amount);

      Account account = removeIsNewStatus(destinationName);

      log.info(account.getEmail());
      InvokeResponseDTO transfer = fabricService.invoke(
          applicationProperties.getTreasury().getAccount(),
          Chaincode.COINS,
          FabricCoinsFunction.TRANSFER,
          new String[]{
              USER_PREFIX, account.getEmail(),
              amount.toBigInteger().toString(), NON_EXPIRABLE.toString()
          },
          InvokeResponseDTO.class);

      Transaction transaction = new Transaction();
      if (Objects.nonNull(transfer.getTransactionID())) {
        transaction.setDestination(account);
        log.info(transfer.getTransactionID());
        transaction.setTransactionId(transfer.getTransactionID());
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
      }

      return transaction;
    }
  }

  /**
   * Distribute coins.
   *
   * @param amount Amount.
   * @param comment Comment for transaction.
   * @return
   */
  @SuppressWarnings("unused")
  @SaveTransaction(comment = "Distributing money.", type = REGULAR_REPLENISHMENT)
  public Transaction distribute(BigDecimal amount, String comment) {
    List<Account> accounts = accountsService.getAll(AccountType.REGULAR);

    removeIsNewStatus(accounts);

    List<TransferRequest> transferRequests = accounts.stream()
        .map(account -> new TransferRequest(account.getEmail(), amount))
        .collect(Collectors.toList());

    InvokeResponseDTO distribute = fabricService.invoke(
        applicationProperties.getTreasury().getAccount(),
        Chaincode.COINS,
        FabricCoinsFunction.BATCH_TRANSFER,
        new Object[]{transferRequests, NON_EXPIRABLE},
        InvokeResponseDTO.class);

    Transaction transaction = new Transaction();
    transaction.setTransactionId(distribute.getTransactionID());
    transaction.setAmount(amount);
    transaction.setStatus(TransactionStatus.SUCCESS);

    return transaction;
  }

  /**
   * Buy something.
   *
   * @param recipientId Merchant account name.
   * @param senderId Account name.
   * @param amount Amount of coins.
   * @return Transaction for buying.
   */
  @SaveTransaction(comment = "Buying", type = EXPENSE)
  public Transaction buy(
      @NonNull String senderId, @NonNull String recipientId, @NonNull BigDecimal amount) {
    return transferWithTransaction(senderId, recipientId, amount, NON_EXPIRABLE);
  }

  /**
   * Move coins.
   *
   * @param senderId From account.
   * @param recipientId To account.
   * @param amount Amount.
   * @return Transaction of moved coins.
   */
  @SuppressWarnings("unused")
  @SaveTransaction(comment = "Transfer money", type = TRANSFER)
  public synchronized Transaction move(
      @NonNull String senderId, @NonNull String recipientId, @NonNull BigDecimal amount) {
    return transferWithTransaction(senderId, recipientId, amount, EXPIRABLE);
  }

  /**
   * Get amount of account by email.
   *
   * @param email Email address.
   * @return Amount of account.
   */
  public BigDecimal getAmount(String email) {
    InvokeResponseDTO.Balance balanceOf = fabricService.query(
        email,
        Chaincode.COINS,
        FabricCoinsFunction.BALANCE_OF,
        new String[]{USER_PREFIX, email},
        InvokeResponseDTO.Balance.class);
    return balanceOf.getPayload().getBalance();
  }

  /**
   * Get amounts of accounts.
   *
   * @param accounts Account list.
   * @return Amount list.
   */
  public List<BalancesDTO> getAmounts(List<Account> accounts) {
    List<String> emails = accounts.stream()
        .map(Account::getEmail)
        .collect(Collectors.toList());

    InvokeResponseDTO.Balances balanceOf = fabricService.query(
        applicationProperties.getTreasury().getAccount(),
        Chaincode.COINS,
        FabricCoinsFunction.BATCH_BALANCE_OF,
        emails,
        InvokeResponseDTO.Balances.class);
    return balanceOf.getPayload();
  }

  /**
   * Get treasury amount.
   *
   * @return Treasury amount.
   */
  public BigDecimal getTreasuryAmount() {
    return getAmount(applicationProperties.getTreasury().getAccount());
  }

  /**
   * Get amount by account type.
   *
   * @param accountType Account type.
   * @return Amount of account type.
   */
  public BigDecimal getAmountByAccountType(AccountType accountType) {
    return Optional.ofNullable(accountsService.getAll(accountType))
        .map(accounts -> accounts.stream()
            .map(Account::getEmail)
            .map(this::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add))
        .orElse(BigDecimal.ZERO);
  }

  /**
   * Rollback transaction.
   *
   * @param txId Transaction ID.
   * @return New reverse transaction.
   */
  @SaveTransaction(comment = "Rollback previous transaction.", type = ROLLBACK)
  public Transaction rollback(Long txId) {
    Transaction transaction = transactionRepository.findOne(txId);
    Account user = transaction.getAccount();
    Account merchant = transaction.getDestination();
    BigDecimal amount = transaction.getAmount();
    InvokeResponseDTO.Balance move = transfer(merchant.getEmail(), user.getEmail(), amount, EXPIRABLE);
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

  /**
   * Move coins from account to treasury.
   *
   * @param accountName Account name (LDAP ID).
   * @param amount Amount of coins.
   * @param comment Comment for transaction.
   * @return Formed transaction - reflection of moving coins.
   */
  @SuppressWarnings("unused")
  @SaveTransaction(comment = "Move money from merchant account to treasury.", type = TRANSFER)
  public synchronized Transaction moveToTreasury(
      String accountName, BigDecimal amount, String comment
  ) {
    checkAmountIsPositive(amount);

    Account account = removeIsNewStatus(accountName);

    if (!isEnoughAmount(account.getEmail(), amount)) {
      throw new NotEnoughAmountInAccountException();
    }

    InvokeResponseDTO.Balance move = transfer(
        account.getEmail(),
        applicationProperties.getTreasury().getAccount(),
        amount, NON_EXPIRABLE);

    return Transaction.builder()
        .transactionId(move.getTransactionID())
        .account(account)
        .amount(amount)
        .status(TransactionStatus.SUCCESS)
        .remain(move.getPayload().getBalance())
        .build();
  }

  /**
   * Check if amount of account is enough.
   *
   * @param accountEmail Account name.
   * @param amount Needed amount.
   */
  private void checkEnoughAmount(String accountEmail, BigDecimal amount) {
    checkAmountIsPositive(amount);
    BigDecimal currentAmount = getAmount(accountEmail);
    if (currentAmount.compareTo(amount) < 0) {
      throw new NotEnoughAmountInAccountException();
    }
  }

  private Transaction transferWithTransaction(
      String senderId, String recipientId, BigDecimal amount, boolean expirable) {
    synchronized (getMonitor(senderId)) {
      Account donorAccount = removeIsNewStatus(senderId);
      Account acceptorAccount = removeIsNewStatus(recipientId);

      checkEnoughAmount(donorAccount.getEmail(), amount);

      InvokeResponseDTO.Balance balanceResponse =
          transfer(donorAccount.getEmail(), acceptorAccount.getEmail(), amount, expirable);

      Transaction transaction = new Transaction();
      transaction.setAccount(donorAccount);
      transaction.setDestination(acceptorAccount);
      transaction.setAmount(amount);
      transaction.setStatus(TransactionStatus.SUCCESS);
      transaction.setTransactionId(balanceResponse.getTransactionID());
      transaction.setRemain(balanceResponse.getPayload().getBalance());

      return transaction;
    }
  }

  /**
   * Move coins.
   *
   * @param senderEmail From entity.
   * @param recipientEmail To entity.
   * @param amount Amount of coins.
   * @return Balance response.
   */
  private InvokeResponseDTO.Balance transfer(
      String senderEmail, String recipientEmail, BigDecimal amount, Boolean expirable
  ) {
    return fabricService.invoke(
        senderEmail,
        Chaincode.COINS,
        FabricCoinsFunction.TRANSFER,
        new String[]{
            USER_PREFIX, recipientEmail, amount.toBigInteger().toString(), expirable.toString()},
        InvokeResponseDTO.Balance.class);
  }

  /**
   * Remove 'is new' flag from account.
   *
   * @param ldapId Account LDAP ID.
   * @return Account with removed 'is new' flag.
   */
  private Account removeIsNewStatus(final String ldapId) {
    Account account = Optional.of(accountsService.getAccount(ldapId))
        .orElseThrow(() -> new AccountNotFoundException(ldapId));

    if (account.isNew()) {
      account = accountsService.changeIsNewStatus(false, account);
    }

    return account;
  }

  /**
   * Remove 'is new' flag for list of account.
   *
   * @param inAccounts List of accounts.
   */
  private void removeIsNewStatus(List<Account> inAccounts) {
    Optional
        .ofNullable(inAccounts)
        .map(accounts -> accountsService.changeIsNewStatus(false, accounts))
        .orElseThrow(() -> new AccountNotFoundException(""));
  }

  /**
   * Check if account has enough coins.
   *
   * @param from Account.
   * @param amount Needed amount.
   * @return Whether account has needed amount.
   */
  private boolean isEnoughAmount(@NonNull String from, BigDecimal amount) {
    return getAmount(from).compareTo(amount) >= 0;
  }

  /**
   * Check if amount is positive.
   *
   * @param amount Amount to check.
   */
  private void checkAmountIsPositive(@NonNull BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount can't be negative");
    }
  }

  /**
   * Get monitor for account ID.
   *
   * @param accountId Account ID.
   * @return Monitor.
   */
  private synchronized String getMonitor(String accountId) {
    monitors.putIfAbsent(accountId, accountId);
    return monitors.get(accountId);
  }
}
