
-- Usuários (cod_usuario agora é chave primária String, até 25 caracteres, sem espaços)
INSERT INTO usuario (cod_usuario, senha, nivel_acesso_usuario) VALUES ('admin01', 'admin123', 1);
INSERT INTO usuario (cod_usuario, senha, nivel_acesso_usuario) VALUES ('autor01', 'autor123', 2);

-- Pessoas (referência cod_usuario como FK)
INSERT INTO pessoa (id, cod_usuario_cod_usuario, nome_pessoa, situacao_pessoa, email_pessoa, celular_pessoa, pais_pessoa, estado_endereco_pessoa, cidade_endereco_pessoa, comentarios, data_inclusao, data_ultima_atualizacao, curriculo_pessoal)
VALUES (1, 'admin01', 'Administrador Geral', 'A', 'admin@agenda.com', '+55-11-99999-0000', 'Brasil', 'SP', 'São Paulo', 'Usuário inicial do sistema', CURRENT_DATE, CURRENT_DATE, 'Administrador do sistema');

INSERT INTO pessoa (id, cod_usuario_cod_usuario, nome_pessoa, situacao_pessoa, email_pessoa, celular_pessoa, pais_pessoa, estado_endereco_pessoa, cidade_endereco_pessoa, comentarios, data_inclusao, data_ultima_atualizacao, curriculo_pessoal)
VALUES (2, 'autor01', 'João Autor', 'A', 'joao@autor.com', '+55-21-88888-1111', 'Brasil', 'RJ', 'Rio de Janeiro', 'Autor de conteúdo', CURRENT_DATE, CURRENT_DATE, 'Experiência em palestras');

-- TipoAtividade
INSERT INTO tipo_atividade (id, titulo_tipo_atividade, descricao_tipo_atividade) VALUES (1, 'Palestra', 'Apresentação formal de conteúdo');
INSERT INTO tipo_atividade (id, titulo_tipo_atividade, descricao_tipo_atividade) VALUES (2, 'Encontro', 'Reunião de participantes com tema livre');

-- Instituicao
INSERT INTO instituicao (id, nome_instituicao, situacao_instituicao, data_ultima_atualizacao) VALUES (1, 'Instituto Luz', 'A', CURRENT_DATE);

-- SubInstituicao
INSERT INTO sub_instituicao (id, id_instituicao_id, nome_sub_instituicao, situacao_sub_instituicao, data_ultima_atualizacao)
VALUES (1, 1, 'Núcleo São Paulo', 'A', CURRENT_DATE);
