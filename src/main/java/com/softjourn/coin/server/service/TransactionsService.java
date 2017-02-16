package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<Transaction> getForUser(String user, Pageable pageable) {
        GenericFilter<Transaction> fromFilter = GenericFilter.or(eq("account", user), eq("destination", user));
        return repository.findAll(fromFilter, pageable);
    }
}
