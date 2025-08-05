# Mensagens de Commit Sugeridas

## Para esta implementação:

```bash
git add .
git commit -m "feat: implementa sistema unificado meus-dados e melhora estrutura de usuários

- Melhorias no cadastro-usuario.html:
  * Adiciona identificação específica de erros com ícones
  * Implementa entrada dual (com/sem login) com cabeçalho condicional
  * Melhora navegação contextual baseada em sessão

- Implementa controller unificado MeusDadosController:
  * Endpoint /meus-dados funciona para todos os níveis
  * Mantém compatibilidade com /participante/meus-dados
  * Template adaptativo baseado no tipo de usuário
  * Redirecionamento inteligente pós-salvamento

- Cria template shared/meus-dados.html:
  * Interface unificada com badges de nível
  * Campos condicionais por tipo de usuário
  * JavaScript aprimorado para gerenciar locais
  * Design responsivo e acessível

- Atualiza menus para todos os níveis:
  * Adiciona link /meus-dados em todos os menus
  * Expande menu administrador com entidades específicas:
    - Instituição, SubInstituição, Atividade
    - TipoAtividade, OcorrenciaAtividade, LogPostagem
  * Melhora organização visual com ícones

- Documenta estrutura de níveis de usuário:
  * Cria docs/estrutura-niveis-usuario.md
  * Define hierarquia e herança de funcionalidades
  * Documenta regras de acesso e endpoints
  * Atualiza README.md com links de documentação

- Cria estrutura de diretórios organizacional:
  * templates/autor/, templates/administrador/
  * templates/super-usuario/, templates/controle-total/
  * templates/shared/ para componentes reutilizáveis

Mantém compatibilidade total com sistema existente"
```

## Para próximos commits (sugestões):

### Implementação dos Controllers Específicos:

```bash
git commit -m "feat: implementa controllers específicos por nível de usuário

- AdministradorController: gestão de instituições e atividades
- SuperUsuarioController: funcionalidades de supervisão
- Endpoints específicos para cada entidade do administrador"
```

### Templates Específicos:

```bash
git commit -m "feat: cria templates específicos para administrador

- Gestão de instituições e sub-instituições
- Interface para atividades e tipos de atividade
- Dashboard de ocorrências e logs
- Reutilização de componentes shared"
```

### Testes e Validações:

```bash
git commit -m "test: adiciona testes para sistema multi-nível

- Testes unitários para MeusDadosController
- Testes de integração entre níveis
- Validação de endpoints por tipo de usuário"
```
