package com.enesincekara.payda.sharing.donation.persistence;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

}
