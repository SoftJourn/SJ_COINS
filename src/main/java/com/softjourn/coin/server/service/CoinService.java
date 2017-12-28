package com.softjourn.coin.server.service;


import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.blockchain.network.ChainNetwork;
import com.softjourn.coin.server.blockchain.network.Chaincode;
import com.softjourn.coin.server.blockchain.network.Organization;
import com.softjourn.coin.server.blockchain.network.User;
import com.softjourn.coin.server.chainImpl.ChainUser;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.FabricAccount;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.exceptions.InvalidTransactionProposalException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInAccountException;
import com.softjourn.coin.server.repository.TransactionRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.entity.TransactionType.EXPENSE;
import static com.softjourn.coin.server.entity.TransactionType.REGULAR_REPLENISHMENT;
import static com.softjourn.coin.server.entity.TransactionType.ROLLBACK;
import static com.softjourn.coin.server.entity.TransactionType.SINGLE_REPLENISHMENT;
import static com.softjourn.coin.server.entity.TransactionType.TRANSFER;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    private Map<String, String> monitors = new HashMap<>();

    private ChainUser sjCoinAccount;

    private TransactionRepository transactionRepository;

    private ChainNetwork chainNetwork;

    private HFClient client;

    private Channel channel;

    private ChaincodeID chaincodeID;


    @SuppressWarnings("unused")
    @Autowired
    public CoinService(AccountsService accountsService,
                       TransactionRepository transactionRepository,
                       ChainNetwork chainNetwork,
                       TransactionMapper mapper,
                       HFClient client,
                       Channel channel) {
        this.accountsService = accountsService;
        this.transactionRepository = transactionRepository;
        this.chainNetwork = chainNetwork;
        this.client = client;
        this.channel = channel;
    }

    @PostConstruct
    private void setUp() {
        Organization organization = chainNetwork.getOrganization();
        User user = organization.getUser();
        sjCoinAccount = new ChainUser(user.getName(),
                user.getCertificate(),
                user.getPrivateKey(),
                organization.getMsp());

        this.chaincodeID = ChaincodeID.newBuilder().setName(chainNetwork.getChaincode().getName())
                .setVersion(chainNetwork.getChaincode().getVersion())
                .setPath(chainNetwork.getChaincode().getPathToFile()).build();
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Moving money to user.", type = SINGLE_REPLENISHMENT)
    public Transaction fillAccount(@NonNull String destinationName,
                                   @NonNull BigDecimal amount,
                                   String comment) throws InterruptedException, InvalidArgumentException, ProposalException, ExecutionException {
        synchronized (getMonitor(destinationName)) {
            checkAmountIsPositive(amount);

            Account account = removeIsNewStatus(destinationName);

            return moveByFabric(sjCoinAccount, account.getLdapId(), amount);
        }
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Distributing money.", type = REGULAR_REPLENISHMENT)
    public Transaction distribute(BigDecimal amount, String comment) throws ExecutionException, InterruptedException, InvalidArgumentException, ProposalException {
        List<Account> accounts = accountsService.getAll(AccountType.REGULAR);

        removeIsNewStatus(accounts);

        List<String> accountsIds = accounts.stream()
                .map(Account::getLdapId)
                .collect(Collectors.toList());

        Chaincode chaincode = this.chainNetwork.getChaincode();
        Organization organization = this.chainNetwork.getOrganization();
        client.setUserContext(sjCoinAccount);

        final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincode.getName())
                .setVersion(chaincode.getVersion())
                .setPath(chaincode.getPathToFile()).build();

        accountsIds.add(amount.toString());

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn("distribute");
        transactionProposalRequest.setArgs(accountsIds.toArray(new String[0]));
        transactionProposalRequest.setTransientMap(prepareTransactionProposal());

        List<ProposalResponse> proposalResponses = new ArrayList<>(channel.sendTransactionProposal(transactionProposalRequest));

        boolean successful = true;
        for (ProposalResponse response : proposalResponses) {
            if (response.isInvalid()) {
                successful = false;
            }
        }
        if (successful) {
            CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture =
                    channel.sendTransaction(proposalResponses, channel.getOrderers());

            try {
                transactionEventCompletableFuture.get(1l, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
            }
        } else {
            throw new InvalidTransactionProposalException("Transaction proposal is invalid");
        }
        proposalResponses.get(0).getTransactionID();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(proposalResponses.get(0).getTransactionID());

        return transaction;

    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Transfer money", type = TRANSFER)
    public synchronized Transaction move(@NonNull String accountName,
                                         @NonNull String destinationName,
                                         @NonNull BigDecimal amount,
                                         String comment) throws InterruptedException, InvalidArgumentException, ProposalException, ExecutionException {
        checkAmountIsPositive(amount);

        Account donorAccount = removeIsNewStatus(accountName);

        FabricAccount donor = getFabricAccount(donorAccount);

        Account acceptorAccount = removeIsNewStatus(destinationName);

        FabricAccount acceptor = getFabricAccount(acceptorAccount);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }

        Organization organization = this.chainNetwork.getOrganization();

        return moveByFabric(new ChainUser(donorAccount.getLdapId(),
                donor.getCertificate(),
                donor.getPrivKey(),
                organization.getMsp()), acceptor.getAccount().getLdapId(), amount);
    }


    public BigDecimal getAmount(String ldapId) {
        try {
            Organization organization = this.chainNetwork.getOrganization();
            Account account = this.accountsService.getAccount(ldapId);
            return getAmountForFabricAccount(new ChainUser(account.getLdapId(),
                    account.getFabricAccount().getCertificate(),
                    account.getFabricAccount().getPrivKey(),
                    organization.getMsp()));
        } catch (Exception e) {
            throw new ErisProcessingException("Can't query balance for account " + ldapId, e);
        }
    }

    public BigDecimal getTreasuryAmount() throws InvalidArgumentException, ProposalException {
        return getAmountForFabricAccount(sjCoinAccount);
    }

    public BigDecimal getAmountByAccountType(AccountType accountType) {
        Organization organization = this.chainNetwork.getOrganization();
        return Optional.ofNullable(accountsService.getAll(accountType))
                .map(accounts -> accounts.stream()
                        .filter(account -> Objects.nonNull(account.getFabricAccount()))
                        .peek(account -> {
                            try {
                                account.setAmount(getAmountForFabricAccount(new ChainUser(account.getLdapId(),
                                        account.getFabricAccount().getCertificate(),
                                        account.getFabricAccount().getPrivKey(),
                                        organization.getMsp())));
                            } catch (InvalidArgumentException | ProposalException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(Account::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Buying", type = EXPENSE)
    public Transaction buy(@NonNull String destinationName, @NonNull String accountName, @NonNull BigDecimal amount, String comment) throws InterruptedException, InvalidArgumentException, ProposalException, ExecutionException {
        synchronized (getMonitor(accountName)) {
            checkEnoughAmount(accountName, amount);

            Account account = removeIsNewStatus(accountName);

            FabricAccount fabricAccount = getFabricAccount(account);
            Account merchantAccount = removeIsNewStatus(destinationName);
            Organization organization = this.chainNetwork.getOrganization();

            return moveByFabric(new ChainUser(account.getLdapId(),
                    fabricAccount.getCertificate(),
                    fabricAccount.getPrivKey(),
                    organization.getMsp()), merchantAccount.getLdapId(), amount);
        }
    }

    @SaveTransaction(comment = "Rollback previous transaction.", type = ROLLBACK)
    public Transaction rollback(Long txId) throws InterruptedException, InvalidArgumentException, ProposalException, ExecutionException {
        Transaction transaction = transactionRepository.findOne(txId);
        Account user = transaction.getAccount();
        Account merchant = transaction.getDestination();
        BigDecimal amount = transaction.getAmount();
        Organization organization = this.chainNetwork.getOrganization();

        Transaction rollbackTx = moveByFabric(new ChainUser(merchant.getLdapId(),
                merchant.getFabricAccount().getCertificate(),
                merchant.getFabricAccount().getPrivKey(),
                organization.getMsp()), user.getLdapId(), amount);

        rollbackTx.setAccount(merchant);
        rollbackTx.setDestination(user);
        rollbackTx.setAmount(amount);
        rollbackTx.setComment("Rollback buying transaction. ID: " + txId);
        return rollbackTx;
    }

    private void checkEnoughAmount(String accountName, BigDecimal amount) {
        checkAmountIsPositive(amount);

        BigDecimal currentAmount = getAmount(accountName);

        if (currentAmount.compareTo(amount) < 0) {
            throw new NotEnoughAmountInAccountException();
        }
    }

    private Transaction moveByFabric(ChainUser chainUser, String ldap, BigDecimal amount)
            throws InvalidArgumentException, ProposalException, ExecutionException, InterruptedException {

        client.setUserContext(chainUser);

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(this.chaincodeID);
        transactionProposalRequest.setFcn("transfer");
        transactionProposalRequest.setArgs(new String[]{"user_", ldap, amount.toString()});
        transactionProposalRequest.setTransientMap(prepareTransactionProposal());

        List<ProposalResponse> proposalResponses = new ArrayList<>(channel.sendTransactionProposal(transactionProposalRequest));

        boolean successful = true;
        for (ProposalResponse response : proposalResponses) {
            if (response.isInvalid()) {
                successful = false;
            }
        }
        if (successful) {
            CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture =
                    channel.sendTransaction(proposalResponses, channel.getOrderers());

            try {
                transactionEventCompletableFuture.get(1l, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
            }
        } else {
            throw new InvalidTransactionProposalException("Transaction proposal is invalid");
        }
        proposalResponses.get(0).getTransactionID();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(proposalResponses.get(0).getTransactionID());

        return transaction;
    }

    private BigDecimal getAmountForFabricAccount(ChainUser chainUser) throws InvalidArgumentException, ProposalException {
        client.setUserContext(chainUser);

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(new String[]{chainUser.getName()});
        queryByChaincodeRequest.setFcn("balanceOf");
        queryByChaincodeRequest.setChaincodeID(this.chaincodeID);
        queryByChaincodeRequest.setTransientMap(prepareQueryByChaincode());

        List<ProposalResponse> proposalResponses = new ArrayList<>(channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers()));

        return new BigDecimal(new String(proposalResponses.get(0).getChaincodeActionResponsePayload()));
    }

    @SuppressWarnings("unused")
    @SaveTransaction(comment = "Move money from merchant account to treasury.", type = TRANSFER)
    public synchronized Transaction moveToTreasury(String accountName, BigDecimal amount, String comment) throws InterruptedException, InvalidArgumentException, ProposalException, ExecutionException {
        checkAmountIsPositive(amount);

        if (!isEnoughAmount(accountName, amount)) {
            throw new NotEnoughAmountInAccountException();
        }
        Account account = removeIsNewStatus(accountName);

        FabricAccount fabricAccount = getFabricAccount(account);

        Organization organization = this.chainNetwork.getOrganization();

        return moveByFabric(new ChainUser(account.getLdapId(),
                fabricAccount.getCertificate(),
                fabricAccount.getPrivKey(),
                organization.getMsp()), organization.getUser().getName(), amount);
    }

    private FabricAccount getFabricAccount(Account account) {
        return Optional
                .ofNullable(account)
                .map(Account::getFabricAccount)
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

    private static Map<String, byte[]> prepareTransactionProposal() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); //Just some extra junk in transient map
        tm.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
        return tm;
    }

    private static Map<String, byte[]> prepareQueryByChaincode() {
        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        return tm;
    }


}
