package com.enesincekara.payda.sharing.reservation.api;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ReservationExceptionHandlerTest {

    private final ReservationExceptionHandler handler =
            new ReservationExceptionHandler();

    @Test
    void shouldReturnConflictForDuplicateIdempotencyKey() {
        ConstraintViolationException databaseCause =
                new ConstraintViolationException(
                        "Duplicate idempotency key",
                        new SQLException("Duplicate key", "23505"),
                        "uq_reservations_idempotency_key"
                );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Could not save reservation",
                        databaseCause
                );

        ProblemDetail response =
                handler.handleDataIntegrityViolation(exception);

        assertEquals(409, response.getStatus());

        assertNotNull(response.getProperties());

        assertEquals(
                "IDEMPOTENCY_CONCURRENT_REQUEST",
                response.getProperties().get("code")
        );
    }

    @Test
    void shouldRethrowUnrelatedDatabaseConstraintViolation() {
        ConstraintViolationException databaseCause =
                new ConstraintViolationException(
                        "Donation does not exist",
                        new SQLException("Foreign key violation", "23503"),
                        "fk_reservations_donation"
                );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Could not save reservation",
                        databaseCause
                );

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> handler.handleDataIntegrityViolation(exception)
        );

        assertSame(exception, thrown);
    }
}