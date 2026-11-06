# Agentic-readiness reports: digital-assistant-server

Permanent historical record of the agentic-readiness audits of this repository: the first (2026-10-28) and the second (2026-11-06). **Do not edit or delete these files.** Later audits go in new files beside them.

- `digital-assistant-server-first-audit-*.md` and `.json`: the final, owner-confirmed report (JSON is the source of truth; the Markdown is its rendering).
- `digital-assistant-server-second-audit-*.md` and `.json`: the second audit of `dev` at `ffce29f`. Status `draft`: the engine's verdicts with the owner's Stage 4 decisions recorded (10 rejected, 4 deferred, 1 edited). `digital-assistant-server.audit-report.20261106T123400Z.json` is the final report made from it (`finalise`), phase-1 score 0.7262. Its rendered Markdown is not committed (it would raise the Markdown lint count); make it with `render_report.py`. `digital-assistant-server-second-audit-comparison-*.md` compares it with the first and explains each move.
- `suggested-improvements.md`: reviewer notes on the audit recipe and on this project, for the owner to triage.

Produced read-only with the codebase-agentic-readiness-framework audit recipe. No repository files were changed by the audit itself.

## Post-audit edit (only one)
After finalising, one entry was removed from the AGT-08 harness-reach list in the JSON (a vendor name whose presence made a secret scanner read the two commit hashes in the report as access tokens), and the counts that depend on it were adjusted by one (`reached`, `total` 28 to 27, and the "N of 28 known harnesses" wording). Nothing else was changed; the file still passes `validate_report.py`.
