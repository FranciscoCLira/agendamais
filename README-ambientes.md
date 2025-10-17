# Separação de Ambientes: Desenvolvimento e Produção

## Como usar

### Desenvolvimento

- **Opção recomendada:**
- **Opção recomendada:**
- Execute `run-dev.bat` (local) ou `run-prod.bat` (produção) — ambos agora carregam automaticamente variáveis do arquivo `.env`, se presente.
- Alternativamente, use `run-env-maven.bat` que executa um `mvn clean install` antes do `spring-boot:run`.
- Dessa forma, as variáveis de e-mail e outras sensíveis são lidas corretamente tanto no build quanto na execução.
- Execute `run-dev.bat` (local) ou `run-prod.bat` (produção) — ambos agora delegam o carregamento de `.env` ao utilitário PowerShell `scripts/run-with-env.ps1`.
- Alternativamente, use `run-env-maven.bat` que executa um `mvn clean install` antes do `spring-boot:run`.
- Dessa forma, as variáveis de e-mail e outras sensíveis são lidas corretamente tanto no build quanto na execução.

- **Opção alternativa:**
- **Opção alternativa:**
- Se preferir, use `run-env-maven.bat` para garantir que as variáveis sejam carregadas antes do `mvn clean install`.
- Ou defina as variáveis manualmente no terminal antes de rodar:
  ```cmd
  set SPRING_MAIL_HOST=smtp.gmail.com
  set SPRING_MAIL_USERNAME=seu-email@exemplo.com
  set SPRING_MAIL_PASSWORD=sua-senha
  set SPRING_MAIL_PORT=587
  set GESTOR_EMAIL=gestor@exemplo.com
  run-dev.bat
  ```

## Formatos de chave aceitos no `.env`

Os scripts aceitam várias formas de declarar as chaves no `.env`. Eles são mapeados automaticamente para as variáveis internas esperadas.

- MAIÚSCULAS com underscore (recomendado):
  - SPRING_MAIL_HOST, SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD, SPRING_MAIL_PORT, GESTOR_EMAIL
- minúsculas com pontos (mapeado automaticamente):
  - spring.mail.host, spring.mail.username, spring.mail.password, spring.mail.port, gestor.email
- minúsculas com underscore (mapeado automaticamente):
  - spring_mail_host, spring_mail_username, spring_mail_password

Exemplo `.env` válido (qualquer formato acima):

```
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=seu-email@exemplo.com
SPRING_MAIL_PASSWORD=sua-senha
SPRING_MAIL_PORT=587
GESTOR_EMAIL=gestor@exemplo.com

# ou usando formato com pontos
spring.mail.host=smtp.gmail.com
spring.mail.username=seu-email@exemplo.com
spring.mail.password=sua-senha
```

### Produção

- Em produção, as variáveis de ambiente devem ser configuradas diretamente no servidor (ou serviço cloud), não via `.env`.
- O script `run-prod.bat` apenas executa o projeto com o profile de produção, mas **não carrega variáveis do `.env`**.
- Certifique-se de definir as variáveis de ambiente no sistema operacional do servidor antes de rodar o projeto.
- Os scripts têm comportamento de validação configurável por perfil:
  - por padrão `dev` pula validações (útil para desenvolvimento local)
  - por padrão `prod` exige que variáveis essenciais estejam definidas
- Você pode sobrescrever esse comportamento com as flags:
  - `SKIP_ENV_VALIDATION=1` para pular validação
  - `FORCE_ENV_VALIDATION=1` para forçar validação mesmo em `dev`
- Exemplo: forçar validação ao rodar localmente:

```cmd
set FORCE_ENV_VALIDATION=1
run-dev.bat
```

- Se preferir carregar `.env` no servidor (não recomendado), garanta permissões e segurança apropriadas.

- Para depuração do parser `.env` você pode usar `DEBUG_ENV=1` para preservar o arquivo gerado `.env.generated.bat` e inspecioná-lo.
- Em desenvolvimento preferimos o novo PowerShell runner (mais robusto). Exemplos de uso direto com o runner:

- Apenas imprimir variáveis extraídas (sem executar a aplicação):

  ```powershell
  pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/run-with-env.ps1 -PrintVarsOnly -Debug
  ```

- Executar com profile `dev` (padrão):

  ```powershell
  pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/run-with-env.ps1 -Profile dev
  ```

- Executar com profile `prod` e forçar validação das variáveis obrigatórias:

  ```powershell
  pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/run-with-env.ps1 -Profile prod -ForceValidate
  ```

- Lista de variáveis obrigatórias personalizável (para validação):
  - Use `-RequiredEnvVars 'VAR1,VAR2,VAR3'` ao chamar `run-with-env.ps1`, ou
  - Coloque um arquivo `.env.required` com uma variável por linha no diretório do projeto.
  - Se nenhum for fornecido, o padrão é: `SPRING_MAIL_HOST,SPRING_MAIL_USERNAME,SPRING_MAIL_PASSWORD`.

## Arquivos de configuração

- `src/main/resources/application-dev.properties`: configurações de DEV
- `src/main/resources/application-prod.properties`: configurações de PROD

## Dica

Se quiser que `run-dev.bat` ou `run-prod.bat` também carreguem variáveis do `.env`, basta copiar o bloco de carregamento do `.env` do `run-env-maven.bat` para esses scripts.

Você pode adicionar variáveis e configurações específicas em cada arquivo conforme necessário.
