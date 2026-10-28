CREATE TABLE reservations (
                              id UUID PRIMARY KEY,
                              donation_id UUID NOT NULL,
                              package_count INTEGER NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL,
                              version BIGINT NOT NULL DEFAULT 0,

                              CONSTRAINT fk_reservations_donation
                                  FOREIGN KEY (donation_id)
                                      REFERENCES donations (id),

                              CONSTRAINT ck_reservations_package_count_positive
                                  CHECK (package_count > 0),

                              CONSTRAINT ck_reservations_status_valid
                                  CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_reservations_donation_id
    ON reservations (donation_id);