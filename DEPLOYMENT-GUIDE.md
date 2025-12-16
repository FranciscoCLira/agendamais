# Deployment Guide - AgendaMais PROD

## Release v2025.12.15 - Estabilidade para Produção

✅ **Status**: Aprovado para produção  
🔖 **Tag**: `v2025.12.15`  
📅 **Data**: 15 de dezembro de 2025  
⏱️ **Build Time**: ~32s (Maven)

---

## 🚀 Quick Deploy (Automated)

### Usando o script de automação:

```powershell
# Deploy com confirmação de backup
cd C:\DEV-IA2\agendamais
.\deploy-prod.ps1

# Deploy sem confirmação (use com cuidado)
.\deploy-prod.ps1 -Force

# Deploy com health check customizado
.\deploy-prod.ps1 -HealthCheckUrl "http://localhost:8080/administrador" -HealthCheckTimeout 180
```

**O que o script faz:**

1. ✓ Para processos Java existentes
2. ✓ Constrói JAR (mvn clean package -DskipTests)
3. ✓ Inicia app com profile `prod`
4. ✓ Valida saúde da aplicação (health check)
5. ✓ Registra logs em `app-prod.log`

---

## 🔄 Rollback (se necessário)

### Voltar para versão anterior:

```powershell
# Rollback para a tag anterior
.\rollback-prod.ps1 -Version v2025.12.14

# Rollback para versão específica
.\rollback-prod.ps1 -Version v2025.12.10

# Rollback usando arquivo de backup local
.\rollback-prod.ps1 -BackupJar "C:\backups\agenda-mais-2025-12-14.jar"
```

**O que o script faz:**

1. ✓ Para Java process atual
2. ✓ Restaura JAR da tag especificada
3. ✓ Reconstrói ou copia JAR
4. ✓ Reinicia aplicação

---

## 📋 Manual Deploy (alternativa)

Se preferir executar manualmente:

```powershell
# 1. Parar app
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# 2. Build
cd C:\DEV-IA2\agendamais
mvn clean package -DskipTests

# 3. Start com PROD profile
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar `
  --spring.profiles.active=prod `
  --app.reload-data=false
```

### Ou usando scripts existentes:

```bash
# Usar script padrão
.\run-prod.bat

# Ou com H2 TCP (se aplicável)
.\run-prod-agendadb-prod.bat
```

---

## ✅ Validação pós-deployment

### 1. Health Check

```powershell
# Verificar se app está respondendo
Invoke-WebRequest -Uri "http://localhost:8080/acesso" -UseBasicParsing
```

**Status esperado**: 200 OK (redirecionará para login)

### 2. Logs

```powershell
# Acompanhar logs em tempo real
Get-Content app-prod.log -Wait -Tail 30

# Ou ver últimas 100 linhas
Get-Content app-prod.log -Tail 100
```

**Procure por:**

- ✓ "Started AgendaMaisApplication"
- ✓ "Tomcat started on port 8080"
- ✗ Nenhum erro crítico (ERROR, FATAL)

### 3. Funcionalidade

- Login em: http://localhost:8080
- Admin em: http://localhost:8080/administrador/instituicao/editar
- Modo de envio de email deve persistir (Online ↔ Offline)

---

## 🔍 Troubleshooting

### Porta 8080 já em uso

```powershell
# Encontrar processo
Get-NetTCPConnection -LocalPort 8080 | Get-Process

# Ou mudar porta (temporário para testes)
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar `
  --spring.profiles.active=prod `
  --server.port=9090
```

### App não inicia

1. Verifique logs: `Get-Content app-prod.log -Tail 50`
2. Banco de dados acessível?
   ```powershell
   # Para PostgreSQL
   psql -h localhost -U postgres -d agendadb_prod
   ```
3. Variáveis de ambiente `.env` configuradas?
   ```powershell
   .\scripts\run-with-env.ps1 -PrintVarsOnly -Profile prod
   ```

### Health check timeout

- Aumentar timeout:
  ```powershell
  .\deploy-prod.ps1 -HealthCheckTimeout 300
  ```
- Ou aguardar startup manual:
  ```powershell
  Start-Sleep -Seconds 60
  Invoke-WebRequest -Uri "http://localhost:8080/acesso" -UseBasicParsary
  ```

---

## 📦 Características da release v2025.12.15

### Novos recursos

- **Dynamic Email Mode**: Rodapé de email adaptado por instituição (Online/Offline)
- **Inline Icons**: Ícones em gerenciar-instituicoes agora exibem inline
- **Persistent Settings**: Modo de envio persiste no banco

### Correções

- ✓ Salvamento de modoEnvioEmail no formulário de admin
- ✓ Compatibilidade cross-browser para layout flex

### Ambiente

- **Java**: 17+
- **Spring Boot**: 3.3.0
- **PostgreSQL**: 15.15
- **Backup**: Automático diário configurado

---

## 📊 Monitoramento

### Sugerido: Configure alertas para

- Erro de log ("ERROR", "FATAL")
- Porta 8080 indisponível
- Taxa de erro HTTP > 5%
- Tempo de resposta > 5s

### Exemplo (PowerShell)

```powershell
# Monitorar por 10 minutos
$endTime = (Get-Date).AddMinutes(10)
while ((Get-Date) -lt $endTime) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/acesso" `
          -UseBasicParsing -TimeoutSec 5
        Write-Host "✓ $(Get-Date -Format 'HH:mm:ss') - Status: $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "✗ $(Get-Date -Format 'HH:mm:ss') - ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Seconds 30
}
```

---

## 🔐 Segurança

- ✓ `--app.reload-data=false`: Desativa reload automático de dados (seguro para PROD)
- ✓ `app.security.requireAdmin`: Validar em `application-prod.properties`
- ✓ HTTPS: Configurar reverse proxy (nginx/Apache) na frente

---

## 📞 Suporte

Para mais informações:

- Ver [README.md](README.md)
- Ver [run-prod.bat](run-prod.bat)
- Check logs: `app-prod.log`
- Git tag: `git tag -l v2025.12.15`

---

**Última atualização**: 2025-12-15  
**Versão**: v2025.12.15  
**Status**: ✅ Production Ready
