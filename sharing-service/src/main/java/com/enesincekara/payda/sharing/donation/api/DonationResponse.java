package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.domain.Donation;

import java.time.Instant;
import java.util.UUID;

public record DonationResponse(
        UUID id,
        String title,
        int totalPackageCount,
        int reservedPackageCount,
        int availablePackageCount,
        Instant createdAt
) {
    public static DonationResponse from(Donation donation) {
        return new DonationResponse(
                donation.getId(),
                donation.getTitle(),
                donation.getTotalPackageCount(),
                donation.getReservedPackageCount(),
                donation.getAvailablePackageCount(),
                donation.getCreatedAt()

        );
    }
}
