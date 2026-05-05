INSERT INTO users (id, name, email, password, role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Administrador', 'admin@empresa.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000002', 'João Silva', 'joao@empresa.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'RECURSO')
ON CONFLICT (email) DO NOTHING;
