CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'RECURSO',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    area VARCHAR(100),
    nivel VARCHAR(20),
    nivel_override VARCHAR(20),
    nivel_score INT,
    nivel_justificativa TEXT,
    experience_years INT,
    projects_count INT,
    availability VARCHAR(100),
    certifications TEXT,
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE profile_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id),
    level VARCHAR(50),
    UNIQUE(profile_id, skill_id)
);

CREATE INDEX idx_profiles_status ON profiles(status);
CREATE INDEX idx_profiles_user_id ON profiles(user_id);
