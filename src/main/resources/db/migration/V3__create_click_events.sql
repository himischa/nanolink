CREATE TABLE click_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id     UUID         NOT NULL REFERENCES links(id) ON DELETE CASCADE,
    clicked_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    referer     TEXT
);

CREATE INDEX idx_click_events_link_id    ON click_events(link_id);
CREATE INDEX idx_click_events_clicked_at ON click_events(clicked_at);
