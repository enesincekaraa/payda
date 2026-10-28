package com.enesincekara.payda.sharing.reservation.api;

import com.enesincekara.payda.sharing.reservation.application.ReservationNotFoundException;
import com.enesincekara.payda.sharing.reservation.domain.ReservationAlreadyCancelledException;
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
}