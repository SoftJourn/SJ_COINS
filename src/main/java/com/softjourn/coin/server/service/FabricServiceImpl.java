package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.EnrollResponseDTO;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.exceptions.AccountEnrollException;
import com.softjourn.coin.server.exceptions.FabricRequestInvokeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FabricServiceImpl implements FabricService {

  private final String url;
  private final String organization;

  private final RestTemplate template;

  @Autowired
  public FabricServiceImpl(
      @Value("${node.fabric.client}") String url, @Value("${org.name}") String organization,
      RestTemplate template
  ) {
    this.url = url;
    this.organization = organization;
    this.template = template;

    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8));
    messageConverters.add(converter);
    template.setMessageConverters(messageConverters);
  }

  @Override
  public ResponseEntity<EnrollResponseDTO> enroll(String email) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, String> request = new HashMap<>();
    request.put("username", email);
    request.put("orgName", organization);

    HttpEntity<?> httpEntity = new HttpEntity<Object>(request, headers);
    try {
      return template.postForEntity(this.url + "enroll", httpEntity, EnrollResponseDTO.class);
    } catch (Exception e) {
      throw new AccountEnrollException(e);
    }
  }

  @Override
  public <T> T invoke(String email, String function, Object args, Class<T> responseType) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    HttpEntity<?> httpEntity = getHttpEntity(function, args, body);

    try {
      return template.postForEntity(this.url + "invoke", httpEntity, responseType).getBody();
    } catch (Exception e) {
      throw new FabricRequestInvokeException(e);
    }
  }

  @Override
  public <T> T invoke(
      String email, Chaincode chaincode, String function, Object args, Class<T> responseType
  ) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    try {
      return template.postForEntity(
          this.url + "invoke/" + chaincode.getName(),
          getHttpEntity(function, args, body),
          responseType
      ).getBody();
    } catch (Exception e) {
      throw new FabricRequestInvokeException(e);
    }
  }

  @Override
  public <T> T query(String email, String function, Object args, Class<T> responseType) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    HttpEntity<?> httpEntity = getHttpEntity(function, args, body);

    try {
      return template.postForEntity(this.url + "query", httpEntity, responseType).getBody();
    } catch (Exception e) {
      throw new FabricRequestInvokeException(e);
    }
  }

  @Override
  public <T> T query(
      String email, Chaincode chaincode, String function, Object args, Class<T> responseType
  ) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    try {
      return template.postForEntity(
          this.url + "query/" + chaincode.getName(),
          getHttpEntity(function, args, body),
          responseType
      ).getBody();
    } catch (Exception e) {
      throw new FabricRequestInvokeException(e);
    }
  }

  private HttpEntity<?> getHttpEntity(String function, Object args, EnrollResponseDTO body) {
    Map<String, Object> request = new HashMap<>();
    request.put("fcn", function);
    request.put("args", args);

    // If args is a single object instead of set of parameters than isObject = true
    request.put("isObject", !(args instanceof String[]));

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("Authorization", "Bearer " + body.getToken());

    return new HttpEntity<Object>(request, headers);
  }
}
