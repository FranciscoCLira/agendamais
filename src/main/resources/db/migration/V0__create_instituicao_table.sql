-- Flyway migration: create base instituicao table (columns present before SMTP additions)
CREATE TABLE instituicao (
  -- Use Postgres identity/serial for auto-incrementing primary key
  id BIGSERIAL PRIMARY KEY,
  nome_instituicao VARCHAR(255),
  situacao_instituicao VARCHAR(10),
  data_ultima_atualizacao DATE,
  email_instituicao VARCHAR(255)
);
