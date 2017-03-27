package com.softjourn.coin.server.service;

import com.softjourn.coin.server.controller.TransactionsController;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.softjourn.coin.server.service.GenericFilter.Condition.eq;

@Service
public class TransactionsService {

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

    private GenericFilter<Transaction> getFilter(TransactionsController.Direction direction, String user) {
        switch (direction) {
            case IN: return GenericFilter.or(eq("destination", user));
            case OUT: return GenericFilter.or(eq("account", user));
            default: return GenericFilter.or(eq("account", user), eq("destination", user));
        }
    }

    /**
     * Metrod prepares Transaction object
     * @param o
     * @param erisTransactionId
     * @param comment
     * @return Transaction
     */
    public static Transaction prepareTransaction(Object o, String erisTransactionId, String comment) {
        Transaction<Object> transaction = new Transaction<>(erisTransactionId);
        transaction.setComment(comment);
        transaction.setAmount(null);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreated(Instant.now());
        transaction.setValue(o);
        return transaction;
    }

}
