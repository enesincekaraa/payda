package com.enesincekara.payda.sharing.reservation.api;

import com.enesincekara.payda.sharing.reservation.application.ReservationService;
import com.enesincekara.payda.sharing.reservation.domain.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Reservations", description = "Rezervasyon işlemleri")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/v1/donations/{donationId}/reservations")
    @Operation(summary = "Bağış için rezervasyon oluştur")
    @ApiResponse(
            responseCode = "201",
            description = "Rezervasyon oluşturuldu"
    )
    public ResponseEntity<ReservationResponse> create(
            @PathVariable("donationId") UUID donationId,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation reservation = reservationService.create(
                donationId,
                request.packageCount()
        );

        ReservationResponse response =
                ReservationResponse.from(reservation);

        URI location = URI.create(
                "/api/v1/reservations/" + response.id()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/api/v1/reservations/{id}")
    @Operation(summary = "Kimliğiyle rezervasyon getir")
    public ResponseEntity<ReservationResponse> findById(
            @PathVariable("id") UUID id
    ) {
        Optional<Reservation> reservation =
                reservationService.findById(id);

        if (reservation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                ReservationResponse.from(reservation.get())
        );
    }

    @PostMapping("/api/v1/reservations/{id}/cancel")
    @Operation(summary = "Rezervasyonu iptal et")
    @ApiResponse(
            responseCode = "200",
            description = "Rezervasyon iptal edildi"
    )
    public ResponseEntity<ReservationResponse> cancel(@PathVariable("id") UUID id) {
        Reservation reservation = reservationService.cancel(id);

        return ResponseEntity.ok(
                ReservationResponse.from(reservation)
        );
    }
}