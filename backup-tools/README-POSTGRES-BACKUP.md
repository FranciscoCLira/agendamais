# 🐘 Backup e Restore PostgreSQL - Produção

**Banco de Dados**: `agendadb_prod` (PostgreSQL 15)  
**Usuário**: `agenda`  
**Senha**: `agenda`  
**Host**: `localhost:5432`

---

## 📋 Scripts Disponíveis

### Para PostgreSQL (PRODUÇÃO)

- ✅ `backup-postgres-prod.bat` - Backup automático com timestamp
- ✅ `restore-postgres-prod.bat` - Restore interativo
- 📁 Backups salvos em: `db-backups/postgres/`

### Para H2 (Desenvolvimento - Legacy)

- `backup-completo.bat` - Backup H2
- `restore-completo.bat` - Restore H2
- 📁 Backups salvos em: `db-backups/`

---

## 🚀 Como Fazer Backup (PROD)

### Método 1: Script Automático (Recomendado)

```cmd
cd C:\DEV-IA2\agendamais\backup-tools
.\backup-postgres-prod.bat
```

**O que faz:**

- Cria backup completo do `agendadb_prod`
- Salva com timestamp: `backup-prod-2025-11-24-22-30.sql`
- Local: `backup-tools\db-backups\postgres\`

### Método 2: Manual (PowerShell)

```powershell
# Definir senha
$env:PGPASSWORD = "agenda"

# Fazer backup
& "C:\Program Files\PostgreSQL\15\bin\pg_dump.exe" `
  -U agenda `
  -h localhost `
  -p 5432 `
  agendadb_prod > backup-prod-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').sql
```

### Método 3: Com Compressão

```powershell
# Backup comprimido (economiza espaço)
& "C:\Program Files\PostgreSQL\15\bin\pg_dump.exe" `
  -U agenda -h localhost -p 5432 agendadb_prod | `
  gzip > backup-prod-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').sql.gz
```

---

## 🔄 Como Restaurar Backup (PROD)

### ⚠️ **ATENÇÃO: Restore SUBSTITUI todos os dados atuais!**

### Método 1: Script Interativo (Recomendado)

```cmd
cd C:\DEV-IA2\agendamais\backup-tools
.\restore-postgres-prod.bat
```

**O script vai:**

1. Listar backups disponíveis
2. Pedir o nome do arquivo
3. Pedir confirmação
4. Executar restore

### Método 2: Manual (PowerShell)

```powershell
# Parar servidor
taskkill /F /IM java.exe

# Definir senha
$env:PGPASSWORD = "agenda"

# Restaurar
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" `
  -U agenda `
  -h localhost `
  -p 5432 `
  -d agendadb_prod `
  -f "backup-tools\db-backups\postgres\backup-prod-2025-11-24.sql"

# Reiniciar servidor
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Método 3: Restore de Backup Comprimido

```powershell
# Descompactar e restaurar em um comando
gunzip -c backup-prod-2025-11-24.sql.gz | `
  psql -U agenda -h localhost -p 5432 -d agendadb_prod
```

---

## 📁 Estrutura de Diretórios

```
backup-tools/
├── backup-postgres-prod.bat     ← Backup PROD (PostgreSQL)
├── restore-postgres-prod.bat    ← Restore PROD (PostgreSQL)
├── README-POSTGRES-BACKUP.md    ← Este arquivo
│
├── db-backups/
│   ├── postgres/                ← Backups PostgreSQL PROD
│   │   ├── backup-prod-2025-11-24-22-30.sql
│   │   ├── backup-prod-2025-11-24-23-15.sql
│   │   └── ...
│   │
│   └── ...                      ← Backups H2 (legacy)
│
├── backup-completo.bat          ← H2 (dev)
├── restore-completo.bat         ← H2 (dev)
└── README-backup-tools.md       ← Instruções H2
```

---

## 🔧 Configuração Inicial

### Verificar Path do PostgreSQL

Se os scripts não funcionarem, ajuste o path do PostgreSQL:

```batch
REM Editar nos arquivos .bat:
set PGBIN=C:\Program Files\PostgreSQL\15\bin

REM Ou se sua instalação for em outro local:
set PGBIN=C:\PostgreSQL\15\bin
```

### Testar Conexão

```powershell
# Testar se consegue conectar
psql -U agenda -h localhost -p 5432 -d agendadb_prod -c "SELECT version();"
```

---

## 📋 Checklist de Backup Antes de Deploy

- [ ] **Parar servidor**: `taskkill /F /IM java.exe`
- [ ] **Fazer backup**: `.\backup-postgres-prod.bat`
- [ ] **Verificar arquivo gerado**: Deve estar em `db-backups\postgres\`
- [ ] **Verificar tamanho**: Arquivo não deve estar vazio
- [ ] **Opcional**: Copiar backup para local seguro (outro disco, nuvem)
- [ ] **Iniciar servidor**: `.\run-prod.bat`

---

## 🚨 Recuperação de Desastre

### Se algo der errado após deploy:

1. **Parar servidor**:

   ```cmd
   taskkill /F /IM java.exe
   ```

2. **Restaurar último backup**:

   ```cmd
   cd backup-tools
   .\restore-postgres-prod.bat
   ```

3. **Voltar código para versão anterior**:

   ```cmd
   git checkout <commit-anterior>
   mvn clean package -DskipTests
   ```

4. **Reiniciar servidor**:
   ```cmd
   .\run-prod.bat
   ```

---

## 💡 Boas Práticas

### Frequência de Backup

- **Antes de cada deploy**: Sempre!
- **Backup diário**: Automatizar com Task Scheduler
- **Backup semanal**: Copiar para local externo

### Retenção

- Manter últimos 7 backups diários
- Manter últimos 4 backups semanais
- Manter último backup mensal por 1 ano

### Segurança

- Backups devem ser guardados fora do servidor
- Considerar criptografia para dados sensíveis
- Testar restore periodicamente

---

## 📞 Troubleshooting

### Erro: "pg_dump: command not found"

**Solução**: Ajustar path do PostgreSQL no script

### Erro: "password authentication failed"

**Solução**: Verificar senha no script (padrão: `agenda/agenda`)

### Erro: "FATAL: database does not exist"

**Solução**: Verificar nome do banco (padrão: `agendadb_prod`)

### Backup muito lento

**Solução**: PostgreSQL pode estar com muitos dados. Considerar backup em horário de baixo uso.

---

**Última atualização**: 24 de novembro de 2025  
**Versão**: 1.0 - PostgreSQL PROD
