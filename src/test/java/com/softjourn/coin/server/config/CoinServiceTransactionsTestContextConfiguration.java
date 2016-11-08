package com.softjourn.coin.server.config;


import com.softjourn.coin.server.service.ErisContractService;
import com.softjourn.eris.accounts.AccountsService;
import com.softjourn.eris.accounts.KeyService;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.ReturnValue;
import com.softjourn.eris.contract.response.TxParams;
import com.softjourn.eris.rpc.HTTPRPCClient;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Configuration
@ComponentScan(basePackages = "com.softjourn.coin.server.service",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {ErisContractService.class})
)
@EnableJpaRepositories(basePackages = "com.softjourn.coin.server.repository", entityManagerFactoryRef="entityManagerFactory")
@EntityScan(basePackages = "com.softjourn.coin.server.entity")
@EnableTransactionManagement
@PropertySource(value= {"classpath:application-test.properties"})
public class CoinServiceTransactionsTestContextConfiguration {

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public EmbeddedDatabase dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("coins_schema.sql")
                .addScript("coins_values.sql")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("package.where.your.entites.like.CustSys.are.stored");
        em.setPersistenceProvider(new HibernatePersistenceProvider());
        return em;
    }

    @Bean
    public AccountsService erisAccountsCreatingService() {
        KeyService keyService = new KeyService();
        return new AccountsService(keyService, "", new HTTPRPCClient("http://127.0.0.1"), "");
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

    @Bean
    public ErisContractService contractService() throws IOException {
        ErisContractService contractService = mock(ErisContractService.class);
        Contract contract = mock(Contract.class);
        when(contractService.getForAccount(any())).thenReturn(contract);
        doNothing().when(contractService).initContract();

        Response<Object> getResp = new Response<>("",
                new ReturnValue<>(Object.class, BigInteger.valueOf(500L)),
                null,
                null);

        Response<Object> sendResp = new Response<>("",
                null,
                null,
                new TxParams("address", "txId"));

        when(contract.call(eq("queryBalance")))
                .thenReturn(getResp);

        when(contract.call(eq("send"), anyVararg()))
                .thenReturn(sendResp);

        return contractService;

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
