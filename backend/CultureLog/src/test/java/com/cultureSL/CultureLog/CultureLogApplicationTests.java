package com.cultureSL.CultureLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
class CultureLogApplicationTests {

	@Test
	void contextLoads() {
	}

}
