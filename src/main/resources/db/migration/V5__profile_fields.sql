ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS photo_url          VARCHAR(500),
    ADD COLUMN IF NOT EXISTS cargo              VARCHAR(200),
    ADD COLUMN IF NOT EXISTS sobre              TEXT,
    ADD COLUMN IF NOT EXISTS prontidao_stack    VARCHAR(150),
    ADD COLUMN IF NOT EXISTS alocacao_status    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS nivel_mentoria     INT,
    ADD COLUMN IF NOT EXISTS autonomia          VARCHAR(150),
    ADD COLUMN IF NOT EXISTS trilha_carreira    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS certificacoes_count VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nivel_acompanhamento VARCHAR(150);
