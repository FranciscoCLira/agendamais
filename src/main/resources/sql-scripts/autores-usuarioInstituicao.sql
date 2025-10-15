-- Conferencia: filtro do dropdown/autocomplete de Autor para listar apenas autores vinculados à instituição logada.
SELECT
    i.id AS instituicao_id,
    i.nome_instituicao,
    u.username,
    ui.id AS  usuarioInstituicao_id,    
    a.id AS autor_id,
    p.id AS pessoa_id,
    p.nome_pessoa,
    p.email_pessoa,
    p.situacao_pessoa,
    ui.sit_acesso_usuario_instituicao,
    ui.nivel_acesso_usuario_instituicao
FROM autor a
JOIN pessoa p ON a.pessoa_id = p.id
JOIN usuario u ON u.pessoa_id = p.id
JOIN usuario_instituicao ui ON ui.usuario_id = u.id
JOIN instituicao i ON i.id = ui.instituicao_id
WHERE ui.sit_acesso_usuario_instituicao = 'A'
  AND i.id = 33
ORDER BY p.nome_pessoa;
