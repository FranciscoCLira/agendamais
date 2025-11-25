# 📚 Entendendo Git Branches - Ambiente DEV e PROD

## 🎯 Situação Atual

### Branch Ativa (Onde Você Está Trabalhando)
```
* tests/greenmail-ci  ← VOCÊ ESTÁ AQUI
```

### Outras Branches Disponíveis
```
  main                ← Branch principal (geralmente usada para PRODUÇÃO)
  copilot/vscode...   ← Branch temporária do Copilot
```

---

## 🌳 Como Funciona o Sistema de Branches

### 1. **Branch = Linha do Tempo Independente**
Cada branch é como uma "linha do tempo paralela" do seu código:
- Commits feitos em uma branch **NÃO afetam** outras branches automaticamente
- É como ter várias cópias do projeto, cada uma evoluindo independentemente

### 2. **Branch Atual: `tests/greenmail-ci`**
```
Você está trabalhando aqui:
tests/greenmail-ci  ←  Suas mudanças estão APENAS nesta branch
         ↓
    17 commits à frente da main
    (suas features de carga massiva)
         ↓
    NÃO está em produção ainda
```

### 3. **Branch `main`**
```
main  ←  Branch principal (geralmente = PRODUÇÃO)
  ↓
Não tem suas mudanças recentes
  ↓
Está 17 commits atrás de tests/greenmail-ci
```

---

## 📊 Visualização dos Commits

### Commits APENAS em `tests/greenmail-ci` (não estão em `main`):
```
ca1df3c ← docs: checklist deploy prod
22837f8 ← fix: contagem erros validacao
e90e7fb ← fix: selectedFile reverter
670b0ca ← fix: IDs dropdowns
09a1a7d ← fix: ID fileInput
6bd1ff4 ← feat: simplifica reversao
de1aa9d ← refactor: reversao Excel
5cc0e58 ← feat: ajustes carga massiva  ⬅️ SUAS FEATURES ESTÃO AQUI
6c27d04 ← fix: validação campos
f01e434 ← feat: criação Locais
448eefb ← feat: carga massiva inscrições
f1d4cf1 ← fix: controle acesso
3cff582 ← feat: dynamic URL email
1cee093 ← fix: CSRF token
2f3d6cd ← fix: null-safety check
...mais 2 commits...
         ↑
    TOTAL: 17 commits
```

### Branch `main` (PRODUÇÃO)
```
Não tem nenhum dos 17 commits acima
  ↓
Está na versão ANTERIOR às suas features
```

---

## ❓ Perguntas e Respostas

### **P: Quando faço um commit, afeta DEV e PROD?**
**R:** NÃO! O commit afeta **apenas a branch atual**.

```
Você está em: tests/greenmail-ci
         ↓
git commit  →  Commit vai para tests/greenmail-ci
         ↓
Branch main NÃO é afetada
         ↓
PROD continua com código antigo
```

### **P: Minhas mudanças estão disponíveis para PROD?**
**R:** NÃO! Elas estão apenas em `tests/greenmail-ci`.

```
CÓDIGO ATUAL:

tests/greenmail-ci  →  TEM carga massiva ✅
                        TEM reversão ✅
                        TEM correções ✅

main (PROD)         →  NÃO TEM carga massiva ❌
                        NÃO TEM reversão ❌
                        NÃO TEM correções ❌
```

### **P: Como levar minhas mudanças para PROD?**
**R:** Fazendo um **MERGE** de `tests/greenmail-ci` para `main`.

---

## 🔄 Como Funciona o Merge (Fusão de Branches)

### Conceito
```
Merge = "Copiar" commits de uma branch para outra

tests/greenmail-ci  →  17 commits novos
         ↓
    git merge      (OPERAÇÃO DE FUSÃO)
         ↓
      main         →  Recebe os 17 commits
```

### Após o Merge
```
ANTES:
main                : commit A
tests/greenmail-ci  : commit A → B → C → D → ... (17 commits)

DEPOIS DO MERGE:
main                : commit A → B → C → D → ... (17 commits) ✅
tests/greenmail-ci  : commit A → B → C → D → ... (17 commits) ✅
                      ↑
                Ambas ficam iguais!
```

---

## 🚀 Processo para Colocar em PRODUÇÃO

### Passo 1: Comitar Mudanças Pendentes
```powershell
# Você tem 17 arquivos modificados (formatter)
git add -A
git commit -m "chore: formata código e ajusta imports"
```

### Passo 2: Mudar para Branch `main`
```powershell
git checkout main
```
**O que acontece:**
- Seus arquivos mudam para a versão da `main`
- Você "volta no tempo" para antes das features
- Código de carga massiva desaparece temporariamente (não se preocupe!)

### Passo 3: Fazer Merge de `tests/greenmail-ci` para `main`
```powershell
git merge tests/greenmail-ci
```
**O que acontece:**
- Git copia todos os 17+ commits para `main`
- Suas features voltam a aparecer
- `main` agora tem tudo que você desenvolveu

### Passo 4: Compilar e Rodar em PROD
```powershell
# Agora você está em main (= PROD)
mvn clean package -DskipTests
java -jar target/agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 🎨 Analogia Visual

### Imagine uma Editora de Livros

```
main (Livro Publicado)
  ↓
Capítulos 1-10  ←  Versão que os leitores têm

tests/greenmail-ci (Manuscrito do Autor)
  ↓
Capítulos 1-10 + 11-27  ←  Novos capítulos escritos
```

**Fazer merge = Publicar nova edição**
- Leitores (PROD) agora têm capítulos 1-27
- Versão publicada = Versão do manuscrito

---

## 📋 Checklist de Deploy para PROD

### ✅ Antes do Merge
- [x] Testar em DEV (`tests/greenmail-ci`)
- [x] Comitar mudanças pendentes
- [ ] **BACKUP do banco PROD**

### ✅ Durante o Merge
- [ ] Mudar para `main`: `git checkout main`
- [ ] Fazer merge: `git merge tests/greenmail-ci`
- [ ] Resolver conflitos (se houver)
- [ ] Verificar logs: `git log --oneline -10`

### ✅ Após o Merge
- [ ] Verificar `application-prod.properties`
- [ ] Compilar: `mvn clean package -DskipTests`
- [ ] Testar JAR: `Test-Path target\agenda-mais-0.0.1-SNAPSHOT.jar`
- [ ] Parar servidor PROD atual
- [ ] Iniciar servidor PROD com novo código
- [ ] Testar funcionalidades

---

## 🔍 Comandos Úteis

### Ver em qual branch você está
```powershell
git branch
# O asterisco (*) mostra a branch ativa
```

### Ver diferenças entre branches
```powershell
# Commits que tests/greenmail-ci tem e main não tem
git log main..tests/greenmail-ci --oneline

# Commits que main tem e tests/greenmail-ci não tem
git log tests/greenmail-ci..main --oneline
```

### Ver status atual
```powershell
git status
```

### Ver histórico de commits
```powershell
git log --oneline -20
```

---

## 🚨 Avisos Importantes

### 1. **Servidor Usa Profile, Não Branch**
```
O servidor NÃO sabe qual branch está ativa!

Ele usa o PROFILE:
- --spring.profiles.active=dev-docker  →  Usa application-dev-docker.properties
- --spring.profiles.active=prod        →  Usa application-prod.properties

Você pode rodar qualquer branch com qualquer profile:
- Branch main + profile dev-docker  ✅ (possível)
- Branch tests/greenmail-ci + profile prod  ✅ (possível)
```

### 2. **Merge Não Cria Backup Automaticamente**
```
⚠️ SEMPRE faça backup do banco ANTES de merge para main!

Motivo: Se algo der errado, você pode restaurar
```

### 3. **Mudanças Locais vs Remotas**
```
Seus commits estão apenas no seu computador (local)

Para enviar para GitHub (remote):
git push origin tests/greenmail-ci  ←  Envia tests/greenmail-ci
git push origin main                 ←  Envia main
```

---

## 🎓 Resumo Simplificado

| Conceito | O que é | Exemplo |
|----------|---------|---------|
| **Branch** | Linha do tempo do código | `main`, `tests/greenmail-ci` |
| **Commit** | Salvar mudanças na branch atual | `git commit -m "mensagem"` |
| **Merge** | Copiar commits de uma branch para outra | `git merge tests/greenmail-ci` |
| **Checkout** | Mudar de branch | `git checkout main` |
| **Profile** | Configuração do servidor | `--spring.profiles.active=prod` |

---

## ✅ Próxima Ação

**Você está pronto para:**
1. Comitar mudanças pendentes em `tests/greenmail-ci`
2. Fazer merge para `main`
3. Compilar e rodar em PROD

**Quer prosseguir?** Vou guiá-lo passo a passo! 🚀
