CREATE TABLE service_category (
                                  id              BIGSERIAL PRIMARY KEY,
                                  name            VARCHAR(100) NOT NULL UNIQUE,
                                  slug            VARCHAR(100) NOT NULL UNIQUE,
                                  icon            VARCHAR(100),
                                  description     TEXT
);
