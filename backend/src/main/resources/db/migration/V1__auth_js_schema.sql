-- Auth.js v5 PostgreSQL Adapter standard schema

CREATE TABLE IF NOT EXISTS users (
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name          VARCHAR(255),
    email         VARCHAR(255) UNIQUE,
    email_verified TIMESTAMPTZ,
    image         TEXT
);

CREATE TABLE IF NOT EXISTS accounts (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type                VARCHAR(255) NOT NULL,
    provider            VARCHAR(255) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    refresh_token       TEXT,
    access_token        TEXT,
    expires_at          BIGINT,
    token_type          VARCHAR(255),
    scope               TEXT,
    id_token            TEXT,
    session_state       TEXT,
    UNIQUE(provider, provider_account_id)
);

CREATE TABLE IF NOT EXISTS sessions (
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires       TIMESTAMPTZ  NOT NULL
);

CREATE TABLE IF NOT EXISTS verification_tokens (
    identifier VARCHAR(255) NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires    TIMESTAMPTZ  NOT NULL,
    UNIQUE(identifier, token)
);
