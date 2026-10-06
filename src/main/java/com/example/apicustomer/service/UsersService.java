package com.example.apicustomer.service;

import com.example.apicustomer.client.UsersApiClient;
import com.example.apicustomer.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class UsersService {

    private final UsersApiClient usersApiClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public UsersService(UsersApiClient usersApiClient, CircuitBreaker circuitBreaker, Retry retry) {
        this.usersApiClient = usersApiClient;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    public List<UserResponse> findAll() {
        Supplier<List<UserResponse>> apiCall = usersApiClient::fetchUsers;
        Supplier<List<UserResponse>> retryingCall = Retry.decorateSupplier(retry, apiCall);
        return CircuitBreaker.decorateSupplier(circuitBreaker, retryingCall).get();
    }
}
