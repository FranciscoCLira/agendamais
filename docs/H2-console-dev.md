# H2 Console — safe local usage (dev profile)

This note explains how to safely enable the H2 Console for local development. The application exposes the H2 console only when the `dev` Spring profile is active. The project contains additional runtime checks that will warn when dev-only flags are enabled while the application is not running under the `dev` profile.

Why the guard exists
- The H2 console is intentionally permissive (frames allowed, CSRF disabled) to make local debugging easy.
- Exposing the console in production or on an internet-accessible host is a serious security risk.

Enable the H2 console for local development
1. Run with the `dev` profile. Example (Maven):

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or run the packaged jar with the dev profile:

```powershell
java -jar -Dspring.profiles.active=dev target/agenda-mais-0.0.1-SNAPSHOT.jar
```

2. Open the console in your browser:

- URL: http://localhost:8080/h2-console/
- JDBC URL (example, in-memory mode): jdbc:h2:mem:agendadb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL

Optional dev flags
- `app.reload-data=true` — reloads seeded data on startup. Use with care.
- `app.security.requireAdmin=false` — relaxes security for local development.

Safety notes
- Do not run the application in production with the `dev` profile or with the above dev flags enabled.
- The codebase includes a startup-time safety check (`DevProfileSafetyCheck`) that will log warnings if dev flags are enabled while the `dev` profile is not active.

If you need remote access for debugging, prefer using a secured VPN or SSH tunnel and enable strong authentication on the host instead of exposing the H2 console publicly.
