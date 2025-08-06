# Correção do Carregamento de Cidades via AJAX

## Problema Identificado

Ao alterar o estado no formulário "Meus Dados", a lista de cidades não era carregada dinamicamente.

### Causas Raiz

1. **JavaScript Desabilitado**: O arquivo `local-form.js` estava comentado no template
2. **Parâmetros Incorretos na API**:
   - JavaScript enviava `?pais=` mas API esperava `?paisNome=`
   - JavaScript enviava `?estado=` mas API esperava `?estadoNome=`
3. **Event Handlers Duplicados**: HTML tinha `onchange` e JavaScript também adicionava events

## Soluções Implementadas

### 1. Reativação do JavaScript

```html
<!-- ANTES: -->
<!-- <script src="/js/local-form.js"></script> -->
<!-- TEMPORARIAMENTE DESABILITADO PARA TESTE -->

<!-- DEPOIS: -->
<script src="/js/local-form.js"></script>
```

### 2. Correção dos Parâmetros da API

```javascript
// ANTES:
fetch("/api/locais/estados?pais=" + encodeURIComponent(pais));
fetch("/api/locais/cidades?estado=" + encodeURIComponent(estado));

// DEPOIS:
fetch("/api/locais/estados?paisNome=" + encodeURIComponent(pais));
fetch("/api/locais/cidades?estadoNome=" + encodeURIComponent(estado));
```

### 3. Remoção de Event Handlers Duplicados

```html
<!-- ANTES: -->
<select id="paisSelect" onchange="paisChange(event)">
  <!-- DEPOIS: -->
  <select id="paisSelect"></select>
</select>
```

### 4. Melhoria do Debug

- Adicionados logs detalhados para identificar problemas
- Criada página de teste (`/teste-ajax`) para debug das APIs
- Verificação se o `local-form.js` foi carregado corretamente

## Validação das APIs

Testado e confirmando funcionamento:

```bash
# Estados do Brasil
curl "http://localhost:8080/api/locais/estados?paisNome=Brasil"
# Retorna: [{"id":4,"nomeLocal":"PR"},{"id":5,"nomeLocal":"RJ"},{"id":6,"nomeLocal":"SP"}]

# Cidades de SP
curl "http://localhost:8080/api/locais/cidades?estadoNome=SP"
# Retorna: [{"id":7,"nomeLocal":"São Paulo"},{"id":8,"nomeLocal":"São Caetano do Sul"}...]
```

## Resultado

✅ **Funcionamento Restaurado**:

- Seleção de país carrega estados dinamicamente
- Seleção de estado carrega cidades dinamicamente
- Campos "Outro" funcionam corretamente
- Logs de debug disponíveis para monitoramento

✅ **Compatibilidade Mantida**:

- Todos os tipos de usuário utilizam a mesma funcionalidade
- Template unificado funciona corretamente
- APIs consistentes com nomenclatura esperada

## Arquivos Modificados

### Templates

- `participante/meus-dados.html` - Reativado JavaScript, removido handlers duplicados
- `teste-ajax.html` - **NOVO** - Página de teste para debug

### JavaScript

- `static/js/local-form.js` - Corrigidos parâmetros das APIs

### Controllers

- `MenuController.java` - Adicionado endpoint de teste

## Benefícios

1. **UX Melhorada**: Seleção dinâmica funciona como esperado
2. **Menos Digitação**: Usuários podem selecionar cidades existentes
3. **Consistência**: Nomenclatura de APIs padronizada
4. **Debuggabilidade**: Logs e página de teste para manutenção futura

---

**Data**: 03/08/2025  
**Status**: ✅ Implementado e Testado
