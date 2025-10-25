# AgendaMais — Production deployment notes

This file documents a minimal production deployment strategy for the AgendaMais application. It covers required environment variables, Docker usage, Flyway notes and a sample systemd unit.

Important: treat credentials and secrets with care. Use a secrets manager or environment variables injected securely (not checked into source control).

Required environment variables
- SPRING_DATASOURCE_URL — JDBC URL for the production database (e.g. jdbc:postgresql://db-host:5432/agendadb)
- SPRING_DATASOURCE_USERNAME — DB username
- SPRING_DATASOURCE_PASSWORD — DB password
- SPRING_DATASOURCE_DRIVER — JDBC driver class (default: org.postgresql.Driver)
- SPRING_MAIL_USERNAME — SMTP username (if mail used)
- SPRING_MAIL_PASSWORD — SMTP password
- SPRING_MAIL_HOST — SMTP host
- SPRING_MAIL_PORT — SMTP port
- GESTOR_EMAIL — manager/owner email used for application notifications
- SERVER_PORT — optional port (defaults to 8080)

Build & run with Docker (recommended, minimal)
1. Build the app (locally or CI):

   mvn -DskipTests package

2. Build the Docker image (from repo root):

   docker build -t agendamais:latest .

3. Run the container (example):

   docker run -d \
     -p 8080:8080 \
     -e SPRING_DATASOURCE_URL="jdbc:postgresql://db-host:5432/agendadb" \
     -e SPRING_DATASOURCE_USERNAME=your_user \
     -e SPRING_DATASOURCE_PASSWORD=your_password \
     -e SPRING_MAIL_USERNAME=... \
     --name agendamais agendamais:latest

Flyway and database migrations
- `spring.flyway.enabled=true` in `application-prod.properties` means the application will attempt to run DB migrations at startup.
- Always BACKUP the database before deploying new code that may run migrations.
- If a migration fails, inspect `flyway_schema_history` and consult Flyway docs for `repair` options. Prefer CI pipelines that run migrations against a staging DB first.

Disable H2 console in production
- The production profile disables the H2 console. Do not enable file-based H2 for production workloads.

Logging / metrics
- Consider externalized logs (e.g., stdout -> centralized log aggregator) and health checks. The app exposes `management` endpoints for `health` and `info` by default.

Systemd example
- See `deployment/agenda.service` for a sample systemd unit.

Further steps / suggestions
- Add a CI job that builds the Docker image and pushes to your registry.
- Add a migration-only job in CI that runs Flyway check/validate against a staging database.
- Add readiness and liveness probes (Kubernetes) or health checks (systemd, load balancer).
