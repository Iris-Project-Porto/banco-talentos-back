INSERT INTO users (id, name, email, password, role) VALUES
    ('00000000-0000-0000-0000-000000000003', 'Maria Santos',   'maria@empresa.com', '$2a$10$w3u4UXrlDuibIq8cJJbYlOi8MILLXkhO3usiywiTYsfahCQ8KFRAK', 'RECURSO'),
    ('00000000-0000-0000-0000-000000000004', 'José Oliveira',  'jose@empresa.com',  '$2a$10$w3u4UXrlDuibIq8cJJbYlOi8MILLXkhO3usiywiTYsfahCQ8KFRAK', 'RECURSO'),
    ('00000000-0000-0000-0000-000000000005', 'M Silva',        'm@empresa.com',     '$2a$10$w3u4UXrlDuibIq8cJJbYlOi8MILLXkhO3usiywiTYsfahCQ8KFRAK', 'RECURSO'),
    ('00000000-0000-0000-0000-000000000006', 'Lionel Messi',   'messi@empresa.com', '$2a$10$w3u4UXrlDuibIq8cJJbYlOi8MILLXkhO3usiywiTYsfahCQ8KFRAK', 'RECURSO'),
    ('00000000-0000-0000-0000-000000000007', 'Neymar Jr',      'ney@empresa.com',   '$2a$10$w3u4UXrlDuibIq8cJJbYlOi8MILLXkhO3usiywiTYsfahCQ8KFRAK', 'RECURSO')
ON CONFLICT (email) DO NOTHING;
