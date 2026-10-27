package com.enesincekara.payda.sharing.donation.persistence;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class DonationConcurrencyTest {

    private final DonationRepository donationRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    DonationConcurrencyTest(
            DonationRepository donationRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.donationRepository = donationRepository;
        this.transactionManager = transactionManager;
    }

    @Test
    void shouldRejectUpdateBasedOnStaleVersion() {
        UUID donationId = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 10)
        ).getId();

        try {
            TransactionTemplate firstTransaction =
                    new TransactionTemplate(transactionManager);

            TransactionTemplate secondTransaction =
                    new TransactionTemplate(transactionManager);

            secondTransaction.setPropagationBehavior(
                    TransactionDefinition.PROPAGATION_REQUIRES_NEW
            );

            assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> firstTransaction.executeWithoutResult(firstStatus -> {
                        Donation firstCopy = donationRepository
                                .findById(donationId)
                                .orElseThrow();

                        secondTransaction.executeWithoutResult(secondStatus -> {
                            Donation secondCopy = donationRepository
                                    .findById(donationId)
                                    .orElseThrow();

                            secondCopy.reserve(7);
                        });

                        firstCopy.reserve(7);
                    })
            );

            Donation persistedDonation = donationRepository
                    .findById(donationId)
                    .orElseThrow();

            assertEquals(10, persistedDonation.getTotalPackageCount());
            assertEquals(7, persistedDonation.getReservedPackageCount());
            assertEquals(3, persistedDonation.getAvailablePackageCount());
        } finally {
            donationRepository.deleteById(donationId);
        }
    }
}