package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.domain.Donation;
import com.enesincekara.payda.sharing.donation.persistence.DonationRepository;
import com.enesincekara.payda.sharing.support.PostgresTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

    @Autowired
    DonationApiTest(
            MockMvc mockMvc,
            DonationRepository donationRepository
    ) {
        this.mockMvc = mockMvc;
        this.donationRepository = donationRepository;
    }

    @BeforeEach
    void cleanDatabase() {
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
    void shouldReservePackagesAndPersistUpdatedCounts() throws Exception {
        Donation donation = donationRepository.saveAndFlush(new Donation(
                "Paketli ekmek",40
        ));

        mockMvc.perform(
                post("/api/v1/donations/{id}/reserve", donation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "packageCount": 15
                                    }
                                """)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.totalPackageCount").value(40))
                .andExpect(jsonPath("$.reservedPackageCount").value(15))
                .andExpect(jsonPath("$.availablePackageCount").value(25));

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
                                "/api/v1/donations/{id}/reserve",
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
                post("/api/v1/donations/{id}/reserve", donation.getId())
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
                                "/api/v1/donations/{id}/reserve",
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
}