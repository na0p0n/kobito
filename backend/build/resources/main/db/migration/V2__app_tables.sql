-- Application tables for Wakaba

CREATE TABLE IF NOT EXISTS contributions (
    id                BIGSERIAL    PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contribution_date DATE         NOT NULL,
    contribution_type VARCHAR(16)  NOT NULL CHECK (contribution_type IN ('COMMIT', 'PR', 'ISSUE', 'REVIEW')),
    count             INT          NOT NULL DEFAULT 0,
    synced_at         TIMESTAMPTZ  NOT NULL,
    UNIQUE(user_id, contribution_date, contribution_type)
);

CREATE TABLE IF NOT EXISTS goals (
    id                BIGSERIAL    PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(255) NOT NULL,
    contribution_type VARCHAR(16)  NOT NULL CHECK (contribution_type IN ('COMMIT', 'PR', 'ISSUE', 'REVIEW')),
    target_count      INT          NOT NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS app_config (
    key        VARCHAR(64) PRIMARY KEY,
    value      TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_config (key, value) VALUES
    ('discord_webhook_url', NULL),
    ('digest_send_day',     'MONDAY'),
    ('digest_send_hour',    '9')
ON CONFLICT (key) DO NOTHING;
