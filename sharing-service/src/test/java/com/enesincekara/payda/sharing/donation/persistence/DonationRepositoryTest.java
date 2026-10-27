package com.enesincekara.payda.sharing.donation.persistence;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class DonationRepositoryTest {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadDonation(){
        Donation donation = new Donation("Paketli ekmek",40);
        donation.reserve(15);

        Donation saved = donationRepository.saveAndFlush(donation);

        UUID id = saved.getId();
        assertNotNull(id);

        entityManager.clear();

        Donation reloaded = donationRepository.findById(id)
                .orElseThrow();

        assertEquals("Paketli ekmek", reloaded.getTitle());
        assertEquals(40, reloaded.getTotalPackageCount());
        assertEquals(15, reloaded.getReservedPackageCount());
        assertEquals(25, reloaded.getAvailablePackageCount());
        assertNotNull(reloaded.getCreatedAt());
    }
    @Test
    void shouldPersistDonationWithoutReservation() {
        Donation donation = new Donation("Paketli ekmek", 40);

        Donation saved = donationRepository.saveAndFlush(donation);

        assertNotNull(saved.getId());
        assertEquals(0, saved.getReservedPackageCount());
        assertEquals(40, saved.getAvailablePackageCount());
    }


}