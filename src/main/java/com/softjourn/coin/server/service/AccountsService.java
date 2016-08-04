package com.softjourn.coin.server.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AccountsService {

    @Value("${auth.server.url}")
    private String authServerUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public boolean isAccountExistInLdapBase(String ldapId) {
        return restTemplate.getForEntity(authServerUrl + "/users/" + ldapId + "/exist", Boolean.class).getBody();
    }
}
