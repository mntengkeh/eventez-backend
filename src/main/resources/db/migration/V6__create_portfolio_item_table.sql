CREATE TABLE portfolio_item (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    media_url       VARCHAR(1000) NOT NULL,
    cloudinary_public_id  VARCHAR(255) NOT NULL UNIQUE,
    media_type      media_type NOT NULL DEFAULT 'IMAGE',
    caption         VARCHAR(500),
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
