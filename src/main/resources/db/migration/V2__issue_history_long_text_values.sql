-- Flyway стартует до Hibernate: таблицы может ещё не быть.
-- Уже существующие БД: колонки были varchar(255), нужен TEXT под HTML описания.
DO $$
BEGIN
  IF to_regclass('public.issue_history') IS NOT NULL THEN
    ALTER TABLE issue_history ALTER COLUMN old_value TYPE TEXT;
    ALTER TABLE issue_history ALTER COLUMN new_value TYPE TEXT;
  END IF;
END $$;
