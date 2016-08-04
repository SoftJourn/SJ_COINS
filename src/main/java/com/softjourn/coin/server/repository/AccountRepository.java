package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, String> {
}
