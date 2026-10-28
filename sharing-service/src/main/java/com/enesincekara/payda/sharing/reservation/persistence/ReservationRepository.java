package com.enesincekara.payda.sharing.reservation.persistence;

import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository
        extends JpaRepository<Reservation, UUID> {
}