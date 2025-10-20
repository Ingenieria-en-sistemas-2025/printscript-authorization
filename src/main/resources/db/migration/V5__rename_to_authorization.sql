CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
  IF to_regclass('public."authorization"') IS NULL
     AND to_regclass('public.authorizations') IS NOT NULL THEN
ALTER TABLE public.authorizations RENAME TO "authorization";
END IF;
END $$;

ALTER INDEX IF EXISTS ix_authorizations_user    RENAME TO ix_authorization_user;
ALTER INDEX IF EXISTS ix_authorizations_snippet RENAME TO ix_authorization_snippet;
ALTER INDEX IF EXISTS ix_authorizations_scope   RENAME TO ix_authorization_scope;

CREATE INDEX IF NOT EXISTS ix_authorization_user
    ON "authorization"(user_id);

CREATE INDEX IF NOT EXISTS ix_authorization_snippet
    ON "authorization"(snippet_id);

CREATE INDEX IF NOT EXISTS ix_authorization_scope
    ON "authorization"(authorization_scope_id);
