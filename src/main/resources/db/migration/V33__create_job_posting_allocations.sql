CREATE TABLE IF NOT EXISTS job_posting_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE SET NULL,
    squad_id UUID REFERENCES squads(id) ON DELETE SET NULL,
    start_date DATE NOT NULL,
    role VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_job_posting_allocations_job_posting_id
    ON job_posting_allocations(job_posting_id);

CREATE INDEX IF NOT EXISTS idx_job_posting_allocations_profile_active
    ON job_posting_allocations(profile_id, active);

CREATE INDEX IF NOT EXISTS idx_job_posting_allocations_squad_id
    ON job_posting_allocations(squad_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_job_posting_allocations_active_profile
    ON job_posting_allocations(profile_id)
    WHERE active = TRUE;
