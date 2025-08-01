-- ====================================================================
-- MIGRAÇÃO SIMPLIFICADA: Normalização dos campos de País/Estado/Cidade 
-- Execute este script no H2 Console: http://localhost:8080/h2-console
-- ====================================================================

-- 1. Verificar dados atuais
SELECT 'SITUAÇÃO ATUAL' as status;
SELECT COUNT(*) as total_pessoas FROM pessoa;
SELECT COUNT(*) as total_locais FROM local;

-- Verificar se já existem referências normalizadas
SELECT COUNT(*) as pessoas_com_pais_id FROM pessoa WHERE id_pais IS NOT NULL;
SELECT COUNT(*) as pessoas_com_estado_id FROM pessoa WHERE id_estado IS NOT NULL;
SELECT COUNT(*) as pessoas_com_cidade_id FROM pessoa WHERE id_cidade IS NOT NULL;

-- 2. Amostras dos dados atuais
SELECT id, nome_pessoa, nome_pais_pessoa, nome_estado_pessoa, nome_cidade_pessoa 
FROM pessoa 
WHERE nome_pais_pessoa IS NOT NULL 
LIMIT 5;

SELECT id, tipo_local, nome_local, id_pai 
FROM local 
ORDER BY tipo_local, nome_local 
LIMIT 10;

-- ====================================================================
-- PARTE 1: MIGRAÇÃO DOS PAÍSES
-- ====================================================================

-- Atualizar referências de país
UPDATE pessoa 
SET id_pais = (
    SELECT l.id 
    FROM local l 
    WHERE l.tipo_local = 1 
    AND UPPER(TRIM(l.nome_local)) = UPPER(TRIM(pessoa.nome_pais_pessoa))
    AND l.id_pai IS NULL
    LIMIT 1
)
WHERE pessoa.nome_pais_pessoa IS NOT NULL 
AND TRIM(pessoa.nome_pais_pessoa) != ''
AND pessoa.id_pais IS NULL;

-- Verificar países migrados
SELECT COUNT(*) as paises_migrados FROM pessoa WHERE id_pais IS NOT NULL;

-- ====================================================================
-- PARTE 2: MIGRAÇÃO DOS ESTADOS  
-- ====================================================================

-- Atualizar referências de estado
UPDATE pessoa 
SET id_estado = (
    SELECT l.id 
    FROM local l 
    WHERE l.tipo_local = 2 
    AND UPPER(TRIM(l.nome_local)) = UPPER(TRIM(pessoa.nome_estado_pessoa))
    AND (
        (pessoa.id_pais IS NOT NULL AND l.id_pai = pessoa.id_pais)
        OR 
        (pessoa.id_pais IS NULL)
    )
    LIMIT 1
)
WHERE pessoa.nome_estado_pessoa IS NOT NULL 
AND TRIM(pessoa.nome_estado_pessoa) != ''
AND pessoa.id_estado IS NULL;

-- Verificar estados migrados
SELECT COUNT(*) as estados_migrados FROM pessoa WHERE id_estado IS NOT NULL;

-- ====================================================================
-- PARTE 3: MIGRAÇÃO DAS CIDADES
-- ====================================================================

-- Atualizar referências de cidade
UPDATE pessoa 
SET id_cidade = (
    SELECT l.id 
    FROM local l 
    WHERE l.tipo_local = 3 
    AND UPPER(TRIM(l.nome_local)) = UPPER(TRIM(pessoa.nome_cidade_pessoa))
    AND (
        (pessoa.id_estado IS NOT NULL AND l.id_pai = pessoa.id_estado)
        OR 
        (pessoa.id_estado IS NULL AND pessoa.id_pais IS NOT NULL 
         AND l.id_pai IN (SELECT e.id FROM local e WHERE e.tipo_local = 2 AND e.id_pai = pessoa.id_pais))
        OR
        (pessoa.id_estado IS NULL AND pessoa.id_pais IS NULL)
    )
    LIMIT 1
)
WHERE pessoa.nome_cidade_pessoa IS NOT NULL 
AND TRIM(pessoa.nome_cidade_pessoa) != ''
AND pessoa.id_cidade IS NULL;

-- Verificar cidades migradas
SELECT COUNT(*) as cidades_migradas FROM pessoa WHERE id_cidade IS NOT NULL;

-- ====================================================================
-- RELATÓRIO FINAL
-- ====================================================================

SELECT 'RELATÓRIO FINAL DA MIGRAÇÃO' as titulo;

SELECT 
    'TOTAL PESSOAS' as tipo,
    COUNT(*) as quantidade
FROM pessoa
UNION ALL
SELECT 
    'PESSOAS COM PAÍS MIGRADO' as tipo,
    COUNT(*) as quantidade
FROM pessoa WHERE id_pais IS NOT NULL
UNION ALL
SELECT 
    'PESSOAS COM ESTADO MIGRADO' as tipo,
    COUNT(*) as quantidade
FROM pessoa WHERE id_estado IS NOT NULL
UNION ALL
SELECT 
    'PESSOAS COM CIDADE MIGRADA' as tipo,
    COUNT(*) as quantidade
FROM pessoa WHERE id_cidade IS NOT NULL
UNION ALL
SELECT 
    'PESSOAS TOTALMENTE MIGRADAS' as tipo,
    COUNT(*) as quantidade
FROM pessoa WHERE id_pais IS NOT NULL AND id_estado IS NOT NULL AND id_cidade IS NOT NULL;

-- Verificar algumas pessoas após migração (teste)
SELECT 
    p.id,
    p.nome_pessoa,
    p.nome_pais_pessoa as pais_antigo,
    pais.nome_local as pais_novo,
    p.nome_estado_pessoa as estado_antigo,
    estado.nome_local as estado_novo,
    p.nome_cidade_pessoa as cidade_antigo,
    cidade.nome_local as cidade_novo
FROM pessoa p
LEFT JOIN local pais ON p.id_pais = pais.id
LEFT JOIN local estado ON p.id_estado = estado.id  
LEFT JOIN local cidade ON p.id_cidade = cidade.id
WHERE p.id_pais IS NOT NULL
LIMIT 5;
