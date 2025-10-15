-- USUARIOS INSCRITOS EM TIPOS DE ATIVIDADES POR INSTITUICAO 
SELECT
    i.id AS idInstituicao,
    i.nome_Instituicao,
    ta.id AS idTipoAtividade,
    ta.titulo_tipo_atividade,
    u.username,
    p.id AS id_pessoa,
    p.nome_pessoa,
    p.email_pessoa,
    p.celular_pessoa,
    p.situacao_pessoa,
    FORMATDATETIME(p.data_ultima_atualizacao, 'dd/MM/yy') AS dataUltimaAtualizacao,
    cidade.nome_local AS cidade,
    estado.nome_local AS estado,
    pais.nome_local AS pais
FROM
    pessoa p
    INNER JOIN usuario u ON p.id = u.pessoa_id
    INNER JOIN inscricao insc ON insc.id_pessoa = p.id
    INNER JOIN inscricao_tipo_atividade ita ON ita.inscricao_id = insc.id
    INNER JOIN tipo_atividade ta ON ita.tipo_atividade_id = ta.id
    INNER JOIN instituicao i ON insc.id_instituicao = i.id
    LEFT JOIN local cidade ON p.id_cidade = cidade.id
    LEFT JOIN local estado ON cidade.id_pai = estado.id
    LEFT JOIN local pais ON estado.id_pai = pais.id
ORDER BY
    i.id,
    ta.id,
    p.id, 
    p.nome_pessoa ASC;