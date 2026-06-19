DROP TABLE IF EXISTS squad_skills;

ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS vacancy_code VARCHAR(100) UNIQUE;
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS closing_date TIMESTAMP;
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS modality VARCHAR(100);

ALTER TABLE job_postings ALTER COLUMN status TYPE VARCHAR(50);
ALTER TABLE job_postings ALTER COLUMN status SET DEFAULT 'OPEN';

UPDATE job_postings SET status = 'OPEN' WHERE status IS NULL;
UPDATE job_postings SET title = 'Vaga sem título' WHERE title IS NULL;
ALTER TABLE job_postings ALTER COLUMN title SET NOT NULL;

DROP TABLE IF EXISTS job_posting_skills;

CREATE TABLE job_posting_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id),
    type VARCHAR(50) NOT NULL DEFAULT 'MANDATORY',
    min_level VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    importance_weight INT NOT NULL DEFAULT 1,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id),
    UNIQUE(job_posting_id, skill_id)
);
