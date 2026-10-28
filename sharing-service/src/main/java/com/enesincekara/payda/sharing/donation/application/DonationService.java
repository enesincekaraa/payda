package com.enesincekara.payda.sharing.donation.application;


import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public Page<Donation> findAvailable(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"createdAt","id"));
        return donationRepository.findAvailable(pageRequest);
    }


}
