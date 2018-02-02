package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.EnrollResponseDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.exceptions.AccountEnrollException;
import com.softjourn.coin.server.exceptions.FabricRequestInvokeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class FabricServiceImpl implements FabricService {

    private String url;
    private String organization;
    private String[] peers;

    private RestTemplate template;

    @Autowired
    public FabricServiceImpl(@Value("${node.fabric.client}") String url,
                             @Value("${org.name}") String organization,
                             @Value("${fabric.peers}") String[] peers,
                             RestTemplate template) {
        this.url = url;
        this.organization = organization;
        this.peers = peers;
        this.template = template;

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8));
        messageConverters.add(converter);
        template.setMessageConverters(messageConverters);
    }

    public ResponseEntity<EnrollResponseDTO> enroll(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> request = new HashMap<>();
        request.put("username", email);
        request.put("orgName", organization);

        HttpEntity<?> httpEntity = new HttpEntity<Object>(request, headers);
        try {
            return template.postForEntity(this.url + "users", httpEntity, EnrollResponseDTO.class);
        } catch (Exception e) {
            throw new AccountEnrollException(e);
        }
    }

    @Override
    public <T> T invoke(String email, String function, String[] args, Class<T> responseType) {
        EnrollResponseDTO body = this.enroll(email).getBody();

        Map<String, Object> request = new HashMap<>();
        request.put("peers", this.peers);
        request.put("fcn", function);
        request.put("args", args);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + body.getToken());

        HttpEntity<?> httpEntity = new HttpEntity<Object>(request, headers);

        try {
            return template.postForEntity(this.url + "channels/mychannel/chaincodes/coin", httpEntity, responseType).getBody();
        } catch (Exception e) {
            throw new FabricRequestInvokeException(e);
        }
    }

}
