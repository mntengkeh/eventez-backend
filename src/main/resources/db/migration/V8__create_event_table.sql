CREATE TABLE event (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    event_type      event_type NOT NULL,
    event_date      DATE NOT NULL,
    location        VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    budget_min      NUMERIC(12, 2),
    budget_max      NUMERIC(12, 2),
    guest_count     INTEGER,
    description     TEXT,
    status          event_status NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
