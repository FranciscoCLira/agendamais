# Checklist: Deploy Produção e Carga de Inscrições

**Data prevista**: 24 de novembro de 2025

## ✅ Desenvolvimentos Concluídos

### Features Implementadas
1. ✅ Carga Massiva de Inscrições
   - Upload de arquivo Excel (.xlsx) do Microsoft Forms
   - Validação prévia sem processamento
   - Processamento com criação automática de Pessoa, Usuario, relacionamentos
   - dataAfiliacao=null para PessoaInstituicao e PessoaSubInstituicao

2. ✅ Reversão de Carga
   - Lê coluna G (Email) do arquivo Excel
   - Deleta em ordem reversa: InscricaoTipoAtividade → Inscricao → UsuarioInstituicao → PessoaSubInstituicao → PessoaInstituicao → Usuario → Pessoa
   - Valida relacionamentos antes de deletar
   - Não deleta registros de Local (dados geográficos)

3. ✅ Redirect para Alteração de Senha
   - Usuários com situacaoUsuario='P' são redirecionados para /alterar-senha
   - Implementado em LoginController

4. ✅ Correções de UI
   - Contagem correta de erros na validação (registrosInvalidos = total - validos)
   - Rótulo "Registros Processados" na reversão
   - 3 fixes JavaScript (IDs dropdowns, selectedFile, fileInput)

5. ✅ Estabilidade do Servidor
   - Desabilitado app.reload-data=false no profile dev-docker
   - Evita N+1 queries do LocalDataLoader durante startup
   - Servidor inicia em ~30 segundos

---

## 📋 Checklist de Deploy

### Pré-Deploy
- [ ] Verificar se branch está em sync com origin
- [ ] Fazer merge para branch main (ou branch de produção)
- [ ] Backup completo do banco de dados PRODUÇÃO
  - Usar: `backup-tools/backup-completo.bat`
  - Confirmar backup criado em: `backup-tools/db-backups/`

### Configuração de Produção
- [ ] Verificar `application-prod.properties`:
  - [ ] `app.reload-data=false` (dados geográficos já devem estar carregados)
  - [ ] URL do banco: `jdbc:postgresql://localhost:5432/agendadb_prod`
  - [ ] Credenciais do banco corretas
  - [ ] Configurações de email (SMTP) corretas
  - [ ] `spring.jpa.hibernate.ddl-auto=update` (ou `validate` se preferir)

### Build e Deploy
- [ ] Parar servidor de produção atual (se rodando)
  ```powershell
  taskkill /F /IM java.exe
  ```

- [ ] Limpar e compilar para produção:
  ```powershell
  mvn clean package -DskipTests
  ```

- [ ] Verificar se JAR foi gerado:
  ```powershell
  Test-Path "target\agenda-mais-0.0.1-SNAPSHOT.jar"
  ```

- [ ] Iniciar servidor em PRODUÇÃO:
  ```powershell
  .\run-prod.bat
  # OU manualmente:
  java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
  ```

- [ ] Aguardar startup (~30-60 segundos)

- [ ] Testar acesso: http://localhost:8080

### Validação Pós-Deploy
- [ ] Login funciona corretamente
- [ ] Usuários com situacaoUsuario='P' são redirecionados para /alterar-senha
- [ ] Acessar: `/administrador/inscricao-massiva`
- [ ] Testar Validar Arquivo (sem processar)
  - [ ] Contagem de erros está correta
  - [ ] Mensagens de validação aparecem

### Carga de Inscrições
- [ ] Preparar arquivo Excel do Microsoft Forms
  - Colunas G a O: Email, Nome, Celular, Comentários, Cidade, Estado, País, Data Nascimento, Sexo
  - Linha 1 deve ser cabeçalho (será pulada)

- [ ] Selecionar SubInstituição correta
- [ ] Selecionar Tipo de Atividade correto
- [ ] Upload do arquivo
- [ ] Clicar em "Validar Arquivo"
  - [ ] Verificar Total de Linhas
  - [ ] Verificar Registros Válidos
  - [ ] Verificar Erros (se houver)
  - [ ] Revisar mensagens de erro/aviso

- [ ] Se validação OK, clicar em "Processar Carga"
  - [ ] Aguardar processamento
  - [ ] Verificar:
    - Total
    - Novas (inscrições criadas)
    - Existentes (já cadastradas)
    - Erros

- [ ] Verificar no banco de dados:
  ```sql
  -- Contar novas pessoas
  SELECT COUNT(*) FROM pessoa WHERE email_pessoa IN (...emails do arquivo...);
  
  -- Contar novas inscrições
  SELECT COUNT(*) FROM inscricao_tipo_atividade 
  WHERE tipo_atividade_id = <ID_TIPO_ATIVIDADE>;
  ```

### Teste de Reversão (OPCIONAL - Apenas se necessário)
⚠️ **CUIDADO**: Reversão deleta registros permanentemente!

- [ ] **FAZER BACKUP antes de reverter**
- [ ] Mesmo arquivo usado no processamento
- [ ] Mesma SubInstituição e Tipo de Atividade
- [ ] Clicar em "Excluir/Reverter Carga"
- [ ] Confirmar ação
- [ ] Verificar "Registros Processados"
- [ ] Validar no banco que registros foram removidos

---

## 🔍 Troubleshooting

### Servidor não inicia
- Verificar se porta 8080 está livre:
  ```powershell
  netstat -ano | findstr :8080
  ```
- Matar processo Java travado:
  ```powershell
  taskkill /F /IM java.exe
  ```

### Servidor trava durante startup
- **CAUSA**: LocalDataLoader com app.reload-data=true
- **SOLUÇÃO**: Verificar `application-prod.properties` tem `app.reload-data=false`

### Erros de validação excessivos
- Verificar formato do arquivo Excel
- Confirmar colunas G a O estão corretas
- Verificar estados brasileiros com nome completo (não sigla)
  - ❌ "SP" → ✅ "São Paulo"
  - ❌ "RJ" → ✅ "Rio de Janeiro"

### Banco de dados vazio (dados geográficos)
Se banco PROD não tem dados de Local (países, estados, cidades):
```powershell
# Alterar temporariamente para recarregar dados
# application-prod.properties:
app.reload-data=true

# Reiniciar servidor (demora ~10 minutos)
# Após carregar, voltar para:
app.reload-data=false
```

---

## 📊 Monitoramento

### Durante a carga
- Monitorar logs: `logs/app.log` e `logs/app.err`
- Verificar uso de CPU/memória (Task Manager)
- Tempo esperado: ~1-5 segundos por registro (dependendo do hardware)

### Após a carga
- Verificar quantidade de registros criados
- Validar integridade dos relacionamentos
- Testar login com usuários criados (senha inicial aleatória)
- Verificar emails enviados (se configurado)

---

## 🚨 Rollback (Se necessário)

### Restaurar backup
```powershell
cd backup-tools
.\restore-completo.bat
```

### Reverter código
```powershell
git checkout <commit-anterior>
mvn clean package -DskipTests
.\run-prod.bat
```

---

## 📞 Contatos e Suporte

- Documentação: `README.md`, `README-prod.md`
- Backup/Restore: `backup-tools/instrucoes_backup_restore.md`
- Logs: `logs/app.log`, `logs/app.err`

---

## ✅ Commits Recentes (Referência)

```
22837f8 fix: corrige contagem de erros na validacao e rotulo de reversao
e90e7fb fix: usa selectedFile em vez de fileInput.files[0] no botao reverter
670b0ca fix: corrige IDs dos dropdowns no botao reverter
09a1a7d fix: corrige ID do input file para fileInput no botao reverter
6bd1ff4 feat: simplifica reversao para ler apenas coluna Email (G)
c54a8f3 feat: implementa reverter carga com tracking de IDs
7d7c4e9 feat: add redirect para /alterar-senha quando situacaoUsuario=P
```

---

**Última atualização**: 23 de novembro de 2025
**Próxima ação**: Deploy em PRODUÇÃO - 24 de novembro de 2025
