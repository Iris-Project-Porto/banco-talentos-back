ALTER TABLE skills ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'HARD';
ALTER TABLE skills ALTER COLUMN name TYPE VARCHAR(255);

INSERT INTO skills (id, name, type, active, importance_weight) VALUES
(gen_random_uuid(), 'COMPROMETIMENTO E ENGAJAMENTO', 'SOFT', true, 1),
(gen_random_uuid(), 'PROATIVIDADE', 'SOFT', true, 1),
(gen_random_uuid(), 'COMUNICAÇÃO TÉCNICA PARA NEGÓCIO', 'SOFT', true, 1),
(gen_random_uuid(), 'COMUNICAÇÃO ASSERTIVA', 'SOFT', true, 1),
(gen_random_uuid(), 'ORGANIZAÇÃO E PRIORIDADES', 'SOFT', true, 1),
(gen_random_uuid(), 'TRABALHO EM EQUIPE E COLABORAÇÃO', 'SOFT', true, 1),
(gen_random_uuid(), 'GESTÃO DO TEMPO E PRAZOS', 'SOFT', true, 1),
(gen_random_uuid(), 'RESOLUÇÃO DE PROBLEMAS', 'SOFT', true, 1),
(gen_random_uuid(), 'PENSAMENTO CRÍTICO E CENÁRIOS', 'SOFT', true, 1),
(gen_random_uuid(), 'ADERÊNCIA A PROCESSOS E PADRÕES', 'SOFT', true, 1),
(gen_random_uuid(), 'TRANSPARÊNCIA (IMPEDIMENTOS)', 'SOFT', true, 1),
(gen_random_uuid(), 'FOCO EM RESULTADOS', 'SOFT', true, 1),
(gen_random_uuid(), 'SENSO DE DONO (OWNERSHIP)', 'SOFT', true, 1),
(gen_random_uuid(), 'ADAPTABILIDADE E FLEXIBILIDADE', 'SOFT', true, 1),
(gen_random_uuid(), 'DISPONIBILIDADE NO CHAT', 'SOFT', true, 1)
ON CONFLICT (name) DO UPDATE SET type = 'SOFT';
