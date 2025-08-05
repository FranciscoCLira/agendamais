# Atualização dos Menus Super-Usuário e Controle Total

## Definição dos Níveis de Acesso

### Super-Usuário (nível 9)
- **Participante**: Pode se inscrever em tipos de atividades
- **Autor**: Pode gerenciar dados de autor
- **Administrador**: Tem todas as funcionalidades administrativas de instituição
- **Super-Usuário**: Funcionalidades avançadas específicas

### Controle Total (nível 0)
- **Acesso restrito**: Funcionalidades específicas de sistema
- **Administração total**: Controle completo sobre todas as instituições
- **Configurações de sistema**: Backup, logs, migrações
- **Relatórios e auditoria**: Visão completa do sistema

## Menu Super-Usuário Atualizado

### Funcionalidades de Participante/Autor
- (A) Inscrição em Tipos de Atividades → `/inscricao-tipo-atividade`
- (B) Vínculos com Instituições → `/vinculo-instituicao`
- **[ESPAÇO]**
- (C) Meus Dados → `/meus-dados`
- (D) Meus dados de Autor → `/dados-autor`

### Gestão da Instituição (como Administrador)
- **[ESPAÇO]**
- **Gestão da Instituição**
- Cadastro (Usuário/Pessoa) → `/cadastro-usuario?origem=superusuario`
- Gerenciar Instituição → `/administrador/instituicoes`
- Gerenciar Sub-Instituições → `/administrador/subinstituicoes`
- Gerenciar Atividades → `/administrador/atividades`
- Tipos de Atividade → `/administrador/tipos-atividade`
- Ocorrências de Atividades → `/administrador/ocorrencias`
- Postagens → `/administrador/postagens`
- Log de Postagens → `/administrador/log-postagens`
- Gestão de Usuários → `/administrador/usuarios`

### Funcionalidades Avançadas (Específicas)
- **[ESPAÇO]**
- **Funcionalidades Avançadas**
- Gerenciar Locais → `/superusuario/locais`
- Backup/Restore Sistema → `/superusuario/backup`
- Logs do Sistema → `/superusuario/logs`

## Menu Controle Total Atualizado

### Administração do Sistema
- **Administração do Sistema**
- **[ESPAÇO]**
- Gerenciar Todas as Instituições → `/controle-total/instituicoes`
- Gestão Geral de Usuários → `/controle-total/usuarios`
- Gestão de Locais → `/gestao/locais`
- Relação Local ↔ Pessoas → `/gestao/locais/relacao-pessoas`

### Configurações do Sistema
- **[ESPAÇO]**
- **Configurações do Sistema**
- **[ESPAÇO]**
- Backup/Restore Completo → `/controle-total/backup`
- Logs do Sistema → `/controle-total/logs`
- Configurações Avançadas → `/controle-total/configuracoes`
- Migração de Dados → `/controle-total/migracao`
- Modo Manutenção → `/controle-total/manutencao`

### Relatórios e Auditoria
- **[ESPAÇO]**
- **Relatórios e Auditoria**
- **[ESPAÇO]**
- Relatórios Gerais → `/controle-total/relatorios`
- Log de Auditoria → `/controle-total/auditoria`
- Estatísticas do Sistema → `/controle-total/estatisticas`

### Perfil
- **[ESPAÇO]**
- **Perfil**
- **[ESPAÇO]**
- Meus Dados → `/meus-dados`

## Fluxos de Cadastro Atualizados

### Origem do Cadastro
- **Administrador**: `/cadastro-usuario?origem=administrador` → retorna para `/administrador`
- **Super-Usuário**: `/cadastro-usuario?origem=superusuario` → retorna para `/superusuario`
- **Link "Não tem cadastro?"**: `/cadastro-usuario` → retorna para `/acesso`

## Endpoints Implementados

### Super-Usuário
- `/superusuario/locais` → "Em Construção"
- `/superusuario/backup` → "Em Construção"
- `/superusuario/logs` → "Em Construção"

### Controle Total
- `/controle-total/instituicoes` → "Em Construção"
- `/controle-total/usuarios` → "Em Construção"
- `/controle-total/backup` → "Em Construção"
- `/controle-total/logs` → "Em Construção"
- `/controle-total/configuracoes` → "Em Construção"
- `/controle-total/migracao` → "Em Construção"
- `/controle-total/manutencao` → "Em Construção"
- `/controle-total/relatorios` → "Em Construção"
- `/controle-total/auditoria` → "Em Construção"
- `/controle-total/estatisticas` → "Em Construção"

## Arquivos Modificados

### Templates
- `menus/menu-superusuario.html` - Expandido com todas as funcionalidades
- `menus/menu-controle-total.html` - Reorganizado com categorias claras

### Controllers
- `MenuController.java` - Adicionados novos endpoints
- `CadastroRelacionamentoController.java` - Suporte a origem "superusuario"

## Hierarquia de Funcionalidades

```
Participante (nível 1)
├── Inscrição em atividades
├── Vínculos com instituições  
└── Meus dados

Autor (nível 2)
├── [Todas do Participante]
└── Dados de autor

Administrador (nível 5)
├── [Todas do Autor]
└── Gestão da instituição

Super-Usuário (nível 9)
├── [Todas do Administrador]
└── Funcionalidades avançadas de sistema

Controle Total (nível 0)
├── Administração total do sistema
├── Configurações avançadas
├── Relatórios e auditoria
└── Meus dados
```

## Benefícios

1. **Hierarquia Clara**: Cada nível tem acesso às funcionalidades dos níveis anteriores
2. **Separação de Responsabilidades**: Controle Total focado em sistema, Super-Usuário em gestão
3. **Fluxos Corretos**: Cadastros retornam para o menu correto
4. **Organização Visual**: Espaçamentos e categorias facilitam navegação
5. **Escalabilidade**: Estrutura permite fácil adição de novas funcionalidades

---

**Data**: 04/08/2025  
**Status**: ✅ Implementado
