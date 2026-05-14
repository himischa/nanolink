CREATE TABLE links (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_url  TEXT         NOT NULL,
    short_code    VARCHAR(20)  NOT NULL UNIQUE,
    custom_alias  VARCHAR(50)  UNIQUE,
    expires_at    TIMESTAMP,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_links_short_code   ON links(short_code);
CREATE INDEX idx_links_custom_alias ON links(custom_alias);
CREATE INDEX idx_links_user_id      ON links(user_id);
