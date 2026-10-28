package com.enesincekara.payda.sharing.reservation.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    @Test
    void shouldCreateActiveReservation() {
        UUID donationId = UUID.randomUUID();

        Reservation reservation = new Reservation(donationId, 15);

        assertNotNull(reservation.getId());
        assertNotNull(reservation.getCreatedAt());
        assertEquals(donationId, reservation.getDonationId());
        assertEquals(15, reservation.getPackageCount());
        assertEquals(
                ReservationStatus.ACTIVE,
                reservation.getStatus()
        );
    }

    @Test
    void shouldRejectMissingDonationId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(null, 15)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositivePackageCount(int packageCount) {
        UUID donationId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(donationId, packageCount)
        );
    }

    @Test
    void shouldCancelActiveReservation() {
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                15
        );

        reservation.cancel();

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );
        assertEquals(15, reservation.getPackageCount());
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledReservation() {
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                15
        );

        reservation.cancel();

        assertThrows(
                IllegalStateException.class,
                reservation::cancel
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );
    }

    @Test
    void shouldKeepProvidedIdempotencyKey() {
        UUID donationId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();

        Reservation reservation = new Reservation(donationId, 15, idempotencyKey);

        assertEquals(idempotencyKey, reservation.getIdempotencyKey());
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        UUID donationId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(donationId, 15, null)
        );
    }


}