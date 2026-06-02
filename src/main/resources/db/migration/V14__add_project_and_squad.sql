CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id)
);

CREATE TABLE squads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    porto_coordinator VARCHAR(255),
    project_manager VARCHAR(255),
    project_id UUID REFERENCES projects(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id)
);

CREATE TABLE squad_skills (
    squad_id UUID NOT NULL REFERENCES squads(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id),
    PRIMARY KEY (squad_id, skill_id)
);

ALTER TABLE skills ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE skills ADD COLUMN importance_weight INT DEFAULT 1;

CREATE TABLE job_postings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES projects(id),
    squad_id UUID REFERENCES squads(id),
    experience_level VARCHAR(50) NOT NULL,
    description TEXT,
    requirements TEXT NOT NULL,
    recruiter VARCHAR(255) NOT NULL,
    estimated_allocation_weeks INT,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    opening_date TIMESTAMP NOT NULL,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Ajuste nas skills dos perfis: substitui o level (String) por proficiency_level (Int)
-- Primeiro vem a nova coluna
ALTER TABLE profile_skills ADD COLUMN proficiency_level INT DEFAULT 0;

-- Migramos dados numéricos existentes
UPDATE profile_skills 
SET proficiency_level = CAST(level AS INTEGER) 
WHERE level ~ '^[0-9]+$';

-- Removemos a coluna antiga
ALTER TABLE profile_skills DROP COLUMN level;

-- Campos de matrícula no perfil
ALTER TABLE profiles ADD COLUMN registration_number VARCHAR(50);
ALTER TABLE profiles ADD COLUMN registration_status VARCHAR(50) DEFAULT 'NOT_REQUESTED';
