package com.enesincekara.payda.sharing.donation.application;


import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {
    private final DonationRepository  donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @Transactional
    public Donation create(String title, int totalPackageCount) {
        Donation donation = new Donation(title, totalPackageCount);
        return donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public Optional<Donation> findById(UUID id) {
        return donationRepository.findById(id);
    }

    @Transactional
    public Optional<Donation> reserve(UUID id,int packageCount) {
        Optional<Donation> donation = donationRepository.findById(id);

        if (donation.isEmpty()){
            return Optional.empty();
        }
        donation.get().reserve(packageCount);
        return donation;
    }
}
