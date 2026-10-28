package com.enesincekara.payda.sharing.donation.api;


import com.enesincekara.payda.sharing.donation.application.DonationNotFoundException;
import com.enesincekara.payda.sharing.donation.domain.InsufficientPackagesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DonationExceptionHandler {

    @ExceptionHandler(InsufficientPackagesException.class)
    public ProblemDetail handleInsufficientPackagesException(InsufficientPackagesException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Insufficient available packages");
        problem.setProperty("code", "INSUFFICIENT_PACKAGES");
        return problem;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleObjectOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Donation was updated by another request. "
                        + "Read the current state before trying again."
        );
        problem.setTitle("Donation was modified");
        problem.setProperty("code", "DONATION_CONCURRENT_MODIFICATION");
        return problem;
    }
    @ExceptionHandler(DonationNotFoundException.class)
    public ProblemDetail handleDonationNotFoundException(DonationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Donation Not Found");
        problem.setProperty("code", "DONATION_NOT_FOUND");
        return problem;
    }

}
