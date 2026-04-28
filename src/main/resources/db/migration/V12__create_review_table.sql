CREATE TABLE review (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    event_id        BIGINT REFERENCES event(id) ON DELETE SET NULL,
    rating          SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title           VARCHAR(255),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (planner_id, provider_id)
);
