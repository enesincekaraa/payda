ALTER TABLE donations
    RENAME COLUMN total_page_count TO total_package_count;

ALTER TABLE donations
    DROP CONSTRAINT ck_donations_reserved_package_count_valid,
    ADD CONSTRAINT ck_donations_reserved_package_count_valid
        CHECK (
            reserved_package_count >= 0
                AND reserved_package_count <= total_package_count
            );