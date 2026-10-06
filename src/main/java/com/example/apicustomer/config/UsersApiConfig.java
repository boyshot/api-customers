package com.example.apicustomer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class UsersApiConfig {

    @Bean
    JdkClientHttpRequestFactory usersRequestFactory(
        @Value("${users.api.connect-timeout:2s}") Duration connectTimeout,
        @Value("${users.api.read-timeout:3s}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }

    @Bean
    RestClient usersRestClient(
        @Qualifier("usersRequestFactory") JdkClientHttpRequestFactory requestFactory,
        @Value("${users.api.base-url:http://localhost:5000}") String baseUrl
    ) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }
}
