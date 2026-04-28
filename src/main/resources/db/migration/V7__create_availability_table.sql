CREATE TABLE availability (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    status          availability_status NOT NULL DEFAULT 'AVAILABLE',
    note            VARCHAR(500),
    UNIQUE (provider_id, date)
);
