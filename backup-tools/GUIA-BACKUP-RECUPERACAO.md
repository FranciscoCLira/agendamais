# 🛡️ Guia Completo de Backup e Recuperação - AgendaMais

## 📋 Índice

1. [Entendendo a Persistência dos Dados](#persistência)
2. [Estratégia de Backup](#estratégia)
3. [Configurar Backup Automático](#backup-automático)
4. [Como Restaurar](#restauração)
5. [Checklist de Segurança](#checklist)

---

## 🗄️ Entendendo a Persistência dos Dados {#persistência}

### ✅ Seus dados ESTÃO SEGUROS em:

- ✅ Reiniciar o computador
- ✅ Parar o Docker (`docker compose stop`)
- ✅ Desligar as aplicações Java
- ✅ Modo Suspensão do Windows

**Por quê?**  
O Docker usa volumes persistentes. Os dados ficam em:

```
C:\ProgramData\Docker\volumes\agendamais_db-data\_data\
```

### ❌ Seus dados SÃO PERDIDOS em:

- ❌ `docker compose down -v` (o `-v` apaga volumes!)
- ❌ Desinstalar Docker Desktop sem backup
- ❌ Corrupção do volume Docker (muito raro)
- ❌ Erro humano no SQL (DROP TABLE, DELETE sem WHERE)

---

## 📊 Estratégia de Backup Recomendada {#estratégia}

### **Backup Diário Automático** ⭐ (Recomendado)

- **Quando**: Todo dia às 23:00
- **O que**: Ambos os bancos (dev e prod)
- **Retenção**: 30 dias
- **Localização**: `backup-tools/db-backups/postgres/auto-diario/YYYY-MM-DD/`

### **Backup Manual Antes de Mudanças Grandes**

- Antes de migrations complexas
- Antes de importar dados em massa
- Antes de atualizar versões do PostgreSQL

### **Backup Semanal em Local Externo**

- Copiar para OneDrive/Dropbox/pendrive
- Uma vez por semana (domingos)

---

## ⚙️ Configurar Backup Automático {#backup-automático}

### Passo 1: Testar o Script Manualmente

1. Abra PowerShell como **Administrador**
2. Execute:
   ```powershell
   cd C:\DEV-IA2\agendamais\backup-tools
   .\backup-auto-diario.bat
   ```
3. Verifique se os backups foram criados em:
   ```
   db-backups\postgres\auto-diario\[DATA]\
   ```

### Passo 2: Agendar no Windows (Task Scheduler)

1. **Abrir Agendador de Tarefas:**

   - Pressione `Win + R`
   - Digite: `taskschd.msc`
   - Enter

2. **Criar Tarefa Básica:**

   - Clique em "Criar Tarefa Básica..." (lado direito)
   - Nome: `AgendaMais Backup Diário`
   - Descrição: `Backup automático dos bancos PostgreSQL`
   - Clique em "Avançar"

3. **Gatilho (Quando executar):**

   - Selecione: `Diariamente`
   - Clique em "Avançar"
   - Hora: `23:00:00`
   - Clique em "Avançar"

4. **Ação:**

   - Selecione: `Iniciar um programa`
   - Clique em "Avançar"
   - **Programa/script:**
     ```
     C:\DEV-IA2\agendamais\backup-tools\backup-auto-diario.bat
     ```
   - **Iniciar em (opcional):**
     ```
     C:\DEV-IA2\agendamais\backup-tools
     ```
   - Clique em "Avançar"

5. **Finalizar:**

   - Marque: ☑ "Abrir a caixa de diálogo Propriedades..."
   - Clique em "Concluir"

6. **Configurações Avançadas (na janela de Propriedades):**

   - Aba "Geral":

     - ☑ Executar estando o usuário conectado ou não
     - ☑ Executar com privilégios mais altos

   - Aba "Condições":

     - ☐ Desmarque "Iniciar a tarefa apenas se o computador estiver conectado à energia CA"
     - (Para que execute mesmo no notebook com bateria)

   - Aba "Configurações":

     - ☑ Permitir que a tarefa seja executada sob demanda
     - ☑ Executar tarefa assim que possível após uma inicialização agendada ter sido perdida
     - **⚠️ IMPORTANTE:** Esta opção faz com que o backup execute automaticamente quando você acordar o PC, caso ele tenha perdido o horário das 23:00 por estar suspenso/hibernando

   - Clique em "OK"

7. **Testar a Tarefa:**
   - Encontre a tarefa criada na lista
   - Clique com botão direito → "Executar"
   - Verifique se os backups foram criados

---

## 🔄 Como Restaurar um Backup {#restauração}

### Opção 1: Restaurar com Script (Recomendado)

Use o script `restore-postgres-prod.bat` (já existe):

```powershell
cd C:\DEV-IA2\agendamais\backup-tools
.\restore-postgres-prod.bat
```

Quando solicitado, informe o caminho do arquivo `.sql`

### Opção 2: Restaurar Manualmente

**Para DEV:**

```bash
# 1. Parar a aplicação Dev
docker exec agendamais-db psql -U agenda -d postgres -c "DROP DATABASE agendadb_dev;"
docker exec agendamais-db psql -U agenda -d postgres -c "CREATE DATABASE agendadb_dev OWNER agenda;"
docker exec -i agendamais-db psql -U agenda agendadb_dev < backup-tools\db-backups\postgres\auto-diario\[DATA]\dev-[DATA]-[HORA].sql
```

**Para PROD:**

```bash
# 1. Parar a aplicação Prod
docker exec agendamais-db psql -U agenda -d postgres -c "DROP DATABASE agendadb_prod;"
docker exec agendamais-db psql -U agenda -d postgres -c "CREATE DATABASE agendadb_prod OWNER agenda;"
docker exec -i agendamais-db psql -U agenda agendadb_prod < backup-tools\db-backups\postgres\auto-diario\[DATA]\prod-[DATA]-[HORA].sql
```

---

## ✅ Checklist de Segurança {#checklist}

### Diário (Automático)

- [ ] Backup automático às 23:00 configurado no Task Scheduler
- [ ] Verificar pasta de backups periodicamente
- [ ] Confirmar que backups estão sendo criados

### Semanal

- [ ] Copiar backup mais recente para local externo (OneDrive/Pendrive)
- [ ] Testar restauração em ambiente DEV (uma vez por mês)

### Antes de Ações Críticas

- [ ] Fazer backup manual antes de migrations complexas
- [ ] Fazer backup manual antes de importar dados massivos
- [ ] Fazer backup manual antes de atualizar PostgreSQL

### Mensal

- [ ] Testar processo completo de restauração
- [ ] Limpar backups muito antigos (>90 dias) se necessário
- [ ] Revisar espaço em disco usado pelos backups

---

## 🚨 Em Caso de Emergência

### Se perdeu os dados:

1. **Não entre em pânico!**
2. Pare as aplicações imediatamente
3. Identifique o backup mais recente
4. Siga o processo de restauração acima
5. Verifique os dados restaurados antes de retomar operações

### Se o backup automático falhar:

1. Verifique se o Docker está rodando
2. Verifique se o PostgreSQL está acessível
3. Execute o backup manual:
   ```powershell
   cd C:\DEV-IA2\agendamais\backup-tools
   .\backup-postgres-prod.bat
   ```

---

## 📁 Estrutura de Backups

```
backup-tools/
├── db-backups/
│   └── postgres/
│       ├── auto-diario/           ← Backups automáticos diários
│       │   ├── 2025-11-26/
│       │   │   ├── dev-2025-11-26-23-00.sql
│       │   │   └── prod-2025-11-26-23-00.sql
│       │   ├── 2025-11-27/
│       │   └── ...
│       │
│       ├── backup-dev-inicial-2025-11-26.sql   ← Backup inicial (4 usuários)
│       └── backup-prod-inicial-2025-11-26.sql  ← Backup inicial (4 usuários)
│
├── backup-auto-diario.bat        ← Script de backup automático
├── backup-postgres-prod.bat      ← Script de backup manual
└── restore-postgres-prod.bat     ← Script de restauração
```

---

## 🔐 Boas Práticas

1. **NUNCA** use `docker compose down -v` no ambiente de produção
2. **SEMPRE** use `docker compose stop` ou `docker compose down` (sem `-v`)
3. **Teste** a restauração regularmente em DEV
4. **Mantenha** backups em local externo (nuvem ou físico)
5. **Documente** qualquer mudança na estrutura do banco
6. **Monitore** o espaço em disco dos backups

---

## 📞 Comandos Úteis

### Ver tamanho dos bancos:

```bash
docker exec agendamais-db psql -U agenda -d postgres -c "SELECT pg_database.datname, pg_size_pretty(pg_database_size(pg_database.datname)) FROM pg_database ORDER BY pg_database_size(pg_database.datname) DESC;"
```

### Verificar último backup:

```powershell
dir backup-tools\db-backups\postgres\auto-diario\ -Recurse | Sort-Object LastWriteTime -Descending | Select-Object -First 5
```

### Listar conexões ativas:

```bash
docker exec agendamais-db psql -U agenda -d postgres -c "SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;"
```

---

**✅ Com essa estratégia, seus dados estão protegidos!**
