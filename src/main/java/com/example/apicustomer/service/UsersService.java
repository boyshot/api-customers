package com.example.apicustomer.service;

import com.example.apicustomer.client.UsersApiClient;
import com.example.apicustomer.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    private final UsersApiClient usersApiClient;

    public UsersService(UsersApiClient usersApiClient) {
        this.usersApiClient = usersApiClient;
    }

    @CircuitBreaker(name = "users-api")
    @Retry(name = "users-api")
    public List<UserResponse> findAll() {
        return usersApiClient.fetchUsers();
    }
}
