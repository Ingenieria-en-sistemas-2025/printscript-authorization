BEGIN;

ALTER TABLE "authorization"
DROP CONSTRAINT IF EXISTS authorization_authorization_scope_id_fkey;

ALTER TABLE authorization_scope
    ALTER COLUMN id DROP DEFAULT,
ALTER COLUMN id TYPE TEXT USING id::text;

ALTER TABLE "authorization"
ALTER COLUMN authorization_scope_id TYPE TEXT USING authorization_scope_id::text;

ALTER TABLE "authorization"
    ALTER COLUMN id DROP DEFAULT,
ALTER COLUMN id TYPE TEXT USING id::text;

ALTER TABLE "authorization"
    ADD CONSTRAINT authorization_authorization_scope_id_fkey
        FOREIGN KEY (authorization_scope_id)
            REFERENCES authorization_scope(id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

COMMIT;
