CREATE TABLE authorization_scope (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     name VARCHAR(50) NOT NULL UNIQUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT now(),
                                     updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Indice para buscar por nombre
CREATE INDEX ix_authorization_scope_name ON authorization_scope(name);

-- Niveles de acceso:
INSERT INTO authorization_scope (name)
VALUES ('OWNER'), ('EDITOR'), ('READER')
    ON CONFLICT (name) DO NOTHING;