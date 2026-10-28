package com.enesincekara.payda.sharing.reservation.api;

import jakarta.validation.constraints.Positive;

public record CreateReservationRequest(
        @Positive int packageCount
) {
}
