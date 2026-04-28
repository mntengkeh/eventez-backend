CREATE TABLE inquiry (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    event_id        BIGINT REFERENCES event(id) ON DELETE SET NULL,
    message         TEXT NOT NULL,
    status          inquiry_status NOT NULL DEFAULT 'PENDING',
    response        TEXT,
    responded_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
