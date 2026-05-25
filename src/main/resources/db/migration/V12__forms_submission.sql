CREATE TABLE form_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    form_definition_id UUID NOT NULL REFERENCES form_definitions(id),
    user_id UUID NOT NULL REFERENCES users(id),
    answers JSONB,
    updated_at TIMESTAMP DEFAULT NOW()
);