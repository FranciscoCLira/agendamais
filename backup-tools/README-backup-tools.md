# 🗄️ Backup & Restore - Agenda Mais

Scripts para gerenciar backups do banco H2.

## 📂 Estrutura

- `backup-completo.bat`: Backup completo (todas as tabelas).
- `backup-subinstituicao.bat`: Backup somente da tabela `sub_instituicao`.
- `restore-completo.bat`: Restaura backup completo.
- `restore-subinstituicao.bat`: Restaura somente `sub_instituicao`.

## 💻 Como usar

### 1️⃣ Preparar o ambiente

- Baixe o JAR do H2 e configure o caminho no `.bat`:
- https://h2database.com/html/main.html  


- set H2JAR=C:\libs\h2\h2-2.1.214.jar


- Pare a aplicação antes de rodar o backup/restore.


### 2️⃣ Executar os scripts

Abra o PowerShell na pasta `backup-tools`:

```powershell
cd C:\SEU_PROJETO\agendamais\backup-tools
```

Backup completo:

```powershell
.\backup-completo.bat
```

Backup apenas sub_instituicao:

```powershell
.\backup-subinstituicao.bat
```

Restore completo:

-- Aqui precisa remover todas as tabelas pois o restore tem o create table, EX: 
```powershell
   DROP TABLE IF EXISTS pessoa_sub_instituicao;
   DROP TABLE IF EXISTS pessoa_instituicao;
   DROP TABLE IF EXISTS usuario_instituicao; 
```

```powershell
.\restore-completo.bat
```

Restore apenas sub_instituicao:

-- mesma observação acima, remove da tabela na console do H2
-- DROP TABLE IF EXISTS sub_instituicao; 

```powershell
.\restore-subinstituicao.bat
```
 

## 🚀 Fluxo de Backup e restore
##### 1️⃣ Backup:

```powershell
.\backup-completo.bat
```
 
##### 2️⃣ Limpar DB:

```powershell
spring.jpa.hibernate.ddl-auto=create
```
  
 app.reload-data=false
 
 (inicia o app, que dropará e recriará vazio)


##### 3️⃣ Parar a app

##### 4️⃣ Restore:

 Mudar spring.jpa.hibernate.ddl-auto=none

 Rodar:

```powershell
.\restore-completo.bat
```
  
##### 5️⃣ Reiniciar app

### 🎯 3) Como não criar nada ao iniciar o app?
Use:

```powershell
spring.jpa.hibernate.ddl-auto=none
```
  
 Assim:

 O Hibernate não cria nenhuma tabela.

 Você pode rodar só o RUNSCRIPT para recriar tudo.



###🚀 Fluxo para restaurar todo o Banco de Dados - H2

#####1️⃣ Parar o app.

#####2️⃣ Rodar o script de DROP TABLE na Console H2 (vai excluir todas as tabelas).
````bash
        drop_all_tables.sql

        http://localhost:8080/h2-console
```` 
        Colar todo o conteúdo do drop_all_tables.sql e executar  
        O banco ficará vazio.
        
        Rodar para restaurar o banco de dados:
        
        .\restore-completo.bat
              

#####3️⃣ Rodar o restore-completo.bat (vai recriar tudo igual ao backup).

#####4️⃣ Subir o app com:

```powershell
spring.jpa.hibernate.ddl-auto=none
```

Assim o Hibernate não mexe no banco restaurado.

#####💡 Importante:
Esse é o fluxo de qualquer restore 100% fiel. Não dá para restaurar por cima das tabelas existentes se o backup inclui CREATE TABLE.






## 📂 Caminho padrão dos arquivos de backup
Se você usar apenas o nome do arquivo, ele é salvo no diretório raiz do projeto, junto do /target.

Recomendado: informe sempre o path relativo para /src/main/resources/sql-backups.


---

### 🟢 **Como fica a pasta `backup-tools`**

/agendamais/
/backup-tools/
backup-completo.bat
backup-subinstituicao.bat
restore-completo.bat
restore-subinstituicao.bat
README.md

