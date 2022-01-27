package com.softjourn.coin.server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.AutocompleteService;
import com.softjourn.common.auth.OAuthHelper;
import java.io.IOException;
import java.io.InputStream;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class CoinsConfiguration extends ResourceServerConfigurerAdapter {

  private final ApplicationProperties applicationProperties;

  @Bean
  public Module springDataPageModule() {
    return new SimpleModule().addSerializer(Page.class, new JsonSerializer<>() {

      @Override
      public void serialize(Page value, JsonGenerator gen, SerializerProvider serializers)
          throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("totalElements", value.getTotalElements());
        gen.writeBooleanField("last", value.isLast());
        gen.writeBooleanField("first", value.isFirst());
        gen.writeNumberField("totalPages", value.getTotalPages());
        gen.writeNumberField("numberOfElements", value.getNumberOfElements());
        gen.writeNumberField("size", value.getSize());
        gen.writeNumberField("number", value.getNumber());
        gen.writeObjectField("sort", value.getSort());
        gen.writeFieldName("content");
        serializers.defaultSerializeValue(value.getContent(), gen);
        gen.writeEndObject();
      }
    });
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  public OAuthHelper oAuthHelper() {
    return new OAuthHelper(
        applicationProperties.getAuth().getClient().getId(),
        applicationProperties.getAuth().getClient().getSecret(),
        applicationProperties.getAuth().getServer().getUrl(),
        new RestTemplate());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    String publicKey = readPublicKey(applicationProperties.getAuth().getPublicKeyFile());
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
    try (InputStream inputStream =
        new UrlResource("file:" + authPublicKeyFile).getInputStream()
    ) {
      return IOUtils.toString(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Can't read auth public key from file " + authPublicKeyFile);
    }
  }

  @Bean
  @Autowired
  public AutocompleteService<Transaction> autocompleteService(EntityManager entityManager) {
    return new AutocompleteService<>(Transaction.class, entityManager);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/v1/foundations/images/**").permitAll()
        .antMatchers(HttpMethod.GET, "/v1/foundations/projects").permitAll()
        .anyRequest().authenticated()
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
        .and()
        .csrf().disable();
  }
}
