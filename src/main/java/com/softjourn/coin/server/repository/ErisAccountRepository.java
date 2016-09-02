package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.entity.ErisAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

/**
 * Created by volodymyr on 8/30/16.
 */
public interface ErisAccountRepository extends CrudRepository<ErisAccount,Long> {

    @Query("SELECT ea FROM ErisAccount ea WHERE ea.account = null")
    public Stream<ErisAccount> getFree();

}
