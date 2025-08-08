# Agenda Mais - Modelo ER Atualizado V2.0

## Descrição Geral

Modelo de dados atualizado após refatoração arquitetural que migrou os níveis de acesso de uma abordagem global (campo `nivelAcessoUsuario` na entidade `Usuario`) para uma abordagem por instituição (campo `nivelAcessoUsuarioInstituicao` na entidade `UsuarioInstituicao`).

## Principais Mudanças Arquiteturais

### ✅ ANTES (V1.0)

- `Usuario.nivelAcessoUsuario` → Nível global do usuário
- Usuário tinha apenas um nível de acesso para todo o sistema
- **Problema**: "Furo conceitual" - não permitia diferentes níveis por instituição

### ✅ DEPOIS (V2.0)

- `UsuarioInstituicao.nivelAcessoUsuarioInstituicao` → Nível por instituição
- Usuário pode ter diferentes níveis de acesso em diferentes instituições
- **Solução**: Flexibilidade total - usuário pode ser Participante em uma instituição e Administrador em outra

## Níveis de Acesso Suportados

| Nível | Descrição          | Capacidades                                           |
| ----- | ------------------ | ----------------------------------------------------- |
| 1     | **Participante**   | Acesso básico, visualização de atividades, inscrições |
| 2     | **Autor**          | Criação de atividades, gestão de conteúdo             |
| 5     | **Administrador**  | Gestão de usuários, configurações da instituição      |
| 9     | **SuperUsuário**   | Acesso total + "Controle Total" global                |
| 0     | **Controle Total** | Contexto especial para SuperUsuários (gestão global)  |

## Entidades Principais

### 🔹 Usuario (Core)

- **Chave**: `id` (PK)
- **Função**: Autenticação e dados de login
- **Relacionamentos**:
  - 1:1 com `Pessoa` (obrigatório)
  - 1:N com `UsuarioInstituicao` (níveis de acesso)
- **Campos removidos**: ~~`nivelAcessoUsuario`~~ (migrado para UsuarioInstituicao)

### 🔹 UsuarioInstituicao (Níveis de Acesso)

- **Chave**: `id` (PK)
- **Função**: Define níveis de acesso por instituição
- **Relacionamentos**:
  - N:1 com `Usuario`
  - N:1 com `Instituicao`
- **Campos principais**:
  - `nivelAcessoUsuarioInstituicao` (1,2,5,9)
  - `sitAcessoUsuarioInstituicao` (A/B/C)

### 🔹 Pessoa (Perfil)

- **Chave**: `id` (PK)
- **Função**: Dados pessoais e localização
- **Relacionamentos**:
  - 1:1 com `Usuario`
  - N:1 com `Local` (país, estado, cidade)
  - 1:N com `PessoaInstituicao`

### 🔹 Instituicao (Organização)

- **Chave**: `id` (PK)
- **Função**: Entidade organizacional principal
- **Relacionamentos**:
  - 1:N com `SubInstituicao`
  - 1:N com `UsuarioInstituicao`
  - 1:N com `TipoAtividade`

### 🔹 Local (Localização Normalizada)

- **Chave**: `id` (PK)
- **Função**: Hierarquia geográfica normalizada
- **Tipos**: 1=País, 2=Estado, 3=Cidade
- **Relacionamentos**: Autorreferencial (hierárquico)

## Fluxo de Autenticação

```
1. Usuario faz login com username/password
2. Sistema busca todos os UsuarioInstituicao ativos
3. Se SuperUsuário (nível 9): oferece "Controle Total" + instituições
4. Se múltiplas instituições: exibe seleção
5. Se única instituição: login direto
6. Sessão armazena:
   - usuarioLogado
   - instituicaoSelecionada
   - nivelAcessoAtual (da instituição selecionada)
```

## Casos de Uso do Novo Modelo

### Exemplo 1: Professor Universitário

- **Instituição A (Universidade)**: Nível 2 (Autor) - cria atividades acadêmicas
- **Instituição B (Escola)**: Nível 5 (Administrador) - gestão completa
- **Benefício**: Flexibilidade total sem conflitos

### Exemplo 2: SuperUsuário

- **Qualquer Instituição**: Nível 9 (SuperUsuário)
- **Contexto Especial**: Nível 0 (Controle Total) - gestão global
- **Benefício**: Acesso administrativo global preservado

## Vantagens da Nova Arquitetura

1. **🎯 Flexibilidade**: Diferentes níveis por instituição
2. **🔒 Segurança**: Controle granular de acesso
3. **📈 Escalabilidade**: Suporte a múltiplas organizações
4. **🔧 Manutenibilidade**: Lógica clara de permissões
5. **🎨 UX**: Seleção dinâmica de contexto institucional

## Sessão e Estado

### Variáveis de Sessão

```java
// Dados do usuário logado
Usuario usuarioLogado = session.getAttribute("usuarioLogado");

// Instituição ativa no contexto atual
Instituicao instituicaoSelecionada = session.getAttribute("instituicaoSelecionada");

// Nível de acesso na instituição atual
Integer nivelAcessoAtual = session.getAttribute("nivelAcessoAtual");
```

### Determinação de Tipo de Usuário

```java
switch (nivelAcessoAtual) {
    case 1: return "participante";
    case 2: return "autor";
    case 5: return "administrador";
    case 9: return "superusuario";
    case 0: return "controle-total";
}
```

---

## Próximos Passos

- [x] Implementação da migração arquitetural
- [x] Testes de validação dos fluxos
- [x] Correção de links e redirecionamentos
- [ ] **Gerar diagramas ER visuais** (PlantUML, Mermaid)
- [ ] Documentação de APIs atualizadas
- [ ] Guia de migração para dados existentes

---

_Documento gerado em: August 7, 2025_  
_Versão: 2.0 - Pós refatoração de níveis de acesso_
