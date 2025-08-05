# Estrutura de Níveis de Usuário - Agenda Mais

## Definições por Nível de Acesso

### 1. Participante (Nível 1)

**Pasta de templates:** `templates/participante/`
**Funcionalidades compartilhadas:**

- Entidades: Pessoa e Inscrição
- Endpoints específicos:
  - `/meus-dados` - Gerenciar dados pessoais
  - `/inscricao` - Inscrições em atividades

### 2. Autor (Nível 2)

**Pasta de templates:** `templates/autor/`
**Funcionalidades:**

- Herda todas as funcionalidades de **Participante**
- Funcionalidades específicas da entidade **Autor**
- Endpoints:
  - `/meus-dados` - Dados pessoais (herda)
  - `/inscricao` - Inscrições (herda)
  - `/autor-form` - Informações específicas de autor

### 5. Administrador (Nível 5)

**Pasta de templates:** `templates/administrador/`
**Funcionalidades:**

- Herda todas as funcionalidades de **Autor**
- Funcionalidades específicas de administrador das entidades da instituição:
  - **Instituição** - Gerenciar dados da instituição
  - **SubInstituição** - Gerenciar sub-instituições
  - **Atividade** - Gerenciar atividades da instituição
  - **TipoAtividade** - Definir tipos de atividades
  - **OcorrenciaAtividade** - Gerenciar ocorrências/sessões de atividades
  - **LogPostagem** - Visualizar logs de postagens/alterações

**Endpoints específicos:**

- `/administrador/instituicoes` - Gestão da instituição
- `/administrador/subinstituicoes` - Gestão de sub-instituições
- `/administrador/atividades` - Gestão de atividades
- `/administrador/tipos-atividade` - Gestão de tipos de atividade
- `/administrador/ocorrencias` - Gestão de ocorrências
- `/administrador/postagens` - Log de postagens
- `/gestao-usuarios-instituicao` - Gestão de usuários da instituição

### 9. Super-Usuário (Nível 9)

**Pasta de templates:** `templates/super-usuario/`
**Funcionalidades:**

- Herda funcionalidades compartilhadas de **Autor** (não administrador)
- Acesso especial a funcionalidades de supervisão
- Endpoints:
  - `/meus-dados` - Dados pessoais
  - Endpoints específicos de supervisão

### 0. Controle Total (Pseudo-nível, armazenado como 9 no BD)

**Pasta de templates:** `templates/controle-total/`
**Funcionalidades:**

- Controle total do sistema
- Funcionalidades de parametrização geral:
  - **Local** - Gestão de países, estados, cidades
  - Configurações globais do sistema

**Endpoints específicos:**

- `/gestao/locais` - Gestão de locais (países, estados, cidades)
- `/gestao/locais/relacao-pessoas` - Relação Local ↔ Pessoas
- `/usuarios/geral` - Gestão geral de usuários
- `/instituicoes` - Gerenciar todas as instituições

## Regras de Acesso

### 1. Acesso à entidade Cadastro:

1.1. **Níveis inferiores a 9:** Podem cadastrar-se e cancelar o próprio cadastro
1.2. **Nível 9 (Super-usuário e Controle Total):** Acesso total a cadastros

### 2. Estrutura de Herança:

- **Participante** → funcionalidades básicas
- **Autor** → herda Participante + funcionalidades de autor
- **Administrador** → herda Autor + gestão da instituição
- **Super-usuário** → funcionalidades de Autor + supervisão
- **Controle Total** → controle total do sistema

### 3. Endpoint Unificado /meus-dados:

- Funciona para todos os níveis de usuário
- Template adaptativo baseado no tipo de usuário
- Campos e funcionalidades condicionais por nível
- Compatibilidade mantida com `/participante/meus-dados`

## Estrutura de Arquivos

```
templates/
├── participante/
│   ├── meus-dados.html (original, mantido para compatibilidade)
│   └── inscricao-form.html
├── autor/
│   └── autor-form.html
├── administrador/
│   ├── instituicoes/
│   ├── subinstituicoes/
│   ├── atividades/
│   ├── tipos-atividade/
│   ├── ocorrencias/
│   └── postagens/
├── super-usuario/
│   └── (funcionalidades específicas)
├── controle-total/
│   └── (funcionalidades de controle total)
├── shared/
│   └── meus-dados.html (template unificado)
└── menus/
    ├── menu-participante.html
    ├── menu-autor.html
    ├── menu-administrador.html
    ├── menu-superusuario.html
    └── menu-controle-total.html
```

## Controllers

### MeusDadosController

- Controller unificado para `/meus-dados`
- Mantém compatibilidade com `/participante/meus-dados`
- Roteamento automático baseado no tipo de usuário
- Template adaptativo

### Específicos por Nível

- `ParticipanteDadosController` - mantido para compatibilidade
- `AdministradorController` - gestão da instituição
- `SuperUsuarioController` - funcionalidades de supervisão
- `ControleToralController` - controle total (LocalAdminController)

## Implementação de Campo Condicional

### Template shared/meus-dados.html:

```html
<!-- CPF visível apenas para alguns níveis -->
<div th:if="${tipoUsuario != 'participante'}">
  <label for="cpfPessoa">CPF:</label>
  <input type="text" id="cpfPessoa" th:field="*{cpfPessoa}" maxlength="14" />
</div>

<!-- Observações visíveis para administradores e superiores -->
<fieldset
  th:if="${tipoUsuario == 'administrador' or tipoUsuario == 'super-usuario' or tipoUsuario == 'controle-total'}"
>
  <legend><i class="fas fa-sticky-note"></i> Observações</legend>
  <textarea th:field="*{observacoes}" rows="3" maxlength="500"></textarea>
</fieldset>
```

## Status de Implementação

✅ **Concluído:**

- Estrutura de diretórios criada
- Controller unificado MeusDadosController
- Template shared/meus-dados.html
- Menus atualizados com /meus-dados
- Compatibilidade mantida com sistema existente

🔄 **Em desenvolvimento:**

- Controllers específicos para administrador
- Templates específicos por nível
- Funcionalidades de gestão da instituição

📋 **Pendente:**

- Implementação completa dos endpoints específicos do administrador
- Testes de integração entre níveis
- Documentação de API para cada nível
