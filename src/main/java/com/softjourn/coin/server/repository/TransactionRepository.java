package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Transaction;
import org.springframework.data.repository.CrudRepository;


public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Override
    <T extends Transaction> T save(T transaction);
}
