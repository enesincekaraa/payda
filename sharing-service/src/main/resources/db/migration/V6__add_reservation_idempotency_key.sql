alter table reservations add column idempotency_key uuid;

update reservations set idempotency_key =gen_random_uuid()
where reservations.idempotency_key is null;

alter table reservations alter column idempotency_key set not null;

alter table reservations add constraint uq_reservations_idempotency_key unique (idempotency_key);