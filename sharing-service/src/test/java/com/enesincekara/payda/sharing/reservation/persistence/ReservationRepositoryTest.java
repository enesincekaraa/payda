package com.enesincekara.payda.sharing.reservation.persistence;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import com.enesincekara.payda.sharing.reservation.domain.ReservationStatus;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class ReservationRepositoryTest {

    private final ReservationRepository reservationRepository;
    private final DonationRepository  donationRepository;
    private final EntityManager entityManager;

    @Autowired
    ReservationRepositoryTest(ReservationRepository reservationRepository, DonationRepository donationRepository) {
        this.reservationRepository = reservationRepository;
        this.donationRepository = donationRepository;
        this.entityManager = Mockito.mock(EntityManager.class);
    }
    @Test
    void shouldPersistAndReloadActiveReservation(){
        Donation donation = new Donation("Paketli ekmek",40);
        donation.reserve(15);

        Donation savedDonation = donationRepository.saveAndFlush(donation);

        Reservation reservation = new Reservation(
                savedDonation.getId(),
                15
        );

        UUID reservationId = reservation.getId();

        reservationRepository.saveAndFlush(reservation);
        entityManager.clear();


        Reservation found = reservationRepository.findById(reservationId).orElseThrow();

        assertEquals(reservationId, found.getId());
        assertEquals(savedDonation.getId(), found.getDonationId());
        assertEquals(15, found.getPackageCount());
        assertEquals(ReservationStatus.ACTIVE, found.getStatus());
        assertNotNull(found.getCreatedAt());


    }

    @Test
    void shouldRejectReservationForUnknownDonation(){

        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                15
        );
        assertThrows(
                DataIntegrityViolationException.class,
                () -> reservationRepository.saveAndFlush(reservation)
        );
    }

}