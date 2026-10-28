package com.enesincekara.payda.sharing.reservation.api;

import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import com.enesincekara.payda.sharing.reservation.domain.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID donationId,
        int packageCount,
        ReservationStatus status,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDonationId(),
                reservation.getPackageCount(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
