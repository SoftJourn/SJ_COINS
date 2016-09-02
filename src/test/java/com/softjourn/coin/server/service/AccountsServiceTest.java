package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;

import static junit.framework.TestCase.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountsServiceTest {

    private static final String NOT_EXISTING_LDAP_ID = "notExist";
    private static final String ID_EXISTING_IN_DB = "existsInDB";
    private static final String EXISTING_LDAP_ID = "ldapId";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    ErisAccountsService erisAccountsService;

    @Mock
    ErisAccountRepository erisAccountRepository;

    @InjectMocks
    private AccountsService accountsService;

    @Before
    public void setUp() throws Exception {

        ReflectionTestUtils.setField(accountsService, "authServerUrl", "http://test.com");

        when(restTemplate.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity<>(new Account(), HttpStatus.OK));

        when(restTemplate.getForEntity("http://test.com/users/" + NOT_EXISTING_LDAP_ID, Account.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));


        Account account = new Account(ID_EXISTING_IN_DB, new BigDecimal(100));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

        when(accountRepository.findOne(ID_EXISTING_IN_DB)).thenReturn(account);

        when(accountRepository.save(any(Account.class))).thenAnswer(new Answer<Account>() {
            @Override
            public Account answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Account)args[0];
            }
        });

        when(erisAccountsService.bindFreeAccount()).thenReturn(new ErisAccount());

    }

    @Test
    public void isAccountExistInLdapBase() throws Exception {
        assertNotNull(accountsService.getAccountIfExistInLdapBase(EXISTING_LDAP_ID));
        assertNull(accountsService.getAccountIfExistInLdapBase(NOT_EXISTING_LDAP_ID));
    }

    @Test
    public void getAll() throws Exception {
        assertEquals(1, accountsService.getAll().size());
        assertEquals(new BigDecimal(100), accountsService.getAll().get(0).getAmount());
    }

    @Test
    public void getAccount() throws Exception {
        assertNotNull(accountsService.getAccount(ID_EXISTING_IN_DB));
    }

    @Test()
    public void getNotExistingAccountThatExistsInLDAP() {
        assertNotNull(accountsService.getAccount(EXISTING_LDAP_ID));
        assertEquals(0, accountsService.getAccount(EXISTING_LDAP_ID).getAmount().intValue());
    }

    @Test(expected = AccountNotFoundException.class)
    public void getNotExistingAccount() {
        accountsService.getAccount(NOT_EXISTING_LDAP_ID);
    }

    @Test
    public void add() throws Exception {
        assertEquals(100, accountsService.add(ID_EXISTING_IN_DB).getAmount().intValue());
        assertEquals(0, accountsService.add(EXISTING_LDAP_ID).getAmount().intValue());
    }

    @Test(expected = AccountNotFoundException.class)
    public void addAccountThatNotExistsInLDAPBase() throws Exception {
        accountsService.add(NOT_EXISTING_LDAP_ID);
    }

}