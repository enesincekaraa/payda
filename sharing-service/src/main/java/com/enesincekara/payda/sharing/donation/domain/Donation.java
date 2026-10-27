package com.enesincekara.payda.sharing.donation.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false,columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "total_package_count", nullable = false)
    private int totalPackageCount;

    @Column(name = "reserved_package_count", nullable = false)
    private int reservedPackageCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version",nullable = false)
    private Long version;

    protected Donation() {
    }

    public Donation(String title, int totalPackageCount) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Donation title must not be blank"
            );
        }

        if (totalPackageCount <= 0) {
            throw new IllegalArgumentException(
                    "Total package count must be greater than zero"
            );
        }

        this.title = title;
        this.totalPackageCount = totalPackageCount;
        this.createdAt = Instant.now();
    }

    public void reserve(int packageCount) {
        if (packageCount <= 0) {
            throw new IllegalArgumentException(
                    "Reservation package count must be greater than zero"
            );
        }

        if (packageCount > getAvailablePackageCount()) {
            throw new InsufficientPackagesException(
                   packageCount,getAvailablePackageCount()
            );
        }

        reservedPackageCount += packageCount;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalPackageCount() {
        return totalPackageCount;
    }

    public int getReservedPackageCount() {
        return reservedPackageCount;
    }

    public int getAvailablePackageCount() {
        return totalPackageCount - reservedPackageCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}