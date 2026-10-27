package com.enesincekara.payda.sharing.donation.api;

import jakarta.validation.constraints.Positive;

public record ReserveDonationRequest(
        @Positive int packageCount
) {
}
