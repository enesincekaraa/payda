package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.application.DonationService;
import com.enesincekara.payda.sharing.donation.domain.Donation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donations")

@Tag(name = "Donations",description = "Bağış işlemleri")
public class DonationController {
    private final DonationService donationService;
    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }
    @Operation(summary = "Yeni bir bağış oluştur")
    @ApiResponse(
            responseCode = "201",
            description = "Bağış başarıyla oluşturuldu"
    )
    @PostMapping
    public ResponseEntity<DonationResponse> create(@Valid @RequestBody CreateDonationRequest request){
        Donation donation = donationService.create(
                request.title(),
                request.totalPackageCount()
        );

        DonationResponse response = DonationResponse.from(donation);

        URI location = URI.create(
                "/api/v1/donations/" + response.id()
        );

        return ResponseEntity.created(location).body(response);

    }

    @Operation(summary = "Kimliğiyle bir bağışı getir")
    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse> findById(@PathVariable("id") UUID id) {
        Optional<Donation> donation = donationService.findById(id);
        if (donation.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        DonationResponse response = DonationResponse.from(donation.get());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bağıştan paket ayır")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<DonationResponse> reserve(@PathVariable("id") UUID id,@Valid @RequestBody ReserveDonationRequest request) {
        Optional<Donation> donation = donationService.reserve(id, request.packageCount());

        if (donation.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(DonationResponse.from(donation.get()));
    }
}
