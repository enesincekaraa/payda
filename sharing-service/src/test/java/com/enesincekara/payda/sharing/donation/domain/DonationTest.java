package com.enesincekara.payda.sharing.donation.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    void shouldReleaseSomeReservedPackages(){
        Donation donation = new Donation("Paketli ekmek",40);
        donation.reserve(15);
        donation.release(5);
        assertEquals(40, donation.getTotalPackageCount());
        assertEquals(10, donation.getReservedPackageCount());
        assertEquals(30, donation.getAvailablePackageCount());

    }
    @Test
    void shouldReleaseAllReservedPackages(){
        Donation donation = new Donation("Paketli ekmek",40);

        donation.reserve(15);
        donation.release(15);
        assertEquals(40, donation.getTotalPackageCount());
        assertEquals(0, donation.getReservedPackageCount());
        assertEquals(40, donation.getAvailablePackageCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositiveReleaseCount(int packageCount){
        Donation donation = new Donation("Paketli ekmek", 40);
        donation.reserve(15);
        assertThrows(IllegalArgumentException.class, () -> donation.reserve(packageCount));
        assertEquals(15, donation.getReservedPackageCount());
        assertEquals(25, donation.getAvailablePackageCount());
    }
    @Test
    void shouldRejectReleasingMoreThanReservedPackages() {
        Donation donation = new Donation("Paketli ekmek", 40);
        donation.reserve(15);

        assertThrows(
                IllegalStateException.class,
                () -> donation.release(16)
        );

        assertEquals(40, donation.getTotalPackageCount());
        assertEquals(15, donation.getReservedPackageCount());
        assertEquals(25, donation.getAvailablePackageCount());
    }

}