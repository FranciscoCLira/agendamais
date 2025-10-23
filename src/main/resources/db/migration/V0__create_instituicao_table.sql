-- Flyway migration: create base instituicao table (columns present before SMTP additions)
CREATE TABLE instituicao (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome_instituicao VARCHAR(255),
  situacao_instituicao VARCHAR(10),
  data_ultima_atualizacao DATE,
  email_instituicao VARCHAR(255)
);
