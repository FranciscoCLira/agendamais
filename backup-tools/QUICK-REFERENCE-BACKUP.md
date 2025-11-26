# ⚡ Quick Reference - Backup e Recuperação

## 🟢 Seus dados ESTÃO SEGUROS quando:

✅ Reiniciar o computador  
✅ Suspender o Windows  
✅ Parar o Docker (`docker compose stop`)  
✅ Fechar as aplicações Java

**Por quê?** Docker usa volumes persistentes em `C:\ProgramData\Docker\volumes\`

---

## 🔴 Seus dados SÃO PERDIDOS quando:

❌ `docker compose down -v` (o `-v` apaga tudo!)  
❌ Desinstalar Docker sem backup  
❌ Erro SQL grave (DROP TABLE, DELETE sem WHERE)

---

## 📅 Backup Automático Configurado

✅ **Quando:** Todos os dias às 23:00  
✅ **O que:** Dev + Prod  
✅ **Onde:** `backup-tools/db-backups/postgres/auto-diario/[DATA]/`  
✅ **Retenção:** 30 dias

### Como Configurar:

1. Abrir Agendador de Tarefas do Windows (`Win + R` → `taskschd.msc`)
2. Criar Tarefa Básica
3. Apontar para: `C:\DEV-IA2\agendamais\backup-tools\backup-auto-diario.bat`
4. Horário: 23:00 diariamente
5. **⚠️ IMPORTANTE:** Marcar "Executar tarefa assim que possível após uma inicialização agendada ter sido perdida"
   - Se o PC estiver suspenso às 23h, o backup roda automaticamente quando você acordar o PC

---

## 🔄 Como Restaurar

### Método Rápido:

```powershell
cd C:\DEV-IA2\agendamais\backup-tools
.\restore-postgres-prod.bat
# Informar caminho do arquivo .sql quando solicitado
```

### Comandos Diretos:

```bash
# Restaurar DEV
docker exec -i agendamais-db psql -U agenda agendadb_dev < [CAMINHO_DO_BACKUP].sql

# Restaurar PROD (cuidado!)
docker exec -i agendamais-db psql -U agenda agendadb_prod < [CAMINHO_DO_BACKUP].sql
```

---

## 🚨 Comandos de Emergência

### Backup Manual Imediato:

```bash
# DEV
docker exec agendamais-db pg_dump -U agenda agendadb_dev > backup-dev-emergencia.sql

# PROD
docker exec agendamais-db pg_dump -U agenda agendadb_prod > backup-prod-emergencia.sql
```

### Ver último backup:

```powershell
dir backup-tools\db-backups\postgres\auto-diario\ -Recurse | Sort LastWriteTime -Desc | Select -First 5
```

---

## ⚠️ REGRA DE OURO

### ❌ NUNCA USE:

```bash
docker compose down -v    # O -v apaga os volumes!
```

### ✅ SEMPRE USE:

```bash
docker compose stop       # Para sem apagar dados
docker compose down       # Para e remove containers (dados ficam)
```

---

## 📞 Contatos Úteis

- **Backups Automáticos:** `backup-auto-diario.bat`
- **Backup Manual:** `backup-postgres-prod.bat`
- **Restauração:** `restore-postgres-prod.bat`
- **Guia Completo:** `GUIA-BACKUP-RECUPERACAO.md`

---

**✅ Seus dados estão protegidos com backup automático diário!**
