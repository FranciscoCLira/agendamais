SELECT
  i.id AS instituicao_id,
  i.nome_instituicao,
  u.username,
  ui.usuario_id AS  idUsuarioInstituicao
  a.id AS autor_id,
  p.id AS pessoa_id,
  p.nome_pessoa,
  p.situacao_pessoa,
  p.email_pessoa
FROM autor a
JOIN pessoa p ON a.pessoa_id = p.id
JOIN usuario u ON u.pessoa_id = p.id
JOIN usuario_instituicao ui ON ui.usuario_id = u.id
JOIN instituicao i ON ui.instituicao_id = i.id
WHERE ui.sit_acesso_usuario_instituicao = 'A'
  AND INSTITUICAO_ID = 33  
ORDER BY INSTITUICAO_ID, NOME_PESSOA