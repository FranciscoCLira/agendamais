# 📧 Próxima Implementação: Disparo Genérico de Emails

**Data de Planejamento**: 25 de novembro de 2025  
**Status**: Planejamento  
**Prioridade**: Alta

---

## 🎯 Objetivo Principal

Criar funcionalidade genérica de disparo de emails com filtros personalizáveis, iniciando com **email de boas-vindas para novos usuários**.

---

## 📋 Requisitos Iniciais

### Caso de Uso 1: Email de Boas-Vindas
**Destinatários**: Usuários com `situacaoUsuario = 'P'` (Pendente - primeira senha)

**Conteúdo do Email:**
- Assunto: "Bem-vindo ao Agenda Mais!"
- Mensagem personalizada
- **Username** (código de usuário)
- **Senha padrão**: `Agenda@2025`
- Link para primeiro acesso: `/alterar-senha`
- Instruções para troca obrigatória de senha

**Filtros:**
- Data de inclusão (a partir de...)
- Instituição
- SubInstituição (opcional)
- Situação do usuário

---

## 🔍 Análise do Código Existente

### DisparoEmailService.java
**Funcionalidades Atuais:**
- ✅ Envio de emails com SMTP institucional ou global
- ✅ Criptografia de credenciais SMTP
- ✅ Progresso de disparo (ProgressoDisparo)
- ✅ Log de envios (LogPostagem)
- ✅ Thread assíncrona para envio
- ✅ Mensagem de rodapé personalizada

**Características:**
```java
- Vinculado a OcorrenciaAtividade (específico)
- Destinatários: inscricaoTipoAtividadeRepository
- Conteúdo: detalheDivulgacao da ocorrência
- Assunto: assuntoDivulgacao + data/hora
```

**Pontos Reutilizáveis:**
1. ✅ Sistema de progresso (ProgressoDisparo)
2. ✅ Thread assíncrona
3. ✅ LogPostagem para auditoria
4. ✅ SMTP institucional
5. ✅ Tratamento de erros

**Pontos Específicos (não reutilizar):**
- ❌ Dependência de OcorrenciaAtividade
- ❌ Filtro por InscricaoTipoAtividade
- ❌ Mensagem de "descadastro" específica

---

## 🏗️ Arquitetura Proposta

### Opção 1: Novo Service Genérico ⭐ (Recomendado)
```
DisparoEmailGenericoService
├── enviarEmailBoasVindas(filtros)
├── enviarEmailPersonalizado(filtros, template)
├── listarDestinatarios(filtros)
└── getProgresso(batchId)
```

**Vantagens:**
- ✅ Desacoplado de OcorrenciaAtividade
- ✅ Reutilizável para outros casos
- ✅ Mantém DisparoEmailService intacto
- ✅ Fácil evolução

### Opção 2: Estender DisparoEmailService
```
DisparoEmailService
├── iniciarDisparo(ocorrenciaId) // Existente
└── iniciarDisparoGenerico(filtros, template) // Novo
```

**Vantagens:**
- ✅ Reutiliza código existente
- ❌ Mistura responsabilidades
- ❌ Mais complexo de manter

---

## 📐 Modelo de Dados

### Nova Tabela: DispararEmailBatch (Sugerido)
```sql
CREATE TABLE disparo_email_batch (
    id BIGSERIAL PRIMARY KEY,
    tipo_disparo VARCHAR(50) NOT NULL,      -- 'BOAS_VINDAS', 'PERSONALIZADO', etc
    instituicao_id BIGINT,
    sub_instituicao_id BIGINT,
    usuario_criador_id BIGINT,
    
    -- Filtros aplicados (JSON ou colunas)
    filtro_situacao_usuario VARCHAR(1),     -- 'P', 'A', etc
    filtro_data_inicio DATE,
    filtro_data_fim DATE,
    
    -- Conteúdo do email
    assunto VARCHAR(255),
    corpo_html TEXT,
    
    -- Controle
    data_criacao TIMESTAMP DEFAULT NOW(),
    data_inicio_envio TIMESTAMP,
    data_fim_envio TIMESTAMP,
    
    -- Estatísticas
    total_destinatarios INT DEFAULT 0,
    emails_enviados INT DEFAULT 0,
    emails_falhados INT DEFAULT 0,
    status VARCHAR(20),                     -- 'PENDENTE', 'ENVIANDO', 'CONCLUIDO', 'ERRO'
    
    FOREIGN KEY (instituicao_id) REFERENCES instituicao(id),
    FOREIGN KEY (sub_instituicao_id) REFERENCES sub_instituicao(id),
    FOREIGN KEY (usuario_criador_id) REFERENCES usuario(id)
);
```

### Tabela Existente: LogPostagem (Reutilizar)
```sql
-- Já existe, vincula envio ao usuário destinatário
log_postagem
├── id
├── ocorrencia_atividade_id  → Tornar NULLABLE para uso genérico
├── pessoa_id
├── email_destinatario
├── situacao ('ENVIADO', 'FALHA')
├── mensagem_erro
└── data_envio
```

---

## 🎨 Interface de Usuário

### Tela: Disparo de Emails
**Localização**: `/administrador/disparo-emails`

**Seções:**

#### 1. Tipo de Disparo
```
[ ] Email de Boas-Vindas (usuários novos)
[ ] Email Personalizado
[ ] Notificação Geral
```

#### 2. Filtros
```
Instituição:       [Dropdown - obrigatório]
SubInstituição:    [Dropdown - opcional]

Tipo de Usuário:
[ ] Todos
[✓] Apenas Pendentes (situacaoUsuario = 'P')
[ ] Apenas Ativos (situacaoUsuario = 'A')

Data de Cadastro:
De: [____] Até: [____]

Visualizar Destinatários (XX usuários encontrados)
```

#### 3. Conteúdo (se personalizado)
```
Assunto: [________________]

Corpo do Email:
[Editor de texto rico]

Variáveis disponíveis:
{{nome}}     - Nome do usuário
{{username}} - Código de usuário
{{senha}}    - Senha padrão (se aplicável)
{{link}}     - Link de primeiro acesso
```

#### 4. Pré-visualização
```
[Visualizar Email] [Testar Envio (enviar para mim)]
```

#### 5. Enviar
```
[Cancelar] [Enviar Emails]

Progresso: [=========>_________] 45%
Enviados: 450 / 1000
Falhas: 5
```

---

## 📝 Template de Email de Boas-Vindas

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #6f42c1; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; background: #f8f9fa; }
        .credentials { background: white; border: 2px solid #6f42c1; padding: 15px; margin: 20px 0; }
        .button { background: #6f42c1; color: white; padding: 12px 24px; text-decoration: none; display: inline-block; border-radius: 5px; }
        .footer { text-align: center; padding: 20px; color: #888; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Bem-vindo ao Agenda Mais!</h1>
        </div>
        
        <div class="content">
            <p>Olá <strong>{{nome}}</strong>,</p>
            
            <p>Seu cadastro foi realizado com sucesso! Agora você pode acessar o sistema e gerenciar suas inscrições em atividades.</p>
            
            <div class="credentials">
                <h3>Seus Dados de Acesso:</h3>
                <p><strong>Usuário:</strong> {{username}}</p>
                <p><strong>Senha Provisória:</strong> {{senha}}</p>
            </div>
            
            <p><strong>⚠️ Importante:</strong> No primeiro acesso, você será solicitado a criar uma nova senha segura.</p>
            
            <p style="text-align: center; margin: 30px 0;">
                <a href="{{link}}" class="button">Acessar o Sistema</a>
            </p>
            
            <p><small>Caso o botão não funcione, copie e cole este link no seu navegador:<br>
            <a href="{{link}}">{{link}}</a></small></p>
        </div>
        
        <div class="footer">
            <p>{{nomeInstituicao}}<br>
            <a href="{{appUrl}}">{{appUrl}}</a></p>
            
            <p><small>Este é um email automático. Por favor, não responda.</small></p>
        </div>
    </div>
</body>
</html>
```

---

## 🔧 Implementação Técnica

### Fase 1: Backend (Core)
- [ ] Criar `DisparoEmailGenericoService`
- [ ] Criar DTO `DisparoEmailRequest`
- [ ] Criar DTO `DisparoEmailFiltros`
- [ ] Implementar `listarDestinatarios(filtros)`
- [ ] Implementar `enviarEmailBoasVindas()`
- [ ] Reutilizar `ProgressoDisparo`
- [ ] Adaptar `LogPostagem` (nullable ocorrenciaId)

### Fase 2: Controller
- [ ] Criar `DisparoEmailGenericoController`
- [ ] Endpoint: `POST /administrador/disparo-email/boas-vindas`
- [ ] Endpoint: `GET /administrador/disparo-email/progresso/{batchId}`
- [ ] Endpoint: `POST /administrador/disparo-email/listar-destinatarios`
- [ ] Validação de acesso (nível >= 5)

### Fase 3: Frontend
- [ ] Criar `disparo-emails.html`
- [ ] Formulário com filtros
- [ ] Preview de destinatários
- [ ] Barra de progresso
- [ ] Histórico de disparos

### Fase 4: Testes
- [ ] Testar filtros
- [ ] Testar template de email
- [ ] Testar envio assíncrono
- [ ] Testar recuperação de erros

---

## 🚀 Roadmap de Evolução

### Versão 1.0 (MVP)
- ✅ Email de boas-vindas
- ✅ Filtro por situação
- ✅ Filtro por data
- ✅ Template fixo

### Versão 2.0
- [ ] Templates personalizáveis
- [ ] Mais filtros (por tipo atividade, local, etc)
- [ ] Agendamento de disparos
- [ ] Relatórios de envio

### Versão 3.0
- [ ] Editor WYSIWYG para templates
- [ ] Biblioteca de templates
- [ ] Teste A/B de emails
- [ ] Estatísticas de abertura/cliques

---

## 📊 Comparação: Reutilizar vs Criar Novo

| Aspecto | Reutilizar DisparoEmailService | Criar Novo Service |
|---------|-------------------------------|-------------------|
| **Velocidade** | ⚡ Mais rápido | 🐢 Mais lento |
| **Manutenibilidade** | ⚠️ Código misturado | ✅ Código limpo |
| **Testabilidade** | ⚠️ Testes complexos | ✅ Testes isolados |
| **Evolução** | ⚠️ Difícil evoluir | ✅ Fácil evoluir |
| **Reuso** | ❌ Acoplado | ✅ Desacoplado |

**Recomendação**: ✅ **Criar novo service genérico**

---

## 💡 Decisões Pendentes

1. **Nome da funcionalidade no menu**:
   - "Disparo de Emails"
   - "Comunicação com Usuários"
   - "Notificações por Email"

2. **Permissão de acesso**:
   - Apenas nível 5 (Administrador)?
   - Ou nível 4 também?

3. **Log de auditoria**:
   - Reutilizar LogPostagem?
   - Criar nova tabela DispararEmailLog?

4. **Limite de envio**:
   - Máximo de destinatários por disparo?
   - Throttling (emails por minuto)?

---

## 📚 Referências de Código

```java
// Reutilizar conceitos de:
DisparoEmailService.java       // Envio, progresso, logs
InscricaoMassivaService.java   // Batch processing
UsuarioRepository.java         // Queries de filtro
```

---

**Próximos Passos:**
1. Confirmar requisitos e design da UI
2. Decidir: novo service ou estender existente
3. Criar branch: `feature/disparo-email-generico`
4. Implementar backend (core)
5. Implementar frontend
6. Testar em DEV
7. Deploy em PROD

---

**Última atualização**: 25 de novembro de 2025  
**Por**: GitHub Copilot + Usuário
