package com.softjourn.coin.server.service;


import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
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

    private AccountRepository accountRepository;

    private RestTemplate restTemplate;

    @Autowired
    public AccountsService(AccountRepository accountRepository, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${auth.server.url}")
    private String authServerUrl;

    public boolean isAccountExistInLdapBase(String ldapId) {
        return restTemplate.getForEntity(authServerUrl + "/users/" + ldapId + "/exist", Boolean.class).getBody();
    }

    public List<Account> getAll() {
        return StreamSupport
                .stream(accountRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public Account getAccount(String ldapId) {
        return Optional
                .ofNullable(accountRepository.findOne(ldapId))
                .orElseThrow(() -> new AccountNotFoundException(ldapId));
    }

    public Account add(String ldapId) {
        Account account = accountRepository.findOne(ldapId);
        if (account == null) return createAccount(ldapId);
        else return account;
    }

    private Account createAccount(String ldapId) {
        if (isAccountExistInLdapBase(ldapId)) {
            Account newAccount = new Account(ldapId, new BigDecimal(0));
            accountRepository.save(newAccount);
            return newAccount;
        } else {
            throw new AccountNotFoundException(ldapId);
        }
    }
}
