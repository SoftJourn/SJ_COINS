package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Transaction;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.*;

import static org.junit.Assert.assertEquals;


@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase
public class AutocompleteServiceTest {

    @Autowired
    private EntityManager entityManager;

    private AutocompleteService<Transaction> autocompleteService;


    @Before
    public void setUp() throws Exception {
        autocompleteService = new AutocompleteService<>(Transaction.class, entityManager);
    }

    @Test
    public void getAutocomplete() throws Exception {
        List<String> expected  = Arrays.asList("vdanyliuk", "ovovchuk", "omartynets", "oyevchuk");

        Assert.assertThat((Iterable<String>)autocompleteService.getAutocomplete("account"), Matchers.containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void getAutocomplete_innerField() throws Exception {
        List<String> expected  = Arrays.asList("00099DE18B254BBE424E0344ACE2762128159937", "00099DE18B254BBE424E0333ACE2762128159937", "00099DE18B254BBE42455544ACE2762128159937", "00099DE18B2511BE424E0344ACE2762128159937");

        Assert.assertThat((Iterable<String>)autocompleteService.getAutocomplete("account.erisAccount.address"), Matchers.containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void getAllPaths_0() throws Exception {
        Map<String, Object> expected = new TreeMap<String, Object>() {{
            put("erisTransactionId", "text");
            put("amount", "number");
            put("comment", "text");
            put("created", "date");
            put("status", "text");
            put("error", "text");
            put("account", new TreeMap<String, Object>(){{
                put("ldapId", "text");
                put("amount", "number");
                put("fullName", "text");
                put("accountType", "text");
                put("isNew", "bool");
                put("deleted", "bool");
                put("erisAccount", new TreeMap<String, Object>(){{
                    put("address", "text");
                    put("pubKey", "text");
                    put("type", "text");
                }});
            }});
            put("destination", new TreeMap<String, Object>(){{
                put("ldapId", "text");
                put("amount", "number");
                put("fullName", "text");
                put("accountType", "text");
                put("isNew", "bool");
                put("deleted", "bool");
                put("erisAccount", new TreeMap<String, Object>(){{
                    put("address", "text");
                    put("pubKey", "text");
                    put("type", "text");
                }});
            }});

        }};
        assertEquals(expected, autocompleteService.getAllPaths(Transaction.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllPaths_2() throws Exception {
        autocompleteService.getAllPaths(String.class);
    }


}