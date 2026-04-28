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
