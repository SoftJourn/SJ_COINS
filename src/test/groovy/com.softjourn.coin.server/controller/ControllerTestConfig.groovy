package com.softjourn.coin.server.controller

import com.softjourn.coin.server.dto.ContractCreateResponseDTO
import com.softjourn.coin.server.dto.MerchantDTO
import com.softjourn.coin.server.dto.NewContractDTO
import com.softjourn.coin.server.dto.NewContractInstanceDTO
import com.softjourn.coin.server.entity.Account
import com.softjourn.coin.server.entity.AccountType
import com.softjourn.coin.server.entity.Contract
import com.softjourn.coin.server.entity.Instance
import com.softjourn.coin.server.entity.Transaction
import com.softjourn.coin.server.entity.TransactionStatus
import com.softjourn.coin.server.entity.Type
import com.softjourn.coin.server.service.AccountsService
import com.softjourn.coin.server.service.CoinService
import com.softjourn.coin.server.service.ContractService
import org.apache.commons.io.IOUtils
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import org.springframework.test.context.web.WebAppConfiguration

import java.time.Instant

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyLong
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.when

@Configuration
@ComponentScan(basePackages = ['com.softjourn.coin.server'])
@EnableAspectJAutoProxy(proxyTargetClass = true)
@WebAppConfiguration
class ControllerTestConfig {

    @Bean
    CoinService coinService() {
        def coinService = Mockito.mock(CoinService.class)

        def account = new Account('account1', 100)
        def account2 = new Account('account2', 100)
        def vm = new Account('VM1', 100)
        def treasury = new Account('Treasury', 100)

        def transaction = createTransaction(account, account)
        def transactionMove = createTransaction(account, account2)
        def transactionMove2 = createTransaction(vm, treasury)

        when(coinService.getAmount(any(String.class))).thenReturn(new BigDecimal('100'))
        when(coinService.buy(any(String.class), any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transaction)
        when(coinService.fillAccount(any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transaction)
        when(coinService.move(any(String.class), any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transactionMove)
        when(coinService.moveToTreasury(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(transactionMove2)

        coinService
    }

    @Bean
    AccountsService accountsService() {
        def accountsService = Mockito.mock(AccountsService.class)
        def account1 = new Account("user1", 1000)
        account1.fullName = "Bruce Wayne"
        account1.image = "images/default.png"

        def account2 = new Account("user2", 1500)
        account2.fullName = "Josh Long"
        account2.image = "images/default.png"

        def seller = new Account("123456-123456-123456", 0)
        seller.fullName = "VM1"
        seller.accountType = AccountType.MERCHANT

        when(accountsService.getAccount(anyString())).thenReturn(account1)

        when(accountsService.addMerchant(any(MerchantDTO.class))).thenReturn(seller)

        when(accountsService.getAll()).thenReturn([account1, account2])

        when(accountsService.getAll(AccountType.MERCHANT)).thenReturn([seller])

        when(accountsService.delete(anyString())).thenReturn(true)

        accountsService
    }

    @Bean
    ContractService contractService() {
        def contractService = Mockito.mock(ContractService.class)
        def contract = new Contract(1L, "some name", "some code", "some abi", new Type("type"), new ArrayList<Instance>() {
            {
                add(new Instance("SomeAddress"))
            }
        })
        when(contractService.newContract(any() as NewContractDTO)).thenReturn(new ContractCreateResponseDTO(1, "contract", "type", "some address"))
        when(contractService.getContracts()).thenReturn([contract])
        when(contractService.newInstance(any() as NewContractInstanceDTO)).thenReturn(new ContractCreateResponseDTO(1, "contract", "type", "some address"))
        when(contractService.getInstances(anyLong())).thenReturn([new ContractCreateResponseDTO(1, "contract", "type", "some address")])

        contractService
    }

    private Transaction createTransaction(Account account, Account destinationAccount) {
        def transaction = new Transaction()
        transaction.account = account
        transaction.amount = 10
        transaction.comment = 'Some comment'
        transaction.created = Instant.now()
        transaction.destination = destinationAccount
        transaction.id = 1
        transaction.status = TransactionStatus.SUCCESS

        transaction
    }

    protected static class ResourceServerConfig extends ResourceServerConfigurerAdapter {
        @Value('${authPublicKeyFile}')
        private String authPublicKeyFile;

        @Bean
        public TokenStore tokenStore() {
            return new JwtTokenStore(accessTokenConverter());
        }

        @Bean
        public JwtAccessTokenConverter accessTokenConverter() {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            String publicKey = readPublicKey(authPublicKeyFile);
            converter.setVerifierKey(publicKey);
            return converter;
        }

        @Bean
        public DefaultTokenServices tokenServices() {
            DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
            defaultTokenServices.setTokenStore(tokenStore());
            return defaultTokenServices;
        }

        private String readPublicKey(String authPublicKeyFile) {
            def inputStream = new ClassPathResource(authPublicKeyFile).getInputStream()

            try {
                return IOUtils.toString(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Can't read auth public key from file " + authPublicKeyFile);
            } finally {
                if (inputStream) {
                    inputStream.close()
                }
            }
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/api/v1/add/**").hasRole("COIN_ADMIN")
                    .anyRequest().authenticated()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
                    .and()
                    .csrf().disable()
            ;
        }

        @Override
        void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.tokenServices(tokenServices()).stateless(false)
        }
    }
}
