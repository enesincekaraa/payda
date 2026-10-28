package com.enesincekara.payda.sharing.reservation.application;

import com.enesincekara.payda.sharing.donation.application.DonationNotFoundException;
import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import com.enesincekara.payda.sharing.reservation.persistence.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {
    private final DonationRepository donationRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(DonationRepository donationRepository, ReservationRepository reservationRepository) {
        this.donationRepository = donationRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation create(UUID donationId, int packageCount) {
        Donation donation = donationRepository.findById(donationId).orElseThrow(
                ()-> new DonationNotFoundException(donationId)
        );

        donation.reserve(packageCount);

        Reservation reservation = new Reservation(
                donationId,
                packageCount
        );

       return reservationRepository.saveAndFlush(reservation);
    }
    @Transactional(readOnly = true)
    public Optional<Reservation>  findById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public Reservation cancel(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                ()-> new ReservationNotFoundException(reservationId)
        );

        Donation donation = donationRepository.findById(reservation.getDonationId()).orElseThrow(
                ()-> new DonationNotFoundException(reservation.getDonationId())
        );

        reservation.cancel();
        donation.release(reservation.getPackageCount());
        return reservationRepository.saveAndFlush(reservation);

    }
}
