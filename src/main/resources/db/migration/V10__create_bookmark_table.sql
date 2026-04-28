CREATE TABLE bookmark (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (planner_id, provider_id)
);
