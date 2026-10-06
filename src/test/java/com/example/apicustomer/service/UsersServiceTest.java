package com.example.apicustomer.service;

import com.example.apicustomer.client.UsersApiClient;
import com.example.apicustomer.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersServiceTest {

    private final UsersApiClient usersApiClient = mock(UsersApiClient.class);
    private final UserResponse user = new UserResponse(
        UUID.fromString("92d4670e-bc6b-4c18-820e-7e4a1f4c5c7e"),
        "Paulo Rocha"
    );
    private final List<UserResponse> users = List.of(user);

    @Test
    void retriesTransientFailureAndReturnsUsers() {
        when(usersApiClient.fetchUsers())
            .thenThrow(new ResourceAccessException("connection reset"))
            .thenThrow(new ResourceAccessException("connection reset"))
            .thenReturn(users);

        UsersService service = new UsersService(
            usersApiClient,
            CircuitBreaker.ofDefaults("users-test"),
            retry(3)
        );

        assertEquals(users, service.findAll());
        verify(usersApiClient, times(3)).fetchUsers();
    }

    @Test
    void circuitBreakerRejectsCallsAfterFailure() {
        when(usersApiClient.fetchUsers()).thenThrow(new ResourceAccessException("connection refused"));
        CircuitBreaker circuitBreaker = CircuitBreaker.of(
            "users-test",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(1)
                .minimumNumberOfCalls(1)
                .failureRateThreshold(100)
                .build()
        );
        UsersService service = new UsersService(usersApiClient, circuitBreaker, retry(1));

        assertThrows(ResourceAccessException.class, service::findAll);
        assertThrows(CallNotPermittedException.class, service::findAll);
        verify(usersApiClient, times(1)).fetchUsers();
    }

    private Retry retry(int maxAttempts) {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(maxAttempts)
            .intervalFunction(attempt -> 0L)
            .retryOnException(exception -> exception instanceof ResourceAccessException)
            .build();
        return Retry.of("users-test", config);
    }
}
