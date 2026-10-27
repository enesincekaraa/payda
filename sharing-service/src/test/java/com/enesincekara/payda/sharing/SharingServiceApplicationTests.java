package com.enesincekara.payda.sharing;

import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class SharingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
