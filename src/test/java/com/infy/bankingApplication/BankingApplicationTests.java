package com.infy.bankingApplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
	    "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
	})
@Import(KafkaTestConfiguration.class)
class BankingApplicationTests {

	@Test
	void contextLoads() {
	}

}
