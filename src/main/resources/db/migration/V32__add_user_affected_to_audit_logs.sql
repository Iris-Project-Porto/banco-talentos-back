ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS user_affected_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_audit_logs_user_affected'
          AND conrelid = 'audit_logs'::regclass
    ) THEN
        ALTER TABLE audit_logs
            ADD CONSTRAINT fk_audit_logs_user_affected
            FOREIGN KEY (user_affected_id)
            REFERENCES users(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

UPDATE audit_logs audit_log
SET user_affected_id = profile.user_id
FROM profiles profile
WHERE audit_log.entity_type = 'PROFILE'
  AND audit_log.entity_id = profile.id
  AND audit_log.user_affected_id IS NULL;

UPDATE audit_logs audit_log
SET user_affected_id = profile.user_id
FROM resource_equipments equipment
JOIN profiles profile ON profile.id = equipment.profile_id
WHERE audit_log.entity_type = 'RESOURCE_EQUIPMENT'
  AND audit_log.entity_id = equipment.id
  AND audit_log.user_affected_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_affected_created_at
    ON audit_logs(user_affected_id, created_at DESC);
