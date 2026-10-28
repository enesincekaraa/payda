package com.enesincekara.payda.sharing.donation.persistence;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    @Query("""  
        select d
             from Donation d 
                 where d.totalPackageCount > d.reservedPackageCount
    """)
    Page<Donation> findAvailable(Pageable pageable);

}
