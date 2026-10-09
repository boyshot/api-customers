package com.example.apicustomer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.Set;

@Configuration
public class ResilienceDefaultConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResilienceDefaultConfig.class);
    // Inclua aqui o nome usado nas anotações @CircuitBreaker e @Retry de cada client.
    private static final Set<String> CLIENT_NAMES = Set.of("users-api");

    @Bean
    SmartInitializingSingleton configureClientResilience(
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry
    ) {
        return () -> CLIENT_NAMES.forEach(clientName -> {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(
                clientName,
                circuitBreakerConfig()
            );
            configureCircuitBreakerLogging(circuitBreaker);

            Retry retry = retryRegistry.retry(clientName, retryConfig());
            configureRetryLogging(retry);
        });
    }

    private static CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // abre o circuito quando pelo menos 50% das chamadas contabilizadas falham.
            .slowCallRateThreshold(50) //também pode abrir o circuito quando pelo menos 50% das chamadas forem consideradas lentas.
            .slowCallDurationThreshold(Duration.ofSeconds(2)) //define o tempo limite para considerar uma chamada lenta.
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) //avalia as 10 chamadas mais recentes.
            .slidingWindowSize(10) //avalia as 10 chamadas mais recentes.
            .minimumNumberOfCalls(5) //define o número mínimo de chamadas para avaliar o estado do circuito.
            .waitDurationInOpenState(Duration.ofSeconds(30)) //define o tempo que o circuito permanece aberto.
            .permittedNumberOfCallsInHalfOpenState(3) //define o número de chamadas permitidas enquanto o circuito está em estado half-open.
            .recordException(ResilienceDefaultConfig::isTransientFailure) // contabiliza como falhas apenas erros de rede (ResourceAccessException) e respostas HTTP 5xx (HttpServerErrorException). Outros erros não são registrados como falha pelo circuit breaker.
            .build();
    }

    private static void configureCircuitBreakerLogging(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> LOGGER.warn(
                "circuit_breaker_changed [{}] to [{}]",
                circuitBreaker.getName(),
                event.getStateTransition()
            ))
            .onCallNotPermitted(event -> LOGGER.warn(
                "circuit_breaker_open name='{}'",
                circuitBreaker.getName()
            ));
    }

    private static RetryConfig retryConfig() {
        return RetryConfig.custom()
            .maxAttempts(5) //permite até 5 chamadas no total — a primeira tentativa e até 4 repetições.
            .intervalFunction(attempt -> Math.min(200L * (1L << (attempt - 1)), 1_000L)) // define espera exponencial entre tentativas: 200 ms e depois 400 ms; o cálculo tem limite máximo de 1 segundo
            .retryOnException(ResilienceDefaultConfig::isTransientFailure) // repete apenas erros de rede e respostas HTTP 5xx. Respostas HTTP 4xx não são repetidas.
            .build();
    }

    private static void configureRetryLogging(Retry retry) {
        retry.getEventPublisher().onRetry(event -> {
            Throwable cause = event.getLastThrowable();
            LOGGER.warn(
                "retry name='{}' executing_attempt={} after [{}]:[{}]",
                retry.getName(),
                event.getNumberOfRetryAttempts(),
                cause.getClass().getSimpleName(),
                cause.getMessage()
            );
        });
    }

    private static boolean isTransientFailure(Throwable throwable) {
        return throwable instanceof ResourceAccessException
            || throwable instanceof HttpServerErrorException;
    }
}
