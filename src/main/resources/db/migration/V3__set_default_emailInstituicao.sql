-- One-time migration: Set a default email for existing institutions with null email
UPDATE instituicao SET email_instituicao = CONCAT('instituicao', id, '@example.com') WHERE email_instituicao IS NULL;
