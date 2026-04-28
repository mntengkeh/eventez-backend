CREATE TABLE service (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES service_category(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price_min       NUMERIC(12, 2),
    price_max       NUMERIC(12, 2),
    price_type      price_type NOT NULL DEFAULT 'FIXED',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
