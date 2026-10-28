package com.enesincekara.payda.sharing.reservation.domain;

import java.util.UUID;

public class ReservationAlreadyCancelledException
        extends IllegalStateException {

    public ReservationAlreadyCancelledException(UUID reservationId) {
        super("Reservation is already cancelled: " + reservationId);
    }
}