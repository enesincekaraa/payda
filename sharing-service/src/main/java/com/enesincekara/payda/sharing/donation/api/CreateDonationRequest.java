package com.enesincekara.payda.sharing.donation.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateDonationRequest(
        @NotBlank String title,
        @Positive int totalPackageCount
) {
}
