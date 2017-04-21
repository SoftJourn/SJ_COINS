package com.softjourn.coin.server.service;

import com.softjourn.coin.server.controller.TransactionsController;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.entity.TransactionType;
import com.softjourn.coin.server.entity.Type;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.TxParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

import static com.softjourn.coin.server.service.GenericFilter.Condition.eq;

@Service
public class TransactionsService implements TransactionMapper {

    TransactionRepository repository;

    @Autowired
    public TransactionsService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Page<Transaction> getFiltered(GenericFilter<Transaction> filter, Pageable pageable) {
        return repository.findAll(filter, pageable);
    }

    public Transaction get(Long id) {
        return repository.findOne(id);
    }

    public Page<MobileTransactionDTO> getForUser(String user, Pageable pageable, TransactionsController.Direction direction) {
        GenericFilter<Transaction> fromFilter = getFilter(direction, user);
        Page<Transaction> transactions = repository.findAll(fromFilter, pageable);
        return transactions.map(MobileTransactionDTO::new);
    }

    public Page<Transaction> getTransactionsByTypeAndTime(TransactionType type, String start, String due, Pageable pageable) {
        try {
            Instant startTimestamp = Instant.parse(start);
            Instant dueTimestamp = Instant.parse(due);
            return this.repository.getByTypeAndTime(type, startTimestamp, dueTimestamp, pageable);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Datetime field should be in ISO format(Example: 2016-10-06T04:00:00Z)");
        }
    }

    private GenericFilter<Transaction> getFilter(TransactionsController.Direction direction, String user) {
        switch (direction) {
            case IN:
                return GenericFilter.or(eq("destination", user));
            case OUT:
                return GenericFilter.or(eq("account", user));
            default:
                return GenericFilter.or(eq("account", user), eq("destination", user));
        }
    }

    /**
     * Method prepares Transaction object
     *
     * @param o
     * @param erisTransactionId
     * @param comment
     * @return Transaction
     */
    public Transaction prepareTransaction(Object o, String erisTransactionId, String comment) {
        Transaction<Object> transaction = new Transaction<>(erisTransactionId);
        transaction.setComment(comment);
        transaction.setAmount(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreated(Instant.now());
        transaction.setValue(o);
        return transaction;
    }

    /**
     * Method maps eris response to transaction
     *
     * @param response
     * @return Transaction
     */
    public Transaction mapToTransaction(Response response) {
        return Optional.ofNullable(response)
                .map(Response::getTxParams)
                .map(TxParams::getTxId)
                .map(Transaction::new)
                .orElseGet(Transaction::new);
    }

}
