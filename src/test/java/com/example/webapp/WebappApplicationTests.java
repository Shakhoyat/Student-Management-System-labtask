package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// WHAT: Integration test - needs full Spring context + real PostgreSQL database
// HOW: @SpringBootTest loads entire application context (unlike @ExtendWith(MockitoExtension) which only loads mocks)
// NOTE: This test is skipped in CI because it needs a running database
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class WebappApplicationTests {

	@Test
	void contextLoads() {
	}

}
