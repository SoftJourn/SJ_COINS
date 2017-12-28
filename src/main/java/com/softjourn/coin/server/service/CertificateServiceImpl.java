package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CertificateDTO;
import com.softjourn.coin.server.entity.FabricAccount;
import com.softjourn.common.auth.OAuthHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateServiceImpl implements CertificateService {

    private String certificateServer;
    private OAuthHelper oAuthHelper;

    public CertificateServiceImpl(@Value("${certificate.server.url}") String certificateServer,
                                  OAuthHelper oAuthHelper) {
        this.certificateServer = certificateServer;
        this.oAuthHelper = oAuthHelper;
    }

    @Override
    public CertificateDTO generate(String ldap) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", ldap);
        String token = oAuthHelper
                .getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<?> request = new HttpEntity<Object>(parameters, headers);
        return restTemplate.postForEntity(this.certificateServer, request, CertificateDTO.class).getBody();
    }

    @Override
    public FabricAccount newFabricAccount(String ldap) {
        CertificateDTO certificate = generate(ldap);
        FabricAccount account = new FabricAccount();
        account.setCertificate(certificate.getCertificate());
        account.setPubKey(certificate.getPublicKey());
        account.setPrivKey(certificate.getPrivateKey());
        return account;
    }

}
