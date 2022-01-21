package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.entity.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository
    extends CrudRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>
{

  @Override
  <T extends Transaction> T save(T transaction);

  @Query("SELECT t FROM Transaction t WHERE t.id = :id")
  Transaction findOne(@Param("id") Long id);
}
