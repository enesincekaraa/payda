package com.enesincekara.payda.sharing.donation.api;

import com.enesincekara.payda.sharing.donation.application.DonationService;
import com.enesincekara.payda.sharing.donation.domain.Donation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @GetMapping("/available")
    @Operation(summary = "Kullanılabilir bağışları sayfalayarak listele")
    public DonationPageResponse findAvailable(
            @RequestParam(name = "page",defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size",defaultValue = "10") @Min(1) @Max(50) int size) {
        return DonationPageResponse.from(donationService.findAvailable(page, size));
    }

}
