package com.softjourn.coin.server.repository;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.deleted = false")
    List<Account> getAccountsByType(@Param("accountType") AccountType accountType, Sort sort);

    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType")
    Page<Account> getAccountsByType(@Param("accountType") AccountType accountType, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.deleted = false")
    List<Account> findAllUndeleted();

    @Query("SELECT a FROM Account a WHERE a.deleted = true ")
    List<Account> findAllDeleted();

    @Query("SELECT a FROM Account a WHERE a.ldapId = :ldapId AND a.deleted = false")
    Account findOneUndeleted(@Param("ldapId") String ldapId);

    @Query("SELECT a FROM Account a WHERE a.fullName = ?1 AND a.deleted = FALSE")
    Account findOneByFullNameUndeleted(String fullName);

    @Modifying
    @Query("UPDATE Account a SET a.deleted = :isDeleted WHERE a.ldapId = :ldapId")
    int updateIsDeletedByLdapId(@Param("ldapId") String ldapId, @Param("isDeleted") boolean isDeleted);

    @Modifying
    @Query("UPDATE Account a SET a.isNew = :isNew WHERE a.ldapId IN (:ids)")
    int changeIsNewStatus(@Param("isNew") Boolean isNew, @Param("ids") List<String> ids);

    @Query("SELECT a FROM Account a WHERE a.ldapId like %?1% OR a.fullName like %?1% OR a.email like %?1%")
    Page<Account> findAccountBy(String value, Pageable pageable);
}
