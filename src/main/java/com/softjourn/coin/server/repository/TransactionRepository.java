package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;


public interface TransactionRepository extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Override
    <T extends Transaction> T save(T transaction);

    @Query("select t from Transaction t where t.type = ?1 and t.created >= ?2 and t.created <= ?3")
    Page<Transaction> getByTypeAndTime(TransactionType type, Instant start, Instant due, Pageable pageable);

}
