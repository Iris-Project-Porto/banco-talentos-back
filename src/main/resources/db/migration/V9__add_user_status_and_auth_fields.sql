-- Campos de Status e Auditoria
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;
ALTER TABLE users ADD COLUMN approved_by UUID;
ALTER TABLE users ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD CONSTRAINT fk_users_approved_by FOREIGN KEY (approved_by) REFERENCES users(id);

-- Campos de Verificação de E-mail e Redefinição de Senha
ALTER TABLE users ADD COLUMN verification_code VARCHAR(6);
ALTER TABLE users ADD COLUMN reset_token VARCHAR(36);
ALTER TABLE users ADD COLUMN reset_token_expires TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE NOT NULL;

-- Atualiza usuários existentes (se houver)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;
UPDATE users SET email_verified = TRUE WHERE email_verified IS NULL;
