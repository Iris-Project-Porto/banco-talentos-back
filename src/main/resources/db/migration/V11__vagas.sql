CREATE TABLE vagas (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo            VARCHAR(255) NOT NULL,
    senioridade       VARCHAR(10)  NOT NULL CHECK (senioridade IN ('Jr', 'Pleno', 'Sr')),
    time              VARCHAR(100) NOT NULL,
    solicitante       VARCHAR(100) NOT NULL,
    tempo_contratacao VARCHAR(100),
    numero_vagas      INT          NOT NULL DEFAULT 1,
    area              VARCHAR(100),
    skills            TEXT[],
    descricao         TEXT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'Aberta'
                      CHECK (status IN ('Aberta', 'Em andamento', 'Fechada', 'Cancelada')),
    prioridade        VARCHAR(10)  NOT NULL DEFAULT 'Media'
                      CHECK (prioridade IN ('Baixa', 'Media', 'Alta', 'Urgente')),
    data_abertura     DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
