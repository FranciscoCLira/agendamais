-- Flyway migration: add SMTP columns to instituicao
ALTER TABLE instituicao ADD COLUMN smtp_host VARCHAR(255);
ALTER TABLE instituicao ADD COLUMN smtp_port INTEGER;
ALTER TABLE instituicao ADD COLUMN smtp_username VARCHAR(255);
ALTER TABLE instituicao ADD COLUMN smtp_password VARCHAR(1000);
ALTER TABLE instituicao ADD COLUMN smtp_ssl BOOLEAN;
