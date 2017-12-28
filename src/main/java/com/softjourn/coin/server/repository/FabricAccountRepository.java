package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.entity.FabricAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface FabricAccountRepository extends CrudRepository<FabricAccount,Long> {

}
