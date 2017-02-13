package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.entity.TransactionStoring;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created for body history
 * Created by vromanchuk on 17.01.17.
 */
public interface ErisTransactionRepository extends JpaRepository<TransactionStoring, Long> {
    TransactionStoring findFirstByOrderByBlockNumberDesc();
}
