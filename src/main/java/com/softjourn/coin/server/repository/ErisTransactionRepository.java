package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import org.springframework.data.repository.CrudRepository;

/**
 * Created for transaction history
 * Created by vromanchuk on 17.01.17.
 */
public interface ErisTransactionRepository extends CrudRepository<ErisTransactionDAO, Long> {
}
