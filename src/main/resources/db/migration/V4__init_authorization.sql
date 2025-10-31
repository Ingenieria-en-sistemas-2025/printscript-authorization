CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS "authorization" (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    snippet_id VARCHAR(100) NOT NULL,
    user_id    VARCHAR(100) NOT NULL,
    authorization_scope_id UUID NOT NULL REFERENCES authorization_scope(id),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (user_id, snippet_id),

    CHECK (char_length(user_id) > 0),
    CHECK (char_length(snippet_id) > 0)
    );

CREATE INDEX IF NOT EXISTS ix_authorization_user    ON "authorization"(user_id);
CREATE INDEX IF NOT EXISTS ix_authorization_snippet ON "authorization"(snippet_id);
CREATE INDEX IF NOT EXISTS ix_authorization_scope   ON "authorization"(authorization_scope_id);
