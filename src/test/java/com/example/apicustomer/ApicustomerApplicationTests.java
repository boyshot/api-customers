package com.example.apicustomer;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ApicustomerApplicationTests {

	@Autowired
	private MeterRegistry meterRegistry;

	@Test
	void contextLoads() {
	}

	@Test
	void resilience4jCircuitBreakerMetricsAreRegistered() {
		assertNotNull(meterRegistry.find("resilience4j.circuitbreaker.state")
			.tag("name", "users-api")
			.gauge());
	}

}
