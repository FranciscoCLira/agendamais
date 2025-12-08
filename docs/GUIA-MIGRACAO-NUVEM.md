# Guia de Migração para Nuvem - AgendaMais

## 📋 Visão Geral

Este guia detalha como migrar o sistema AgendaMais do ambiente local para um servidor na nuvem, preservando todos os dados dos 690+ usuários e configurações.

**Situação Atual**: Sistema rodando localmente com PostgreSQL Docker  
**Objetivo**: Deploy em produção com URL pública  
**Dados a Migrar**: Usuários, instituições, atividades, inscrições, ocorrências

---

## 🎯 Plataformas Recomendadas

### **Opção 1: Render.com** (Gratuito para começar)
- **Custo**: Gratuito (com limitações)
- **PostgreSQL**: 90 dias grátis, depois precisa migrar
- **Tempo de Setup**: 10-15 minutos
- **Ideal para**: Testes, validação, demonstração

### **Opção 2: Railway.app** ⭐ (Melhor custo-benefício)
- **Custo**: ~$8-12/mês
- **PostgreSQL**: Incluído, permanente
- **Tempo de Setup**: 15-20 minutos
- **Ideal para**: Produção com 690+ usuários

### **Opção 3: DigitalOcean** (Profissional)
- **Custo**: ~$15-25/mês
- **PostgreSQL**: Gerenciado ou DIY
- **Tempo de Setup**: 30-60 minutos
- **Ideal para**: Alta disponibilidade, escalabilidade

---

## 📦 Pré-requisitos

### **Antes de Começar**
- [ ] Sistema funcionando localmente
- [ ] Backup completo do banco de dados
- [ ] Conta GitHub com repositório atualizado
- [ ] Conta na plataforma escolhida (Render/Railway/DO)
- [ ] Configurações SMTP para envio de emails

### **Verificações Necessárias**
```bash
# 1. Confirmar que o código está no GitHub
git status
git push

# 2. Criar backup do banco de dados
cd backup-tools
.\backup-completo.bat

# 3. Verificar tamanho do banco
psql -U postgres -d agendadb_prod -c "SELECT pg_size_pretty(pg_database_size('agendadb_prod'));"
```

---

## 🚀 Migração para Render.com (Gratuito)

### **Passo 1: Preparar Aplicação**

1. Garantir que `Dockerfile` está no root do projeto ✅ (já existe)
2. Verificar `application.properties`:

```properties
# src/main/resources/application.properties
app.url=${APP_URL:http://localhost:8080}
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/agendadb_prod}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

### **Passo 2: Criar Web Service no Render**

1. Acesse [render.com](https://render.com) → Sign Up
2. **New** → **Web Service**
3. Conecte seu repositório GitHub `FranciscoCLira/agendamais`
4. Configurações:
   - **Name**: `agendamais`
   - **Region**: Oregon (US West)
   - **Branch**: `main`
   - **Runtime**: Docker
   - **Instance Type**: Free

### **Passo 3: Criar PostgreSQL Database**

1. No Render: **New** → **PostgreSQL**
2. Configurações:
   - **Name**: `agendamais-db`
   - **Database**: `agendadb_prod`
   - **User**: `agendamais_user`
   - **Region**: Same as Web Service
   - **Plan**: Free

3. **Anote as credenciais**:
   - Internal Database URL
   - External Database URL
   - Username
   - Password

### **Passo 4: Configurar Variáveis de Ambiente**

No Web Service, adicione:

```bash
# Banco de Dados (copiar do PostgreSQL criado)
DATABASE_URL=jdbc:postgresql://dpg-xxxxx.oregon-postgres.render.com/agendadb_prod
DB_USERNAME=agendamais_user
DB_PASSWORD=<senha_gerada>

# Aplicação
APP_URL=https://agendamais.onrender.com
SPRING_PROFILES_ACTIVE=prod-docker

# Email (suas configurações SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=sua-senha-app

# JPA/Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

### **Passo 5: Migrar Dados do Banco**

#### **Opção A: Via linha de comando (Recomendado)**

```bash
# 1. Conectar ao banco remoto (copiar External URL do Render)
set PGPASSWORD=<senha_gerada>
psql -h dpg-xxxxx.oregon-postgres.render.com -U agendamais_user -d agendadb_prod

# 2. Restaurar backup local
psql -h dpg-xxxxx.oregon-postgres.render.com -U agendamais_user -d agendadb_prod < backup-completo.sql
```

#### **Opção B: Via DBeaver (Visual)**

1. Abrir DBeaver
2. Criar conexão com banco Render (External URL)
3. **Tools** → **Dump Database** no banco local
4. **Tools** → **Restore Database** no banco remoto

### **Passo 6: Deploy e Verificação**

1. Render fará deploy automático (5-10 minutos)
2. Acessar: `https://agendamais.onrender.com`
3. Testar login com usuário existente
4. Verificar logs em **Logs** tab

### **Passo 7: Enviar Emails de Boas-Vindas**

```
1. Login no sistema remoto
2. Acessar: Disparo de Emails → Novo Disparo
3. Tipo: BOAS_VINDAS
4. Filtro: Situação = Pendente
5. Assunto: "Bem-vindo ao AgendaMais!"
6. Processar disparo
```

Os 690 usuários receberão email com link:  
`https://agendamais.onrender.com/acesso` ✅

---

## 🚄 Migração para Railway.app (Produção)

### **Passo 1: Setup Inicial**

1. Acesse [railway.app](https://railway.app)
2. **New Project** → **Deploy from GitHub repo**
3. Conectar `FranciscoCLira/agendamais`
4. Railway detecta automaticamente: Dockerfile ✅

### **Passo 2: Adicionar PostgreSQL**

1. No projeto: **New** → **Database** → **Add PostgreSQL**
2. Railway cria automaticamente e injeta variáveis:
   - `DATABASE_URL`
   - `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`

### **Passo 3: Configurar Variáveis**

Railway auto-detecta algumas, adicione as faltantes:

```bash
# Aplicação
APP_URL=${{ RAILWAY_PUBLIC_DOMAIN }}
SPRING_PROFILES_ACTIVE=prod-docker

# Converter DATABASE_URL para formato JDBC
# Railway: postgresql://user:pass@host:port/db
# Spring Boot precisa: jdbc:postgresql://host:port/db
DATABASE_URL=jdbc:postgresql://${{ PGHOST }}:${{ PGPORT }}/${{ PGDATABASE }}
DB_USERNAME=${{ PGUSER }}
DB_PASSWORD=${{ PGPASSWORD }}

# Email
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=sua-senha-app
```

### **Passo 4: Migrar Dados**

```bash
# Railway CLI (mais fácil)
railway login
railway link
railway run psql < backup-completo.sql
```

Ou via connection string:
```bash
psql "postgresql://user:pass@containers-us-west-xxx.railway.app:7432/railway" < backup-completo.sql
```

### **Passo 5: Deploy**

1. Railway faz deploy automático
2. URL pública gerada: `https://agendamais-production.up.railway.app`
3. Mapear domínio customizado (opcional):
   - **Settings** → **Domains** → Add Custom Domain
   - Configurar DNS: `CNAME` apontando para Railway

---

## 💻 Migração para DigitalOcean (Droplet)

### **Opção 1: App Platform (PaaS - Mais Fácil)**

Similar ao Railway, mas com mais controle:

1. **Create** → **Apps** → **GitHub**
2. Selecionar repositório
3. Adicionar **PostgreSQL Database**
4. Configurar Environment Variables
5. Deploy automático

**Custo**: App + DB = ~$17/mês

### **Opção 2: Droplet + Docker (Controle Total)**

#### **Setup do Servidor**

```bash
# 1. Criar Droplet Ubuntu 22.04 ($6/mês)
# Via painel DigitalOcean

# 2. Conectar via SSH
ssh root@<ip-do-droplet>

# 3. Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 4. Instalar Docker Compose
apt install docker-compose

# 5. Configurar firewall
ufw allow 22
ufw allow 80
ufw allow 443
ufw allow 5432
ufw enable
```

#### **Deploy da Aplicação**

```bash
# 1. Clonar repositório
git clone https://github.com/FranciscoCLira/agendamais.git
cd agendamais

# 2. Criar .env
cat > .env << EOF
APP_URL=http://<ip-do-droplet>:8080
POSTGRES_DB=agendadb_prod
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<senha-forte>
EOF

# 3. Subir containers
docker-compose -f docker-compose.yml up -d

# 4. Restaurar backup
docker exec -i agendamais_postgres psql -U postgres agendadb_prod < backup-completo.sql

# 5. Verificar logs
docker logs -f agendamais_app
```

#### **Configurar Nginx + SSL (Opcional)**

```bash
# 1. Instalar Nginx
apt install nginx certbot python3-certbot-nginx

# 2. Configurar proxy reverso
cat > /etc/nginx/sites-available/agendamais << EOF
server {
    listen 80;
    server_name seu-dominio.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF

ln -s /etc/nginx/sites-available/agendamais /etc/nginx/sites-enabled/
nginx -t
systemctl restart nginx

# 3. Obter SSL gratuito
certbot --nginx -d seu-dominio.com

# Renovação automática já configurada!
```

---

## 🔄 Processo de Migração de Dados

### **Backup Completo (Local)**

```bash
# Windows
cd C:\DEV-IA2\agendamais\backup-tools
.\backup-completo.bat

# Será criado: db-backups\backup_completo_YYYYMMDD_HHMMSS.sql
```

### **Restauração no Servidor Remoto**

#### **Método 1: Direto via psql**
```bash
# Formato da URL: postgresql://user:password@host:port/database
psql "<url-conexao>" < backup_completo_20251208.sql
```

#### **Método 2: Via pgAdmin**
1. Conectar ao servidor remoto
2. Botão direito no database → **Restore**
3. Selecionar arquivo `.sql`
4. Execute

#### **Método 3: Via DBeaver**
1. Criar conexão remota
2. **Tools** → **Execute SQL Script**
3. Selecionar backup
4. Run

### **Validação Pós-Migração**

```sql
-- Verificar contagem de registros
SELECT 
    'usuarios' as tabela, COUNT(*) as total FROM usuario
UNION ALL
SELECT 'pessoas', COUNT(*) FROM pessoa
UNION ALL
SELECT 'instituicoes', COUNT(*) FROM instituicao
UNION ALL
SELECT 'inscricoes', COUNT(*) FROM inscricao
UNION ALL
SELECT 'atividades', COUNT(*) FROM atividade;

-- Verificar usuários pendentes
SELECT COUNT(*) FROM usuario WHERE situacao_usuario = 'P';

-- Verificar integridade de emails
SELECT COUNT(*) FROM pessoa WHERE email_pessoa IS NULL OR email_pessoa = '';
```

---

## ⚙️ Configurações Importantes

### **Variáveis de Ambiente Essenciais**

```bash
# Aplicação
APP_URL=https://seu-dominio.com
SPRING_PROFILES_ACTIVE=prod-docker

# Banco de Dados
DATABASE_URL=jdbc:postgresql://host:port/database
DB_USERNAME=usuario
DB_PASSWORD=senha

# Hibernate (IMPORTANTE!)
SPRING_JPA_HIBERNATE_DDL_AUTO=validate  # OU update na primeira vez
SPRING_JPA_SHOW_SQL=false

# Email SMTP
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=senha-de-app-google
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Segurança
CRYPTO_SECRET_KEY=<chave-32-caracteres-base64>
```

### **application-prod.properties** (Opcional)

Criar `src/main/resources/application-prod.properties`:

```properties
# Server
server.port=${PORT:8080}
server.compression.enabled=true

# Database Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Logging
logging.level.root=INFO
logging.level.com.agendademais=INFO
logging.file.name=/app/logs/app.log

# Email
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

---

## 📧 Envio de Boas-Vindas Após Migração

### **Checklist Pré-Envio**

- [ ] Sistema acessível via URL pública
- [ ] Login funcionando
- [ ] SMTP configurado e testado
- [ ] Backup recente do banco
- [ ] Teste com 2-3 emails primeiro

### **Processo de Disparo**

```
1. Login como administrador
2. Menu: Disparo de Emails → Novo Disparo
3. Configurar:
   - Tipo: BOAS_VINDAS
   - Assunto: "Bem-vindo ao Sistema AgendaMais!"
   - Filtro Situação: Pendente (P)
   - Filtro Data: (deixar vazio para pegar todos)
4. Visualizar destinatários: ~690 usuários
5. Processar Disparo
6. Acompanhar em "Detalhes do Disparo"
```

### **Template de Email**

O template `boas-vindas.html` já está configurado:
- Link automático: `{{appUrl}}/acesso`
- Credenciais: `{{email}}`
- Senha padrão: `Agenda@2025`

### **Monitoramento**

```sql
-- Acompanhar processamento
SELECT status, total_destinatarios, emails_enviados, emails_falhados
FROM disparo_email_batch
WHERE id = <id-do-disparo>;

-- Ver logs de envio
SELECT * FROM log_postagem
WHERE data_hora_postagem >= NOW() - INTERVAL '1 hour'
ORDER BY data_hora_postagem DESC;
```

---

## 🔧 Troubleshooting

### **Problema: App não inicia**

```bash
# Verificar logs
# Render: Dashboard → Logs
# Railway: railway logs
# DigitalOcean: docker logs agendamais_app

# Comum: Erro de conexão com banco
# Solução: Verificar DATABASE_URL e credenciais
```

### **Problema: Emails não enviam**

```bash
# Testar SMTP manualmente
telnet smtp.gmail.com 587

# Verificar senha de app do Gmail
# https://myaccount.google.com/apppasswords

# Verificar variáveis SMTP no servidor
echo $SPRING_MAIL_USERNAME
echo $SPRING_MAIL_PASSWORD
```

### **Problema: App lento (Render Free)**

```
Causa: App "dorme" após 15min inatividade
Solução:
1. Upgrade para plano pago ($7/mês)
2. Ou migrar para Railway
3. Ou usar UptimeRobot para ping periódico
```

### **Problema: Banco de dados cheio**

```sql
-- Verificar tamanho
SELECT pg_size_pretty(pg_database_size('agendadb_prod'));

-- Limpar logs antigos
DELETE FROM log_postagem WHERE data_hora_postagem < NOW() - INTERVAL '90 days';

-- Vacuum
VACUUM FULL;
```

---

## 📊 Monitoramento em Produção

### **Métricas Importantes**

```sql
-- Dashboard SQL
-- Usuários por status
SELECT situacao_usuario, COUNT(*) FROM usuario GROUP BY situacao_usuario;

-- Inscrições ativas
SELECT COUNT(*) FROM inscricao WHERE situacao_inscricao = 'A';

-- Emails enviados (último mês)
SELECT COUNT(*) FROM log_postagem 
WHERE data_hora_postagem >= NOW() - INTERVAL '30 days';

-- Performance do banco
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### **Ferramentas de Monitoramento**

- **Render**: Built-in metrics e logs
- **Railway**: Metrics tab
- **DigitalOcean**: Monitoring + Alertas
- **Externo**: UptimeRobot (gratuito) para uptime

---

## 💰 Comparação de Custos (690 usuários)

| Plataforma | Custo Mensal | PostgreSQL | SSL | Backup | Uptime |
|------------|--------------|------------|-----|---------|--------|
| **Render Free** | $0 | 90 dias | ✅ | Manual | 99% |
| **Railway** | $8-12 | ✅ Incluído | ✅ | ✅ Auto | 99.9% |
| **DigitalOcean App** | $12 | +$7 | ✅ | ✅ | 99.99% |
| **DigitalOcean Droplet** | $6 | DIY | DIY | DIY | 99.99% |
| **Heroku** | $7 | +$9 | ✅ | ✅ | 99.95% |

**Recomendação**: Railway.app ($10/mês) - Melhor custo-benefício

---

## 📝 Checklist Completo de Migração

### **Pré-Migração**
- [ ] Backup completo do banco local
- [ ] Código atualizado no GitHub
- [ ] Dockerfile testado localmente
- [ ] Variáveis de ambiente documentadas
- [ ] SMTP configurado e testado

### **Durante Migração**
- [ ] Criar conta na plataforma escolhida
- [ ] Configurar Web Service / App
- [ ] Criar banco de dados PostgreSQL
- [ ] Configurar variáveis de ambiente
- [ ] Deploy da aplicação
- [ ] Restaurar backup no banco remoto
- [ ] Testar login com usuário existente

### **Pós-Migração**
- [ ] Validar contagem de registros
- [ ] Testar funcionalidades principais
- [ ] Configurar domínio customizado (opcional)
- [ ] Configurar SSL (se manual)
- [ ] Criar disparo de boas-vindas
- [ ] Enviar para 2-3 usuários teste
- [ ] Enviar para todos os 690 usuários
- [ ] Monitorar logs de email
- [ ] Documentar URL de produção
- [ ] Atualizar README com instruções

---

## 🎓 Próximos Passos Após Migração

1. **Monitorar primeira semana**: Usuários fazendo login, ativando contas
2. **Suporte aos usuários**: Problemas com senha, acesso
3. **Backup automatizado**: Agendar backups diários
4. **Domínio customizado**: `agendamais.seudominio.com.br`
5. **Melhorias**: Analytics, monitoramento avançado

---

## 📞 Suporte e Recursos

### **Documentação Oficial**
- Render: https://render.com/docs
- Railway: https://docs.railway.app
- DigitalOcean: https://docs.digitalocean.com

### **Comunidades**
- Railway Discord: https://discord.gg/railway
- DigitalOcean Community: https://www.digitalocean.com/community

### **Ferramentas Úteis**
- DBeaver: https://dbeaver.io (GUI para PostgreSQL)
- Postman: Testar APIs
- UptimeRobot: https://uptimerobot.com (monitorar uptime)

---

## ✅ Resumo Rápido

```bash
# PASSO A PASSO MÍNIMO (Railway)

1. railway.app → New Project → GitHub repo
2. Add PostgreSQL database
3. Configurar variáveis (APP_URL, EMAIL, etc)
4. Deploy automático
5. railway run psql < backup-completo.sql
6. Testar: https://seu-app.up.railway.app
7. Enviar boas-vindas via sistema
8. Monitorar Dashboard
```

**Tempo estimado**: 20-30 minutos  
**Custo**: ~$10/mês  
**Resultado**: Sistema online para 690+ usuários ✅

---

**Última atualização**: 08/12/2025  
**Versão**: 1.0  
**Autor**: GitHub Copilot com Francisco Lira
