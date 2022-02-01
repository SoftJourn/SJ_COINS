package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.softjourn.coin.server.config.ApplicationProperties;
import com.softjourn.coin.server.dto.EnrollResponseDTO;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.ChaincodeFunction;
import com.softjourn.coin.server.exceptions.AccountEnrollException;
import com.softjourn.coin.server.exceptions.FabricRequestInvokeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class FabricServiceImpl implements FabricService {

  private final String url;
  private final RestTemplate template;
  private final ApplicationProperties applicationProperties;

  @Autowired
  public FabricServiceImpl(
      RestTemplate template,
      ApplicationProperties applicationProperties,
      Jackson2ObjectMapperBuilder builder
  ) {
    this.template = template;
    this.applicationProperties = applicationProperties;
    this.url = applicationProperties.getFabric().getClientUrl();

    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    builder.postConfigurer(objectMapper ->
        objectMapper.coercionConfigFor(LogicalType.Collection)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty));
    MappingJackson2HttpMessageConverter converter =
        new MappingJackson2HttpMessageConverter(builder.build());
    converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
    messageConverters.add(converter);
    template.setMessageConverters(messageConverters);
  }

  @Override
  public ResponseEntity<EnrollResponseDTO> enroll(String email) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, String> request = new HashMap<>();
    request.put("username", email);
    request.put("orgName", applicationProperties.getOrganisation().getName());

    HttpEntity<?> httpEntity = new HttpEntity<>(request, headers);
    try {
      return template.postForEntity(this.url + "enroll", httpEntity, EnrollResponseDTO.class);
    } catch (RestClientException e) {
      throw new AccountEnrollException(e);
    }
  }

  @Override
  public <T> T invoke(
      String email, Chaincode chaincode, ChaincodeFunction function,
      Object args, Class<T> responseType
  ) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    try {
      return template.postForEntity(
          this.url + "invoke/" + chaincode.getName(),
          getHttpEntity(function.getName(), args, body),
          responseType
      ).getBody();
    } catch (Exception e) {
      throw new FabricRequestInvokeException(e);
    }
  }

  @Override
  public <T> T query(
      String email, Chaincode chaincode, ChaincodeFunction function,
      Object args, Class<T> responseType
  ) {
    EnrollResponseDTO body = this.enroll(email).getBody();

    try {
      return template.postForEntity(
          this.url + "query/" + chaincode.getName(),
          getHttpEntity(function.getName(), args, body),
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
    headers.add("Authorization", "Bearer " + (body != null ? body.getToken(): ""));

    return new HttpEntity<>(request, headers);
  }
}
