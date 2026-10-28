package com.enesincekara.payda.sharing.reservation.api;

import com.enesincekara.payda.sharing.reservation.application.IdempotencyKeyConflictException;
import com.enesincekara.payda.sharing.reservation.application.ReservationNotFoundException;
import com.enesincekara.payda.sharing.reservation.domain.ReservationAlreadyCancelledException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(ReservationNotFoundException.class)
    public ProblemDetail handleReservationNotFound(
            ReservationNotFoundException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problem.setTitle("Reservation not found");
        problem.setProperty("code", "RESERVATION_NOT_FOUND");

        return problem;
    }

    @ExceptionHandler(ReservationAlreadyCancelledException.class)
    public ProblemDetail handleAlreadyCancelled(
            ReservationAlreadyCancelledException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Reservation already cancelled");
        problem.setProperty("code", "RESERVATION_ALREADY_CANCELLED");

        return problem;
    }

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ProblemDetail handleIdempotencyKeyConflict(
            IdempotencyKeyConflictException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Idempotency key conflict");
        problem.setProperty("code", "IDEMPOTENCY_KEY_CONFLICT");

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        if (!isIdempotencyKeyViolation(exception)) {
            throw exception;
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Another request used this idempotency key. "
                        + "Retry with the same key and the same request body."
        );

        problem.setTitle("Concurrent reservation request");
        problem.setProperty(
                "code",
                "IDEMPOTENCY_CONCURRENT_REQUEST"
        );

        return problem;
    }

    private boolean isIdempotencyKeyViolation(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation) {
                return "uq_reservations_idempotency_key"
                        .equals(violation.getConstraintName());
            }

            current = current.getCause();
        }

        return false;
    }
}