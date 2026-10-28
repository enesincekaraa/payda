package com.enesincekara.payda.sharing.reservation.application;

import com.enesincekara.payda.sharing.donation.application.DonationNotFoundException;
import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.domain.InsufficientPackagesException;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import com.enesincekara.payda.sharing.reservation.domain.ReservationStatus;
import com.enesincekara.payda.sharing.reservation.persistence.ReservationRepository;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class ReservationServiceTest {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final DonationRepository donationRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    ReservationServiceTest(
            ReservationService reservationService,
            ReservationRepository reservationRepository,
            DonationRepository donationRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.donationRepository = donationRepository;
        this.transactionManager = transactionManager;
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
        donationRepository.deleteAll();
    }

    @Test
    void shouldCreateReservationAndUpdateDonation() {
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        Reservation created = reservationService.create(
                donation.getId(),
                15
        );

        Reservation persistedReservation = reservationRepository
                .findById(created.getId())
                .orElseThrow();

        Donation persistedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        assertEquals(
                donation.getId(),
                persistedReservation.getDonationId()
        );
        assertEquals(15, persistedReservation.getPackageCount());
        assertEquals(
                ReservationStatus.ACTIVE,
                persistedReservation.getStatus()
        );

        assertEquals(15, persistedDonation.getReservedPackageCount());
        assertEquals(25, persistedDonation.getAvailablePackageCount());
        assertEquals(1L, reservationRepository.count());
    }

    @Test
    void shouldRejectUnknownDonationWithoutCreatingReservation() {
        UUID unknownDonationId = UUID.randomUUID();

        assertThrows(
                DonationNotFoundException.class,
                () -> reservationService.create(unknownDonationId, 5)
        );

        assertEquals(0L, reservationRepository.count());
    }

    @Test
    void shouldRejectInsufficientPackagesWithoutCreatingReservation() {
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 10)
        );

        assertThrows(
                InsufficientPackagesException.class,
                () -> reservationService.create(donation.getId(), 11)
        );

        Donation persistedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        assertEquals(0, persistedDonation.getReservedPackageCount());
        assertEquals(10, persistedDonation.getAvailablePackageCount());
        assertEquals(0L, reservationRepository.count());
    }

    @Test
    void shouldNotLeaveExtraReservationAfterVersionConflict() {
        UUID donationId = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 10)
        ).getId();

        TransactionTemplate firstTransaction =
                new TransactionTemplate(transactionManager);

        TransactionTemplate secondTransaction =
                new TransactionTemplate(transactionManager);

        secondTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> firstTransaction.executeWithoutResult(firstStatus -> {
                    // İlk transaction bağışı eski sürümüyle belleğe alır.
                    donationRepository.findById(donationId).orElseThrow();

                    // İkinci transaction rezervasyon oluşturup commit eder.
                    secondTransaction.executeWithoutResult(secondStatus -> {
                        reservationService.create(donationId, 7);
                    });

                    // İlk transaction eski nesne üzerinden işlem yapar.
                    reservationService.create(donationId, 7);
                })
        );

        Donation persistedDonation = donationRepository
                .findById(donationId)
                .orElseThrow();

        assertEquals(7, persistedDonation.getReservedPackageCount());
        assertEquals(3, persistedDonation.getAvailablePackageCount());
        assertEquals(1L, reservationRepository.count());

        Reservation persistedReservation = reservationRepository
                .findAll()
                .get(0);

        assertEquals(donationId, persistedReservation.getDonationId());
        assertEquals(7, persistedReservation.getPackageCount());
    }

    @Test
    void shouldCancelReservationAndReleasePackages(){
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        Reservation created = reservationService.create(
                donation.getId(),
                15
        );

        reservationService.cancel(created.getId());

        Reservation persistedReservation =reservationRepository.findById(created.getId()).orElseThrow();


        Donation persistedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        assertEquals(15, persistedReservation.getPackageCount());
        assertEquals(0, persistedDonation.getReservedPackageCount());
        assertEquals(40, persistedDonation.getAvailablePackageCount());
        assertEquals(1L, reservationRepository.count());


    }
    @Test
    void shouldRejectSecondCancellationWithoutReleasingAgain() {
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        Reservation created = reservationService.create(
                donation.getId(),
                15
        );

        reservationService.cancel(created.getId());

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.cancel(created.getId())
        );

        Donation persistedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        Reservation persistedReservation = reservationRepository
                .findById(created.getId())
                .orElseThrow();

        assertEquals(0, persistedDonation.getReservedPackageCount());
        assertEquals(40, persistedDonation.getAvailablePackageCount());
        assertEquals(
                ReservationStatus.CANCELLED,
                persistedReservation.getStatus()
        );
    }
    @Test
    void shouldRejectCancellingUnknownReservation() {
        assertThrows(
                ReservationNotFoundException.class,
                () -> reservationService.cancel(UUID.randomUUID())
        );

        assertEquals(0L, reservationRepository.count());
    }

    @Test
    void shouldReturnExistingReservationWhenSameRequestIsRepeated(){
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );
        UUID idempotencyKey = UUID.randomUUID();
        Reservation first = reservationService.create(donation.getId(),20,idempotencyKey);

        Reservation repeated = reservationService.create(
                donation.getId(),
                20,
                idempotencyKey);

        assertEquals(first.getId(), repeated.getId());
        assertEquals(1L, reservationRepository.count());
        Donation updatedDonation = donationRepository.findById(donation.getId()).orElseThrow();

        assertEquals(20,updatedDonation.getReservedPackageCount());
        assertEquals(20,updatedDonation.getAvailablePackageCount());
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentPackageCount(){
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );
        UUID idempotencyKey = UUID.randomUUID();
        Reservation first = reservationService.create(donation.getId(),20,idempotencyKey);

        assertThrows(
                IdempotencyKeyConflictException.class,
                ()-> reservationService.create(donation.getId(),10,idempotencyKey)
        );
        Donation updatedDonation = donationRepository.findById(donation.getId()).orElseThrow();
        assertEquals(1L, reservationRepository.count());
        assertEquals(20,updatedDonation.getAvailablePackageCount());


    }
    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentDonation() {
        Donation firstDonation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        Donation secondDonation = donationRepository.saveAndFlush(
                new Donation("Sebze paketi", 30)
        );

        UUID idempotencyKey = UUID.randomUUID();

        reservationService.create(
                firstDonation.getId(),
                20,
                idempotencyKey
        );

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> reservationService.create(
                        secondDonation.getId(),
                        20,
                        idempotencyKey
                )
        );

        assertEquals(1L, reservationRepository.count());

        Donation updatedFirstDonation = donationRepository
                .findById(firstDonation.getId())
                .orElseThrow();

        Donation updatedSecondDonation = donationRepository
                .findById(secondDonation.getId())
                .orElseThrow();

        assertEquals(20, updatedFirstDonation.getReservedPackageCount());
        assertEquals(20, updatedFirstDonation.getAvailablePackageCount());

        assertEquals(0, updatedSecondDonation.getReservedPackageCount());
        assertEquals(30, updatedSecondDonation.getAvailablePackageCount());
    }

    @Test
    void shouldCreateSeparateReservationsForDifferentIdempotencyKeys() {
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        UUID firstKey = UUID.randomUUID();
        UUID secondKey = UUID.randomUUID();

        Reservation first = reservationService.create(
                donation.getId(),
                20,
                firstKey
        );

        Reservation second = reservationService.create(
                donation.getId(),
                20,
                secondKey
        );

        assertNotEquals(first.getId(), second.getId());
        assertEquals(2L, reservationRepository.count());

        Donation updatedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        assertEquals(40, updatedDonation.getReservedPackageCount());
        assertEquals(0, updatedDonation.getAvailablePackageCount());
    }

    @Test
    void shouldNotReactiveCancelledReservationWhenCreateIsRetried(){
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        UUID idempotencyKey = UUID.randomUUID();

        Reservation created = reservationService.create(donation.getId(),20,idempotencyKey);

        reservationService.cancel(created.getId());

        Reservation retried = reservationService.create(donation.getId(),20,idempotencyKey);

        assertEquals(created.getId(), retried.getId());

        assertEquals(
                ReservationStatus.CANCELLED,
                retried.getStatus()
        );

        assertEquals(1L, reservationRepository.count());

        Donation updatedDonation = donationRepository
                .findById(donation.getId())
                .orElseThrow();

        assertEquals(0, updatedDonation.getReservedPackageCount());
        assertEquals(40, updatedDonation.getAvailablePackageCount());
    }



}