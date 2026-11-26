CREATE TABLE job_posting_selected_candidates (
    job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL
);
