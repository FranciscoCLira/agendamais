PR update / Suggested description — branch: tests/greenmail-ci

## Summary

This PR prepares the project for safer production bootstrap and CI validation of database migrations. Main changes:

- Make local seeders safer:

  - `LocalDataLoader` made idempotent (only insert missing 'Local' rows by default).
  - Prevent destructive reloads in dev by default (`app.reload-data=false` in dev config).

- Migration-based bootstrap (Flyway):

  - Added Java migration `V4__seed_initial_admin` which is guarded by `RUN_BOOTSTRAP=true` and idempotent.
  - The migration can create a system superuser (nivel 9) and an institution admin (nivel 5) when provided via environment variables:
    - SUPER_USERNAME, SUPER_PASSWORD, SUPER_EMAIL
    - ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL
  - Default `INSTITUTION_NAME` / `INSTITUTION_EMAIL` can be provided via env vars as well.

- CI improvements:

  - Added CI job `flyway-validate` (optional, runs when STAGING*DB*\* secrets are present) to validate migrations against a staging DB.
  - Added CI job `flyway-validate-service` which spins up a disposable Postgres service and runs Flyway validate — no external secrets required.

- Convenience tooling & docs:
  - `docs/bootstrap-local.md` — instructions for safely running the V4 bootstrap locally.
  - `scripts/run-bootstrap.ps1` and `scripts/run-bootstrap.cmd` — helpers to run Flyway migrate locally.
  - `scripts/transient-postgres.ps1` and `scripts/transient-postgres.cmd` — convenience script to start a transient Postgres container, run migrations, start the app in `prod` profile, and tear down the DB.

## How to bootstrap a staging DB locally (example)

1. Start a transient Postgres (recommended):

PowerShell:

```powershell
# runs container, migrates and runs the app (separate steps)
.\scripts\transient-postgres.ps1 start
.\scripts\transient-postgres.ps1 migrate
.\scripts\transient-postgres.ps1 run-app
```

or run full flow:

```powershell
.\scripts\transient-postgres.ps1 full
```

2. If you want the migration to create the initial accounts, set env vars before running `migrate` or `full`:

```powershell
$env:SUPER_USERNAME='super'
$env:SUPER_PASSWORD='ChangeMe123!'
$env:SUPER_EMAIL='super@example.com'
$env:ADMIN_USERNAME='admin'
$env:ADMIN_PASSWORD='ChangeMe123!'
$env:ADMIN_EMAIL='admin@example.com'
```

## Notes & safety

- The migration requires `RUN_BOOTSTRAP=true` (the script sets this automatically for the transient DB path). Do NOT set `RUN_BOOTSTRAP=true` against production DBs unless you intend to bootstrap and have backups.
- The scripts are intended for development/staging usage only.

## Suggested PR description to paste into GitHub

(Use this content as the PR description body)

---

This PR: safer local loaders, guarded Flyway bootstrap (V4), local transient-Postgres convenience scripts, and CI Flyway validate jobs.

Key points:

- V4\__seed_initial_admin is guard-protected (RUN_BOOTSTRAP) and idempotent. It accepts SUPER__ and ADMIN\__ env vars.
- CI will validate migrations via an ephemeral Postgres service (`flyway-validate-service`) and optionally against a staging DB when secrets are configured.
- Added local scripts for one-shot bootstrap and to run the app against a disposable Postgres container.

Run instructions, safety notes and examples are in `docs/bootstrap-local.md` and `scripts/transient-postgres.*`.

---

If you'd like, I can attempt to update the active PR body using GitHub CLI if you provide a token (or I can open a draft PR body file — already present at `docs/PR-update.md`).
