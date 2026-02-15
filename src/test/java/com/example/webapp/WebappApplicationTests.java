package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// WHAT: Integration test — loads full Spring context to verify app starts correctly
// HOW: @ActiveProfiles("test") uses application-test.yml with H2 in-memory DB (no real PostgreSQL needed)
@SpringBootTest
@ActiveProfiles("test")
class WebappApplicationTests {

	@Test
	void contextLoads() {
	}

}
