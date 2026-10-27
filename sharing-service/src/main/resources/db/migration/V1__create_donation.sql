create table donation(
    id uuid primary key ,
    title text not null ,
    total_page_count INTEGER not null ,
    reserved_package_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    constraint ck_donations_title_not_blank check ( title ~ '[^[:space:]]' ),
    constraint ck_donations_total_package_count_positive check ( total_page_count > 0 ),
    constraint ck_donations_reserved_package_count_valid check ( reserved_package_count >0 and reserved_package_count <= total_page_count )
);