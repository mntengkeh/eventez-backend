-- ============================================================
-- ENUMS (PostgreSQL native enums)
-- ============================================================

CREATE TYPE user_role AS ENUM ('PLANNER', 'PROVIDER');
CREATE TYPE price_type AS ENUM ('FIXED', 'HOURLY', 'PER_EVENT', 'CUSTOM');
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO');
CREATE TYPE availability_status AS ENUM ('AVAILABLE', 'BOOKED', 'TENTATIVE', 'BLOCKED');
CREATE TYPE event_type AS ENUM ('WEDDING', 'CORPORATE', 'BIRTHDAY', 'CONFERENCE', 'PARTY', 'SOCIAL', 'FUNDRAISER', 'OTHER');
CREATE TYPE event_status AS ENUM ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED');
CREATE TYPE inquiry_status AS ENUM ('PENDING', 'READ', 'RESPONDED', 'CLOSED');

-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE app_user (
                          id              BIGSERIAL PRIMARY KEY,
                          email           VARCHAR(255) NOT NULL UNIQUE,
                          password_hash   VARCHAR(255) NOT NULL,
                          role            user_role NOT NULL,
                          full_name       VARCHAR(255) NOT NULL,
                          phone           VARCHAR(20),
                          enabled         BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE service_category (
                                  id              BIGSERIAL PRIMARY KEY,
                                  name            VARCHAR(100) NOT NULL UNIQUE,
                                  slug            VARCHAR(100) NOT NULL UNIQUE,
                                  icon            VARCHAR(100),
                                  description     TEXT
);

CREATE TABLE provider_profile (
                                  id              BIGSERIAL PRIMARY KEY,
                                  user_id         BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
                                  business_name   VARCHAR(255) NOT NULL,
                                  description     TEXT,
                                  address         VARCHAR(500),
                                  city            VARCHAR(100),
                                  state           VARCHAR(100),
                                  zip_code        VARCHAR(20),
                                  latitude        DOUBLE PRECISION,
                                  longitude       DOUBLE PRECISION,
                                  service_radius  INTEGER DEFAULT 50,           -- in kilometers
                                  website         VARCHAR(500),
                                  verified        BOOLEAN NOT NULL DEFAULT FALSE,
                                  avg_rating      DOUBLE PRECISION DEFAULT 0.0,
                                  review_count    INTEGER DEFAULT 0,
                                  response_rate   DOUBLE PRECISION DEFAULT 0.0, -- percentage 0-100
                                  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

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

CREATE TABLE portfolio_item (
                                id              BIGSERIAL PRIMARY KEY,
                                provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
                                media_url       VARCHAR(1000) NOT NULL,
                                media_type      media_type NOT NULL DEFAULT 'IMAGE',
                                caption         VARCHAR(500),
                                display_order   INTEGER NOT NULL DEFAULT 0,
                                created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE availability (
                              id              BIGSERIAL PRIMARY KEY,
                              provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
                              date            DATE NOT NULL,
                              status          availability_status NOT NULL DEFAULT 'AVAILABLE',
                              note            VARCHAR(500),

                              UNIQUE (provider_id, date)
);

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

CREATE TABLE event_service_requirement (
                                           id              BIGSERIAL PRIMARY KEY,
                                           event_id        BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
                                           category_id     BIGINT NOT NULL REFERENCES service_category(id),
                                           note            VARCHAR(500),

                                           UNIQUE (event_id, category_id)
);

CREATE TABLE bookmark (
                          id              BIGSERIAL PRIMARY KEY,
                          planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                          provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
                          created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

                          UNIQUE (planner_id, provider_id)
);

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

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_provider_profile_city ON provider_profile(city);
CREATE INDEX idx_provider_profile_location ON provider_profile(latitude, longitude);
CREATE INDEX idx_service_provider ON service(provider_id);
CREATE INDEX idx_service_category ON service(category_id);
CREATE INDEX idx_service_active ON service(active) WHERE active = TRUE;
CREATE INDEX idx_event_planner ON event(planner_id);
CREATE INDEX idx_event_date ON event(event_date);
CREATE INDEX idx_availability_provider_date ON availability(provider_id, date);
CREATE INDEX idx_inquiry_provider ON inquiry(provider_id);
CREATE INDEX idx_inquiry_planner ON inquiry(planner_id);
CREATE INDEX idx_review_provider ON review(provider_id);
CREATE INDEX idx_bookmark_planner ON bookmark(planner_id);
