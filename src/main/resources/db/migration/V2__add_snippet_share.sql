CREATE TABLE snippet_share (
                               id BIGSERIAL PRIMARY KEY,
                               snippet_id BIGINT NOT NULL REFERENCES snippet(id) ON DELETE CASCADE,
                               shared_with_user_id VARCHAR(120) NOT NULL,
                               permission VARCHAR(20) NOT NULL DEFAULT 'READ',
                               created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX ix_share_snippet ON snippet_share(snippet_id);
CREATE INDEX ix_share_user ON snippet_share(shared_with_user_id);