package com.example.apicustomer.client;

import com.example.apicustomer.dto.UserResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
public class UsersApiClient {

    private static final ParameterizedTypeReference<List<UserResponse>> USERS_TYPE =
        new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public UsersApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<UserResponse> fetchUsers() {
        List<UserResponse> users = restClient.get()
            .uri("/users")
            .retrieve()
            .body(USERS_TYPE);

        return List.copyOf(Objects.requireNonNull(users, "Users API returned an empty response body"));
    }
}
