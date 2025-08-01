-- ====================================================================
-- MIGRAÇÃO: Normalização dos campos de País/Estado/Cidade na tabela Pessoa
-- Data: 2024
-- Descrição: Migra os dados dos campos de nome (String) para referências
--            por ID (chave estrangeira) para a tabela Local
-- ====================================================================

-- Primeiro, adicionar as colunas de chave estrangeira na tabela Pessoa
-- (Se ainda não existirem - o JPA deve criá-las automaticamente)
-- ALTER TABLE pessoa ADD COLUMN id_pais BIGINT;
-- ALTER TABLE pessoa ADD COLUMN id_estado BIGINT;
-- ALTER TABLE pessoa ADD COLUMN id_cidade BIGINT;

-- ====================================================================
-- FASE 1: Migração dos Países
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
AND TRIM(pessoa.nome_pais_pessoa) != '';

-- Log de países não encontrados
SELECT DISTINCT 'PAÍS NÃO ENCONTRADO: ' || nome_pais_pessoa as log
FROM pessoa 
WHERE nome_pais_pessoa IS NOT NULL 
AND TRIM(nome_pais_pessoa) != ''
AND id_pais IS NULL;

-- ====================================================================
-- FASE 2: Migração dos Estados
-- ====================================================================

-- Atualizar referências de estado (considerando o país pai)
UPDATE pessoa 
SET id_estado = (
    SELECT l.id 
    FROM local l 
    WHERE l.tipo_local = 2 
    AND UPPER(TRIM(l.nome_local)) = UPPER(TRIM(pessoa.nome_estado_pessoa))
    AND (
        -- Se temos país definido, deve bater com o pai
        (pessoa.id_pais IS NOT NULL AND l.id_pai = pessoa.id_pais)
        OR 
        -- Se não temos país, pega qualquer estado com esse nome
        (pessoa.id_pais IS NULL)
    )
    LIMIT 1
)
WHERE pessoa.nome_estado_pessoa IS NOT NULL 
AND TRIM(pessoa.nome_estado_pessoa) != '';

-- Log de estados não encontrados
SELECT DISTINCT 'ESTADO NÃO ENCONTRADO: ' || nome_estado_pessoa || 
       ' (País: ' || COALESCE(nome_pais_pessoa, 'N/A') || ')' as log
FROM pessoa 
WHERE nome_estado_pessoa IS NOT NULL 
AND TRIM(nome_estado_pessoa) != ''
AND id_estado IS NULL;

-- ====================================================================
-- FASE 3: Migração das Cidades
-- ====================================================================

-- Atualizar referências de cidade (considerando o estado pai)
UPDATE pessoa 
SET id_cidade = (
    SELECT l.id 
    FROM local l 
    WHERE l.tipo_local = 3 
    AND UPPER(TRIM(l.nome_local)) = UPPER(TRIM(pessoa.nome_cidade_pessoa))
    AND (
        -- Se temos estado definido, deve bater com o pai
        (pessoa.id_estado IS NOT NULL AND l.id_pai = pessoa.id_estado)
        OR 
        -- Se não temos estado mas temos país, busca cidade filha de qualquer estado desse país
        (pessoa.id_estado IS NULL AND pessoa.id_pais IS NOT NULL 
         AND l.id_pai IN (SELECT e.id FROM local e WHERE e.tipo_local = 2 AND e.id_pai = pessoa.id_pais))
        OR
        -- Se não temos hierarquia, pega qualquer cidade com esse nome
        (pessoa.id_estado IS NULL AND pessoa.id_pais IS NULL)
    )
    LIMIT 1
)
WHERE pessoa.nome_cidade_pessoa IS NOT NULL 
AND TRIM(pessoa.nome_cidade_pessoa) != '';

-- Log de cidades não encontradas
SELECT DISTINCT 'CIDADE NÃO ENCONTRADA: ' || nome_cidade_pessoa || 
       ' (Estado: ' || COALESCE(nome_estado_pessoa, 'N/A') || 
       ', País: ' || COALESCE(nome_pais_pessoa, 'N/A') || ')' as log
FROM pessoa 
WHERE nome_cidade_pessoa IS NOT NULL 
AND TRIM(nome_cidade_pessoa) != ''
AND id_cidade IS NULL;

-- ====================================================================
-- RELATÓRIO FINAL DA MIGRAÇÃO
-- ====================================================================

-- Resumo da migração
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

-- ====================================================================
-- INSTRUÇÕES PARA APÓS A MIGRAÇÃO
-- ====================================================================

-- IMPORTANTE: Após validar a migração, executar:
-- 
-- 1. Testar o sistema com os novos campos
-- 2. Validar que todos os formulários funcionam
-- 3. Quando tudo estiver OK, remover os campos antigos:
--    ALTER TABLE pessoa DROP COLUMN nome_pais_pessoa;
--    ALTER TABLE pessoa DROP COLUMN nome_estado_pessoa;  
--    ALTER TABLE pessoa DROP COLUMN nome_cidade_pessoa;
--
-- 4. Adicionar constraints de chave estrangeira:
--    ALTER TABLE pessoa ADD CONSTRAINT fk_pessoa_pais 
--        FOREIGN KEY (id_pais) REFERENCES local(id);
--    ALTER TABLE pessoa ADD CONSTRAINT fk_pessoa_estado 
--        FOREIGN KEY (id_estado) REFERENCES local(id);
--    ALTER TABLE pessoa ADD CONSTRAINT fk_pessoa_cidade 
--        FOREIGN KEY (id_cidade) REFERENCES local(id);
