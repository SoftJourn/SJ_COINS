package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType")
    List<Account> getAccountsByType(@Param("accountType") AccountType accountType);
}
