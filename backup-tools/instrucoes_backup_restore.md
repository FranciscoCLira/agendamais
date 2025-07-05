✅ origem: `/src/main/resources/sql-backups/instrucoes_backup_restore.md`

---

# Instruções de Backup e Restore do Banco de Dados H2

Este documento descreve como gerar e restaurar backups específicos de tabelas do banco H2 utilizado no projeto **AgendaMais**.

---

## 🎯 Objetivo

Facilitar a criação e restauração de dados parciais sem afetar o restante do banco.

---

## 🗄️ Backup (Unloading)

Para gerar o script SQL contendo os dados de uma ou mais tabelas:

### 🔹 Exemplo: Backup de uma tabela

```sql
SCRIPT TO 'sub_instituicao_backup.sql' TABLE sub_instituicao;
































# 🎯 **Agenda Mais — Backup e Restore do Banco H2**

## 📂 Onde colocar os scripts

**Pasta sugerida no projeto (não pública):**

```
/src/main/resources/sql-backups
```

Exemplo de estrutura:

```
/src/main/resources/sql-backups
    backup_20250702.sql
    instrucoes_backup_restore.txt
```

---

## 🟢 Como gerar um backup

Abra o **H2 Console**:

```
http://localhost:8080/h2-console
```

Digite sua senha e conecte.

Depois, execute:

```sql
SCRIPT TO 'C:/meus-backups/backup_20250702.sql' TABLE
    instituicao,
    pessoa,
    usuario,
    usuario_instituicao,
    pessoa_instituicao,
    pessoa_sub_instituicao,
    sub_instituicao;
```

✅ O arquivo será criado no caminho informado.

---

## 🔄 Como restaurar o backup

1. Abra o H2 Console.
2. Execute o comando:

**Se estiver dentro do projeto (classpath):**

```sql
RUNSCRIPT FROM 'classpath:sql-backups/backup_20250702.sql';
```

**Se estiver fora do projeto (filesystem):**

```sql
RUNSCRIPT FROM 'C:/meus-backups/backup_20250702.sql';
```

---

## 🚮 Como excluir/recriar tabelas

Se precisar apagar antes de restaurar:

```sql
DROP TABLE pessoa_sub_instituicao;
DROP TABLE pessoa_instituicao;
DROP TABLE usuario_instituicao;
DROP TABLE sub_instituicao;
DROP TABLE instituicao;
DROP TABLE pessoa;
DROP TABLE usuario;
```

*(Atenção à ordem, por causa das chaves estrangeiras)*

---

## 🧹 Como limpar os dados sem apagar tabelas

Se quiser só **esvaziar**:

```sql
DELETE FROM pessoa_sub_instituicao;
DELETE FROM pessoa_instituicao;
DELETE FROM usuario_instituicao;
DELETE FROM sub_instituicao;
DELETE FROM instituicao;
DELETE FROM pessoa;
DELETE FROM usuario;
```

---

## 🏃 Executando pelo terminal

Você também pode usar o **H2 Jar** para executar o script:

Windows:

```
java -cp h2*.jar org.h2.tools.RunScript -url jdbc:h2:file:C:/caminho/do/banco -user sa -password suaSenha -script C:/meus-backups/backup_20250702.sql
```

---

## 🔑 Dicas importantes

✅ Sempre faça backup antes de **DROP** ou **DELETE**.

✅ Verifique se o arquivo SQL está salvo e completo.

✅ Você pode criar vários backups nomeados por data.

✅ Não armazene scripts sensíveis em pastas públicas (`static`).

---

Se quiser, posso te ajudar a:

* Criar `.bat` ou `.sh` para automatizar esses comandos.
* Montar exemplos prontos para Linux/macOS.

---

### 📁 **Próximos passos**

Se quiser, já te preparo um **exemplo de script .bat** ou um **template de pasta** para copiar.

Me conte! 🚀
