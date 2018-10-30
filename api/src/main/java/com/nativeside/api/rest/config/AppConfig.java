package com.nativeside.api.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties nativeSideDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.datasource.hikari")
  public HikariDataSource nativeSideDataSource(
      @Qualifier("nativeSideDataSourceProperties") DataSourceProperties nativeSideDataSourceProperties) {
    return nativeSideDataSourceProperties
        .initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource nativeSideDataSource) {
    return new JdbcTemplate(nativeSideDataSource);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public RestTemplate restTemplate(List<HttpMessageConverter<?>> messageConverters) {
    return new RestTemplate(messageConverters);
  }

  @Bean
  public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
    return new ByteArrayHttpMessageConverter();
  }
}
