-- Reference DDL for `users` / `accounts` (phase1 entity model).
-- The running app does not execute this file; local dev typically relies on JPA (ddl-auto).
-- Use for manual setup, DBA review, or if you later adopt migrations again.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    display_name VARCHAR(255) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    auth_provider VARCHAR(64) NOT NULL,
    auth_subject VARCHAR(1024) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_accounts_provider_subject UNIQUE (auth_provider, auth_subject),
    CONSTRAINT uk_accounts_user_id UNIQUE (user_id),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
