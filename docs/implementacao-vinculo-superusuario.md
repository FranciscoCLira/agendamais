# Implementação: Auto-Vínculo de SuperUsuário com Instituições

## Problema Identificado

Quando o SuperUsuário cria uma nova instituição através do menu "Gerenciar Instituições", o sistema **não cria automaticamente** o vínculo `PessoaInstituicao` e `UsuarioInstituicao` entre o superusuário e a instituição criada.

**Resultado**: A nova instituição não aparece na lista de seleção no login do superusuário.

**Exemplo**: Instituição ID=4 "Loja R+C SCSul AMORC" foi criada pelo superusuário "superu" mas ele não consegue acessá-la no login.

## Solução Implementada

Criada uma funcionalidade específica para o superusuário se **auto-vincular** a instituições existentes, similar ao fluxo de cadastro inicial (`/cadastro-relacionamentos`) mas adaptado para o contexto de um usuário já existente.

## Arquivos Criados/Modificados

### 1. Controller Novo

**Arquivo**: `VinculoSuperusuarioController.java`

- **Localização**: `src/main/java/com/agendademais/controllers/`
- **Endpoints**:
  - `GET /superusuario/vincular-instituicoes` - Exibe formulário
  - `POST /superusuario/vincular-instituicoes` - Processa vínculos

**Funcionalidades**:

- Lista todas as instituições ativas
- Identifica quais instituições o superusuário já está vinculado
- Permite selecionar múltiplas instituições para vincular
- Cria vínculos em `PessoaInstituicao` e `UsuarioInstituicao`
- Define nível de acesso 9 (SuperUsuário) automaticamente
- Suporta vínculo com sub-instituições opcionalmente

### 2. View Nova

**Arquivo**: `vincular-instituicoes.html`

- **Localização**: `src/main/resources/templates/superusuario/`
- **Características**:
  - Tabela com todas as instituições
  - Destaque visual (verde) para instituições já vinculadas
  - Campos para data de afiliação e identificação
  - Seleção opcional de sub-instituição
  - Instruções de uso claras

### 3. Menu Atualizado

**Arquivo**: `menu-superusuario.html`

- **Modificação**: Adicionado novo item de menu
- **Link**: "Vincular-me a Instituições" (`/superusuario/vincular-instituicoes`)
- **Ícone**: `fa-link`

## Como Usar (Passo a Passo)

### Para o SuperUsuário "superu" se vincular à Instituição ID=4:

1. **Login como superusuário**:

   - Fazer login com usuário "superu"
   - Selecionar "**_ Controle Total _**" na lista de instituições

2. **Acessar a funcionalidade**:

   - No menu SuperUsuário, clicar em "Vincular-me a Instituições"
   - Ou acessar diretamente: `http://localhost:8080/superusuario/vincular-instituicoes`

3. **Selecionar instituições**:

   - Marcar checkbox da(s) instituição(ões) desejada(s)
   - Instituições já vinculadas aparecem com fundo verde e desabilitadas
   - Opcionalmente, preencher:
     - Data de afiliação (padrão: data atual)
     - ID/Identificação na instituição
     - Sub-instituição (se aplicável)

4. **Confirmar**:

   - Clicar em "Criar Vínculos"
   - Mensagem de sucesso será exibida

5. **Fazer novo login**:
   - Fazer logout
   - Fazer login novamente
   - A(s) nova(s) instituição(ões) aparecerão na lista de seleção

## Detalhes Técnicos

### Segurança

- Apenas usuários com nível 9 (SuperUsuário) ou nível 0 (Controle Total) podem acessar
- Validação de sessão e permissões em ambos endpoints (GET e POST)

### Validações

- Impede criação de vínculos duplicados
- Verifica se instituição existe e está ativa
- Valida datas de afiliação (não podem ser futuras)
- Requer seleção de ao menos uma instituição

### Criação de Vínculos

Para cada instituição selecionada, são criados:

1. **PessoaInstituicao**:

   - `pessoa_id`: ID da pessoa do superusuário
   - `instituicao_id`: ID da instituição selecionada
   - `identificacaoPessoaInstituicao`: Campo opcional
   - `dataAfiliacao`: Data informada ou data atual
   - `dataUltimaAtualizacao`: Data atual

2. **UsuarioInstituicao**:

   - `usuario_id`: ID do usuário superusuário
   - `instituicao_id`: ID da instituição selecionada
   - `sitAcessoUsuarioInstituicao`: "A" (Ativo)
   - `nivelAcessoUsuarioInstituicao`: 9 (SuperUsuário)

3. **PessoaSubInstituicao** (se selecionada):
   - Vínculos opcionais com sub-instituições

### Diferença do Cadastro Inicial

- **Cadastro inicial** (`/cadastro-relacionamentos`):

  - Para novos usuários completarem seu cadastro
  - **Deleta** todos os vínculos antigos antes de criar novos
  - Redireciona para `/acesso` após conclusão

- **Auto-vínculo superusuário** (`/superusuario/vincular-instituicoes`):
  - Para usuários existentes adicionarem vínculos
  - **Preserva** vínculos existentes, apenas adiciona novos
  - Redireciona para `/superusuario` após conclusão
  - Impede criação de duplicatas

## Testes Necessários

### Cenário 1: Vincular à Instituição ID=4

1. Login como "superu" no Controle Total
2. Acessar "Vincular-me a Instituições"
3. Marcar "Loja R+C SCSul AMORC" (ID=4)
4. Clicar "Criar Vínculos"
5. Fazer logout e login novamente
6. **Verificar**: Instituição ID=4 aparece na lista de login

### Cenário 2: Tentar vincular instituição já vinculada

1. Acessar "Vincular-me a Instituições"
2. **Verificar**: Instituições já vinculadas aparecem com fundo verde
3. **Verificar**: Checkboxes dessas instituições estão desabilitados
4. **Verificar**: Mensagem "(vinculado)" aparece ao lado

### Cenário 3: Vincular múltiplas instituições

1. Marcar 2 ou mais instituições não vinculadas
2. Clicar "Criar Vínculos"
3. **Verificar**: Mensagem mostra quantidade de vínculos criados
4. Fazer logout/login
5. **Verificar**: Todas aparecem na lista de login

### Cenário 4: Validações

1. Tentar acessar sem ser superusuário
   - **Esperado**: Acesso negado
2. Tentar enviar sem selecionar nenhuma instituição
   - **Esperado**: Mensagem de erro
3. Informar data de afiliação futura
   - **Esperado**: Mensagem de erro

## Logs/Debug

O controller não possui logs de debug específicos, mas pode adicionar se necessário para troubleshooting.

## Próximos Passos Sugeridos

### Melhorias Futuras (opcional):

1. **Auto-vínculo automático**: Modificar o controller de criação de instituições para vincular automaticamente o superusuário criador
2. **Edição de vínculos**: Permitir editar/remover vínculos existentes
3. **Auditoria**: Log de quando vínculos são criados/removidos
4. **Notificação**: Aviso visual no menu se houver instituições sem vínculo

## Compatibilidade

- ✅ Não afeta funcionalidades existentes
- ✅ Não modifica banco de dados (usa estrutura existente)
- ✅ Compatível com fluxo de login atual
- ✅ Mantém nível de acesso 9 (SuperUsuário)

## Status

✅ **IMPLEMENTADO E TESTADO (compilação)**
⏳ **AGUARDANDO TESTE FUNCIONAL** com superusuário "superu" e instituição ID=4

---

**Data**: 08/12/2025
**Desenvolvedor**: GitHub Copilot
**Versão**: 1.0
