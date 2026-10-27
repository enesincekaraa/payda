package com.enesincekara.payda.sharing.donation.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DonationTest {

    @Test
    void shouldCreateDonationWithValidInformation() {
        Donation donation = new Donation("Paketli ekmek",40);

        assertEquals("Paketli ekmek", donation.getTitle());

        assertEquals(40, donation.getTotalPackageCount());
    }

    @Test
    void shouldRejectZeroPackageCount() {
        assertThrows(
                IllegalArgumentException.class, () -> new Donation("Paketli ekmek",0)
        );
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Donation("   ", 40)
        );
    }

    @Test
    void shouldReduceAvailablePackagesWhenReserved(){
        Donation donation = new Donation("Paketli ekmek",40);

        donation.reserve(15);
        assertEquals(25, donation.getAvailablePackageCount());
        assertEquals(40, donation.getTotalPackageCount());
        assertEquals(15, donation.getReservedPackageCount());
    }

    @Test
    void shouldKeepAvailablePackagesWhenReservationIsRejected(){
        Donation donation =  new Donation("Paketli ekmek",40);
        donation.reserve(15);
        assertThrows(
                IllegalStateException.class,
                () -> donation.reserve(26)
        );

        assertEquals(25, donation.getAvailablePackageCount());
        assertEquals(15, donation.getReservedPackageCount());
    }


}