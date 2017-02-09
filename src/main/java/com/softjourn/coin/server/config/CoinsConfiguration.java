package com.softjourn.coin.server.config;

import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.eris.accounts.AccountsService;
import com.softjourn.eris.accounts.KeyService;
import com.softjourn.eris.rpc.HTTPRPCClient;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class CoinsConfiguration extends ResourceServerConfigurerAdapter {

    @Value("${authPublicKeyFile}")
    private String authPublicKeyFile;

    @Value("${eris.chain.url}")
    private String erisChainUrl;

    @Value("${eris.treasury.account.key.private}")
    private String treasuryAccountPrivKey;

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
        defaultTokenServices.setTokenEnhancer(accessTokenConverter());
        return defaultTokenServices;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String readPublicKey(String authPublicKeyFile) {
        try (InputStream inputStream = new ClassPathResource(authPublicKeyFile).getInputStream()) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read auth public key from file " + authPublicKeyFile);
        }
    }

    @Bean
    public AccountsService erisAccountsCreatingService() {
        KeyService keyService = new KeyService();
        return new AccountsService(keyService, treasuryAccountPrivKey, new HTTPRPCClient(erisChainUrl));
    }

    @Bean(name = "transactionResultMap")
    public Map<String, List<Future<Transaction>>> map() {
        return new HashMap<>();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
                .csrf().disable();
    }

}
