-- Описание задачи — HTML (рич-текст, таблицы); varchar(255) недостаточно.
DO $$
BEGIN
  IF to_regclass('public.issue') IS NOT NULL THEN
    ALTER TABLE issue ALTER COLUMN description TYPE TEXT;
  END IF;
END $$;
