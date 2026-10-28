package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record DonationPageResponse(
        List<DonationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static DonationPageResponse from(Page<Donation> donations) {

        List<DonationResponse> contents = new ArrayList<>();
        for (Donation donation:donations) {
            DonationResponse response = DonationResponse.from(donation);
            contents.add(response);
        }
        return new DonationPageResponse(
                contents,
                donations.getNumber(),
                donations.getSize(),
                donations.getTotalElements(),
                donations.getTotalPages()
        );
    }
}
