package com.enesincekara.payda.sharing.donation.application;

import java.util.UUID;

public class DonationNotFoundException extends RuntimeException {
    public DonationNotFoundException(UUID donationId) {
        super("Donation not found: " + donationId);
    }
}
