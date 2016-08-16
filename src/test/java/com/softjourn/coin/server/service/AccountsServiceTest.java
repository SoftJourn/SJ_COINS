package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
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

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountsService accountsService;

    @Before
    public void setUp() throws Exception {

        ReflectionTestUtils.setField(accountsService, "authServerUrl", "http://test.com");

        when(restTemplate.getForEntity(anyString(), any()))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        when(restTemplate.getForEntity("http://test.com/users/testLdapId/exist", Boolean.class))
                .thenReturn(new ResponseEntity<>(false, HttpStatus.OK));


        Account account = new Account("testId", new BigDecimal(100));
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

        when(accountRepository.findOne("testId")).thenReturn(account);

        when(accountRepository.save(any(Account.class))).thenAnswer(new Answer<Account>() {
            @Override
            public Account answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Account)args[0];
            }
        });

    }

    @Test
    public void isAccountExistInLdapBase() throws Exception {
        assertTrue(accountsService.isAccountExistInLdapBase("ldapId"));
        assertFalse(accountsService.isAccountExistInLdapBase("testLdapId"));
    }

    @Test
    public void getAll() throws Exception {
        assertEquals(1, accountsService.getAll().size());
        assertEquals(new BigDecimal(100), accountsService.getAll().get(0).getAmount());
    }

    @Test
    public void getAccount() throws Exception {
        assertNotNull(accountsService.getAccount("testId"));
    }

    @Test(expected = AccountNotFoundException.class)
    public void getNotExistingAccount() {
        accountsService.getAccount("notExistingId");
    }

    @Test
    public void add() throws Exception {
        assertEquals(100, accountsService.add("testId").getAmount().intValue());
        assertEquals(0, accountsService.add("nonExistingAccount").getAmount().intValue());
    }

    @Test(expected = AccountNotFoundException.class)
    public void addAccountThatNotExistsInLDAPBase() throws Exception {
        accountsService.add("testLdapId");
    }

}