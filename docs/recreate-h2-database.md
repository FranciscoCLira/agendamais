# Recreating H2 Database for Development

## Overview

This document explains how to recreate the H2 database from scratch in the development environment. This is useful when you need to:

- Start with a clean database
- Reset all data to the initial state
- Fix database schema issues during development
- Test the complete database initialization process

## Prerequisites

- Maven installed and available in PATH
- Java 17+ installed
- All required environment variables set (if using email features)

## How to Use

### Windows

Simply run the batch script from the project root directory:

```cmd
recreate-h2-db.bat
```

### Linux / macOS / WSL

Run the shell script from the project root directory:

```bash
./recreate-h2-db.sh
```

## What the Script Does

1. **Checks for existing database files** in the `./data` directory
   - `agendadb.mv.db`
   - `agendadb.trace.db`

2. **Deletes existing database files** if found

3. **Creates temporary configuration** that overrides:
   - `spring.jpa.hibernate.ddl-auto=create` (drops and recreates schema)
   - `app.reload-data=true` (loads initial data via DataLoader)

4. **Starts the Spring Boot application** which will:
   - Create fresh database schema via JPA/Hibernate
   - Apply Flyway migrations
   - Load initial data via LocalDataLoader and DataLoader
   - Create sample institutions, users, and activities

5. **Cleans up** temporary configuration files

## Expected Behavior

When the script runs successfully, you should see:

1. Maven compilation output
2. Spring Boot startup logs
3. LocalDataLoader creating location data (countries, states, cities)
4. DataLoader creating:
   - 3 Institutions (Instituto Aurora, Instituto Luz, Instituto Cruz)
   - 9 Sub-institutions (3 per institution)
   - Sample activity types for each institution
   - 4 Users with different access levels
   - Associated person and institution relationships

5. Success message:
   ```
   ==================================================
     H2 Database recreated successfully!
   ==================================================
   ```

## After Recreation

Once the database is recreated, you can:

1. Stop the application (press Ctrl+C)
2. Run the application normally using:
   - Windows: `run-dev.bat`
   - Linux/Mac: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

## Sample Users

After recreation, these users will be available for login:

| Username | Password  | Access Level     | Institutions          |
|----------|-----------|------------------|-----------------------|
| parti1   | parti1$   | Participante (1) | All 3 institutions    |
| autor1   | autor1$   | Autor (2)        | All 3 institutions    |
| admin1   | admin1$   | Administrador (5)| All 3 institutions    |
| superu   | superu1$  | SuperUsuário (9) | All 3 institutions    |

## Troubleshooting

### Database files locked

If you see errors about database files being locked:
1. Stop any running instances of the application
2. Check for any H2 console connections and close them
3. Run the script again

### Maven not found

Ensure Maven is installed and available in your PATH:
```bash
mvn --version
```

### Application fails to start

Check for:
- Missing environment variables (see `.env.sample`)
- Port conflicts (default is 8080)
- Java version compatibility (requires Java 17+)

## Manual Alternative

If you prefer to recreate the database manually:

1. Stop the application if running
2. Delete database files:
   ```bash
   rm -f ./data/agendadb.mv.db
   rm -f ./data/agendadb.trace.db
   ```
3. Edit `src/main/resources/application-dev.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=create
   app.reload-data=true
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
5. After successful startup, stop the application
6. Revert `application-dev.properties` back to:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   app.reload-data=false
   ```

## Related Documentation

- [README.md](../README.md) - Main project documentation
- [README-ambientes.md](../README-ambientes.md) - Environment configuration
- `.env.sample` - Environment variables template
