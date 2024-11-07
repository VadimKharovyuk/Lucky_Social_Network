-- V1__add_requires_join_approval.sql
ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS requires_join_approval BOOLEAN DEFAULT false;
UPDATE groups
SET requires_join_approval = false
WHERE requires_join_approval IS NULL;
ALTER TABLE groups
    ALTER COLUMN requires_join_approval SET NOT NULL;

-- Добавляем комментарий к колонке
COMMENT ON COLUMN groups.requires_join_approval IS 'Флаг, определяющий, требуется ли одобрение для вступления в группу';