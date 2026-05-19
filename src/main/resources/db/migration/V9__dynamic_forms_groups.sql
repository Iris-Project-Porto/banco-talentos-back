CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id)
);

INSERT INTO groups (name) VALUES ('QA');
INSERT INTO groups (name) VALUES ('DEV Front-end');
INSERT INTO groups (name) VALUES ('DEV Back-end');
INSERT INTO groups (name) VALUES ('Gerente');

ALTER TABLE users ADD COLUMN group_id UUID REFERENCES groups(id);

-- Atribui usuários existentes ao grupo 'Gerente' temporariamednte
UPDATE users SET group_id = (SELECT id FROM groups WHERE name = 'Gerente' LIMIT 1);

ALTER TABLE users ALTER COLUMN group_id SET NOT NULL;
