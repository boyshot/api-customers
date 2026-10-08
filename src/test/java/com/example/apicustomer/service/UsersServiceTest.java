package com.example.apicustomer.service;

import com.example.apicustomer.client.UsersApiClient;
import com.example.apicustomer.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class UsersServiceTest {

    @MockitoBean
    private UsersApiClient usersApiClient;

    @Autowired
    private UsersService usersService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private final UserResponse user = new UserResponse(
        UUID.fromString("92d4670e-bc6b-4c18-820e-7e4a1f4c5c7e"),
        "Paulo Rocha"
    );
    private final List<UserResponse> users = List.of(user);

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("users-api").reset();
    }

    @Test
    void retriesTransientFailureAndReturnsUsers() {
        when(usersApiClient.fetchUsers())
            .thenThrow(new ResourceAccessException("connection reset"))
            .thenThrow(new ResourceAccessException("connection reset"))
            .thenReturn(users);

        assertEquals(users, usersService.findAll());
        verify(usersApiClient, times(3)).fetchUsers();
        assertEquals(
            1,
            circuitBreakerRegistry.circuitBreaker("users-api").getMetrics().getNumberOfBufferedCalls()
        );
    }

    @Test
    void circuitBreakerRejectsCallsAfterFailure() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("users-api");
        circuitBreaker.transitionToOpenState();

        assertThrows(CallNotPermittedException.class, usersService::findAll);
        verify(usersApiClient, times(0)).fetchUsers();
    }
}
