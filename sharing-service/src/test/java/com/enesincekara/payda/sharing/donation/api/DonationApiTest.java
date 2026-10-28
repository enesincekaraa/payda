package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.reservation.persistence.ReservationRepository;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class DonationApiTest {

    private final MockMvc mockMvc;
    private final DonationRepository donationRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    DonationApiTest(
            MockMvc mockMvc,
            DonationRepository donationRepository,
            ReservationRepository reservationRepository
    ) {
        this.mockMvc = mockMvc;
        this.donationRepository = donationRepository;
        this.reservationRepository = reservationRepository;
    }

    @BeforeEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
        donationRepository.deleteAll();
    }

    @Test
    void shouldCreateDonationAndReadItFromLocation() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/v1/donations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Paketli ekmek",
                                          "totalPackageCount": 40
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Paketli ekmek"))
                .andExpect(jsonPath("$.totalPackageCount").value(40))
                .andExpect(jsonPath("$.reservedPackageCount").value(0))
                .andExpect(jsonPath("$.availablePackageCount").value(40))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);

        String id = location.substring(location.lastIndexOf('/') + 1);
        UUID donationId = UUID.fromString(id);

        assertTrue(donationRepository.existsById(donationId));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(donationId.toString()))
                .andExpect(jsonPath("$.title").value("Paketli ekmek"))
                .andExpect(jsonPath("$.totalPackageCount").value(40))
                .andExpect(jsonPath("$.reservedPackageCount").value(0))
                .andExpect(jsonPath("$.availablePackageCount").value(40));
    }

    @Test
    void shouldRejectBlankTitleWithoutCreatingDonation() throws Exception {
        mockMvc.perform(
                        post("/api/v1/donations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "   ",
                                          "totalPackageCount": 40
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        assertEquals(0L, donationRepository.count());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositivePackageCountWithoutCreatingDonation(
            int packageCount
    ) throws Exception {
        String requestBody = """
                {
                  "title": "Paketli ekmek",
                  "totalPackageCount": %d
                }
                """.formatted(packageCount);

        mockMvc.perform(
                        post("/api/v1/donations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        assertEquals(0L, donationRepository.count());
    }

    @Test
    void shouldReturnNotFoundForUnknownDonation() throws Exception {
        mockMvc.perform(
                        get("/api/v1/donations/{id}", UUID.randomUUID())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateReservationAndReadItFromLocation() throws Exception {
        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        MvcResult result = mockMvc.perform(
                        post(
                                "/api/v1/donations/{id}/reservations",
                                donation.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "packageCount": 15
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.donationId")
                        .value(donation.getId().toString()))
                .andExpect(jsonPath("$.packageCount").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);

        UUID reservationId = UUID.fromString(
                location.substring(location.lastIndexOf('/') + 1)
        );

        assertTrue(reservationRepository.existsById(reservationId));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.donationId")
                        .value(donation.getId().toString()))
                .andExpect(jsonPath("$.packageCount").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(
                        get("/api/v1/donations/{id}", donation.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedPackageCount").value(15))
                .andExpect(jsonPath("$.availablePackageCount").value(25));
    }

    @Test
    void shouldRejectReservationWhenPackagesAreInsufficient() throws Exception {
        Donation donation = new Donation("Paketli ekmek", 40);
        donation.reserve(15);

        Donation savedDonation = donationRepository.saveAndFlush(donation);

        mockMvc.perform(
                        post(
                                "/api/v1/donations/{id}/reservations",
                                savedDonation.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "packageCount": 26
                                    }
                                    """)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_PACKAGES"));

        mockMvc.perform(
                        get("/api/v1/donations/{id}", savedDonation.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPackageCount").value(40))
                .andExpect(jsonPath("$.reservedPackageCount").value(15))
                .andExpect(jsonPath("$.availablePackageCount").value(25));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositiveReservationCount(int packageCount)
            throws Exception {
        Donation donation =donationRepository.saveAndFlush(new Donation("Paketli ekmek",40));
        String requestBody = """
                {
                    "packageCount": %d
                }
        """.formatted(packageCount);

        mockMvc.perform(
                post("/api/v1/donations/{id}/reservations", donation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                        get("/api/v1/donations/{id}", donation.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedPackageCount").value(0))
                .andExpect(jsonPath("$.availablePackageCount").value(40));
    }
    @Test
    void shouldReturnNotFoundWhenReservingUnknownDonation() throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/donations/{id}/reservations",
                                UUID.randomUUID()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "packageCount": 15
                                    }
                                    """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownReservation() throws Exception {
        mockMvc.perform(
                        get("/api/v1/reservations/{id}", UUID.randomUUID())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidReservationId() throws Exception {
        mockMvc.perform(
                        get("/api/v1/reservations/{id}", "invalid-id")
                )
                .andExpect(status().isBadRequest());
    }

    private UUID createReservation(UUID donationId, int packageCount)
            throws Exception {

        MvcResult result = mockMvc.perform(
                        post(
                                "/api/v1/donations/{id}/reservations",
                                donationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "packageCount": %d
                                    }
                                    """.formatted(packageCount))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);

        return UUID.fromString(
                location.substring(location.lastIndexOf('/') + 1)
        );
    }
    @Test
    void shouldReturnConflictWhenCancellingAlreadyCancelledReservation()
            throws Exception {

        Donation donation = donationRepository.saveAndFlush(
                new Donation("Paketli ekmek", 40)
        );

        UUID reservationId = createReservation(donation.getId(), 15);

        mockMvc.perform(
                        post(
                                "/api/v1/reservations/{id}/cancel",
                                reservationId
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post(
                                "/api/v1/reservations/{id}/cancel",
                                reservationId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("RESERVATION_ALREADY_CANCELLED"));

        mockMvc.perform(
                        get("/api/v1/donations/{id}", donation.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedPackageCount").value(0))
                .andExpect(jsonPath("$.availablePackageCount").value(40));
    }

    @Test
    void shouldReturnNotFoundWhenCancellingUnknownReservation()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/reservations/{id}/cancel",
                                UUID.randomUUID()
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidReservationIdWhenCancelling()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/reservations/{id}/cancel",
                                "invalid-id"
                        )
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldListOnlyDonationsWithAvailablePackages() throws Exception {
        Donation availableDonation = new Donation("Paketli ekmek", 40);
        availableDonation.reserve(15);

        Donation fullyReservedDonation = new Donation("Meyve paketi", 10);
        fullyReservedDonation.reserve(10);

        donationRepository.saveAllAndFlush(List.of(availableDonation, fullyReservedDonation));

        mockMvc.perform(get("/api/v1/donations/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].availablePackageCount")
                        .value(25))
                .andExpect(jsonPath("$.totalElements").value(1));

    }
    @Test
    void shouldReturnEmptyPageWhenNoDonationsAreAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/donations/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldReturnRequestedPageOfAvailableDonations() throws Exception {
        donationRepository.saveAllAndFlush(
                List.of(
                        new Donation("Ekmek paketi", 10),
                        new Donation("Meyve paketi", 20),
                        new Donation("Sebze paketi", 30)
                )
        );

        mockMvc.perform(get("/api/v1/donations/available")
                .param("page", "1")
                .param("size", "2")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, 51"
    })
    void shouldRejectInvalidPagination(int page, int size)
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/donations/available")
                                .param("page", String.valueOf(page))
                                .param("size", String.valueOf(size))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOneDonationOnThirdPage()  throws Exception {

        Donation oneAvailableDonation = new Donation("Ekmek", 10);
        oneAvailableDonation.reserve(9);

        for (int i = 1; i <= 7; i++) {
            Donation donation = new Donation("Bağış" + i, 40);
            donationRepository.saveAndFlush(donation);
        }
        donationRepository.saveAndFlush(oneAvailableDonation);


        mockMvc.perform(
                get("/api/v1/donations/available").param("page", "2").param("size", "3")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(3));


    }
}