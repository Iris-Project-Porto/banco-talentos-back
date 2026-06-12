-- Adição de colunas de auditoria faltantes em todas as tabelas

-- 1. Tabela users (já possui created_at)
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);

-- 2. Tabela profiles (já possui created_at e updated_at)
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);

-- 3. Tabela skills
ALTER TABLE skills ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE skills ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE skills ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);

-- 4. Tabela profile_skills
ALTER TABLE profile_skills ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE profile_skills ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE profile_skills ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE profile_skills ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);

-- 5. Tabela form_definitions
ALTER TABLE form_definitions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE form_definitions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE form_definitions ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE form_definitions ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);

-- 6. Tabela form_submissions (já possui updated_at)
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS created_by_id UUID REFERENCES users(id);
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS updated_by_id UUID REFERENCES users(id);
