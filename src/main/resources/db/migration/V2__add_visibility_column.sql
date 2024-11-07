-- Сначала добавляем колонку без ограничений
ALTER TABLE groups
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(255);

-- Устанавливаем значение по умолчанию для существующих записей
UPDATE groups
SET visibility = 'PUBLIC'
WHERE visibility IS NULL;

-- Теперь добавляем ограничения с проверкой существования
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'check_visibility'
        ) THEN
            ALTER TABLE groups
                ADD CONSTRAINT check_visibility CHECK (visibility IN ('PUBLIC', 'RESTRICTED', 'PRIVATE'));
        END IF;
    END $$;

-- Устанавливаем значение по умолчанию для новых записей
ALTER TABLE groups
    ALTER COLUMN visibility SET DEFAULT 'PUBLIC';

-- Добавляем комментарий
COMMENT ON COLUMN groups.visibility IS 'Тип видимости группы: PUBLIC, RESTRICTED или PRIVATE';