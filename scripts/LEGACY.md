This folder documents legacy scripts found in the repository root and `backup-tools`.

Recommendation:

- Keep legacy files for audit/history but move them under `scripts/legacy/` or `legacy-scripts/` if you want to declutter the root.
- Before deleting any legacy script, ensure no active process depends on them in deployment tooling.

Files considered legacy:

- run-env-maven.bat (older wrapper)
- any older `.bat` files that duplicate `run-dev.bat`/`run-prod.bat` functionality

To archive legacy scripts:

1. Create a folder `scripts/legacy/`
2. Move legacy files there and update README references
3. Optionally create a short wrapper that explains how to migrate to the new PowerShell runner

If you want, I can perform the move and update references automatically. Reply with "move legacy" to proceed.
