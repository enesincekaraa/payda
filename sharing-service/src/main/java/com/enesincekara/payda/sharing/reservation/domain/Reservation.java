package com.enesincekara.payda.sharing.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "donation_id", nullable = false, updatable = false)
    private UUID donationId;

    @Column(name = "package_count", nullable = false, updatable = false)
    private int packageCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private UUID idempotencyKey;

    protected Reservation() {}



    public Reservation(
            UUID donationId,
            int packageCount,
            UUID idempotencyKey
    ){
        if (donationId == null) {
            throw new IllegalArgumentException(
                    "Donation id must not be null"
            );
        }
        if (packageCount <= 0) {
            throw new IllegalArgumentException(
                    "Reservation package count must be greater than zero"
            );
        }
        if (idempotencyKey == null) {
            throw new IllegalArgumentException(
                    "Idempotency key must not be null"
            );
        }
        this.id = UUID.randomUUID();
        this.donationId = donationId;
        this.packageCount = packageCount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.status = ReservationStatus.ACTIVE;
    }

    public Reservation(UUID donationId, int packageCount) {
        this(donationId, packageCount, UUID.randomUUID());
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException(id);
        }
        status = ReservationStatus.CANCELLED;
    }
    public UUID getId() {
        return id;
    }

    public UUID getDonationId() {
        return donationId;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}