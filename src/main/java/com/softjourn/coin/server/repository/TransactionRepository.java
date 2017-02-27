package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;


public interface TransactionRepository extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Override
    <T extends Transaction> T save(T transaction);
}
