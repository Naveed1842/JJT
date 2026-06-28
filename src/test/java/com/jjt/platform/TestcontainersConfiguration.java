package com.jjt.platform;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    /**
     * Apache HttpClient 4.x arrives transitively via google-http-client (Cloud SQL socket factory).
     * Spring's RestTemplateBuilder auto-selects it, causing HttpRetryException on 401 POST responses.
     * JdkClientHttpRequestFactory (Java 11 HttpClient) has no legacy auth-retry behaviour.
     */
    @Bean
    RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .requestFactory(JdkClientHttpRequestFactory.class);
    }
}
