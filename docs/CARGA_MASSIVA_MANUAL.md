# Carga Massiva de Usuários - AgendaMais

## Como Acessar
1. Faça login como usuário com permissão de Controle Total
2. Acesse **Menu Controle Total**
3. Clique em **"Carga Massiva de Usuários"**

## Formatos Suportados
- **CSV** (.csv) - Recomendado
- **Excel** (.xlsx, .xls) - Convertido automaticamente para CSV

## Estrutura do Arquivo

### Colunas Obrigatórias:
- `email` - Email único do usuário
- `nome` - Nome completo da pessoa
- `celular` - Número de celular (vários formatos aceitos)
- `pais` - País (ex: Brasil)
- `estado` - Estado/UF (ex: São Paulo)
- `cidade` - Cidade (ex: São Paulo)

### Colunas Opcionais:
- `comentarios` - Observações sobre o usuário
- `instituicaoId` - ID da instituição (numérico)
- `identificacaoPessoaInstituicao` - Identificação da pessoa na instituição
- `subInstituicaoId` - ID da sub-instituição (numérico)
- `identificacaoPessoaSubInstituicao` - Identificação da pessoa na sub-instituição
- `username` - Nome de usuário (se não informado, será gerado automaticamente)
- `password` - Senha (se não informada, será gerada automaticamente)

## Tipos de Carga

### Carga de Teste
- **Prefixo username**: X (ex: X00001, X00002, ...)
- **Sufixo senha**: $ (ex: X00001$, X00002$, ...)
- **Uso**: Para testes e demonstrações

### Carga Real
- **Prefixo username**: U (ex: U00001, U00002, ...)
- **Sufixo senha**: (nenhum) (ex: U00001, U00002, ...)
- **Uso**: Para usuários de produção

## Formatos de Celular Aceitos
- `+55 11 99999-9999`
- `11 99999-9999`
- `11999999999`
- `(11) 99999-9999`
- `55 11 99999-9999`

*Todos os formatos são automaticamente convertidos para: `+55-11-99999-9999`*

## Exemplo de Arquivo CSV

```csv
email;nome;celular;pais;estado;cidade;comentarios;instituicaoId;identificacaoPessoaInstituicao;subInstituicaoId;identificacaoPessoaSubInstituicao
joao@email.com;João Silva;11999999999;Brasil;São Paulo;São Paulo;Usuario de teste;;;
maria@email.com;Maria Santos;21888888888;Brasil;Rio de Janeiro;Rio de Janeiro;Usuario de produção;;;
pedro@teste.com;Pedro Oliveira;11987654321;Brasil;São Paulo;Campinas;Usuario exemplo;;;
```

## Processo de Importação

1. **Upload**: Arraste o arquivo ou clique para selecionar
2. **Configuração**: 
   - Escolha o tipo de carga (Teste ou Real)
   - Configure as opções de validação
   - Defina separador CSV se necessário
3. **Validação**: Clique em "Validar Arquivo" para verificar o formato
4. **Processamento**: Clique em "Processar Arquivo" para importar
5. **Resultado**: Visualize o log de processamento e baixe o arquivo com credenciais

## Validações Automáticas
- Email único no sistema
- Formato de celular válido
- Campos obrigatórios preenchidos
- Instituições existentes (se informadas)

## Arquivo de Resultado
Após o processamento, será gerado um arquivo CSV contendo:
- Todos os dados importados
- Usernames e senhas gerados
- Status do processamento de cada registro

## Dicas Importantes
- Use ponto e vírgula (;) como separador no CSV
- Salve o Excel como CSV UTF-8 para preservar acentos
- Verifique se não há emails duplicados no arquivo
- Teste primeiro com carga de teste antes de fazer carga real
- Faça backup do banco antes de grandes importações

## Limitações
- Tamanho máximo: 10MB
- Formatos suportados: CSV, XLSX, XLS
- Emails devem ser únicos no sistema
- Senhas seguem política de segurança do sistema

## Suporte
Para dúvidas ou problemas, verifique:
1. Log de processamento na tela
2. Mensagens de erro específicas
3. Formato do arquivo de exemplo fornecido
