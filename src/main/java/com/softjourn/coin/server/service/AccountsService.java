package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AccountsService {

    /*
     * TODO
     * This method allow to ignore wrong certificates on testing.
     * It trust all certificates what is insecure.
     * Only for testing.
     * Remove for using in production environment.
     */
    static {
        disableSslVerification();
    }
    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {}

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}

                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static final String DEFAULT_IMAGE_NAME = "images/default.png";

    private AccountRepository accountRepository;

    @Autowired
    private ErisAccountsService erisAccountsService;

    private ErisAccountRepository erisAccountRepository;

    private RestTemplate restTemplate;

    public AccountsService(AccountRepository accountRepository, ErisAccountsService erisAccountsService, ErisAccountRepository erisAccountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.erisAccountsService = erisAccountsService;
        this.erisAccountRepository = erisAccountRepository;
        this.restTemplate = restTemplate;
    }

    @Autowired
    public AccountsService(AccountRepository accountRepository, ErisAccountRepository erisAccountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.erisAccountRepository = erisAccountRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${auth.server.url}")
    private String authServerUrl;

    public Account getAccountIfExistInLdapBase(String ldapId) {
        try {
            return restTemplate.getForEntity(authServerUrl + "/users/" + ldapId, Account.class).getBody();
        } catch (RestClientException rce) {
            return null;
        }
    }

    public List<Account> getAll() {
        return StreamSupport
                .stream(accountRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public Account getAccount(String ldapId) {
        return Optional
                .ofNullable(accountRepository.findOne(ldapId))
                .orElseGet(() -> createAccount(ldapId));
    }

    public Account add(String ldapId) {
        Account account = accountRepository.findOne(ldapId);
        if (account == null) return createAccount(ldapId);
        else return account;
    }

    public Account update(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public Account createAccount(String ldapId) {
        Account account = getAccountIfExistInLdapBase(ldapId);
        if (account != null) {
            account.setLdapId(ldapId);
            account.setAmount(new BigDecimal(0));
            account.setImage(DEFAULT_IMAGE_NAME);
            ErisAccount erisAccount = erisAccountsService.bindFreeAccount();
            if (erisAccount == null) throw new RuntimeException("Can't create account for " + ldapId + ". " +
                    "There is no free eris accounts");
            accountRepository.save(account);
            account.setErisAccount(erisAccount);
            erisAccount.setAccount(account);
            erisAccountRepository.save(erisAccount);
            return account;
        } else {
            throw new AccountNotFoundException(ldapId);
        }
    }
}
