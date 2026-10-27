package com.enesincekara.payda.sharing.donation.domain;

public class InsufficientPackagesException extends IllegalStateException {

    public InsufficientPackagesException(int requested, int available) {
        super(
                "Requested package count: " + requested
                        + ", available package count: " + available
        );
    }
}