package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, String> {

    @Query("SELECT a FROM Account a")
    List<Account> getAll();
}
