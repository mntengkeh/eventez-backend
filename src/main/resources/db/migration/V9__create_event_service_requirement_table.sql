CREATE TABLE event_service_requirement (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES service_category(id),
    note            VARCHAR(500),
    UNIQUE (event_id, category_id)
);
