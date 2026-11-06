# Second audit comparison: digital-assistant-server

Second audit of `dev` at `ffce29f9c23a756d98466ff1f5cc41b7a24ebda0`, run 2026-11-06 by the implement recipe's
close-out (phase IR14.2). Status of the second report: **draft, awaiting the owner's Stage 4 confirmation**. The
engine's verdicts are accepted as they stand and no finding has a person's disposition yet, so the report cannot
be finalised in an unattended run. Every finding in it is **unconfirmed by owner**. The first report is unchanged.

- Framework revision: local branch `feat/implement-recipe` at `c132d97` (not yet pushed), ruleset `2026-11-06.1`,
  catalogue 0.2.2, `--no-user-config`. The first audit used ruleset `2026-10-19.2`.
- Probes: none allowed, as in the first audit (owner answer Q1). No target command ran; the Docker stack was not used.
- Score: 0.5437 to 0.7262 (57 scored checks). Blockers remaining 6 to 7; the count rose because the engine no longer
  reads the machine's own configuration (see the annotations).

The table section after the annotations is the output of `compare_reports.py`, unedited. The annotations explain the
12 moves it calls unexplained.

## Annotations on unexplained moves

| Move | What happened | Cause | Reference |
| --- | --- | --- | --- |
| IDX-01 PASS to FAIL (new F-003) | The first audit found the MCP registration in a user-level file. The second run does not read user files. | Engine change | MR06.3 (`--no-user-config`); issue #16 (open) |
| IDX-02 PASS to UNKNOWN | The index store sits in a user-level location, no longer read. An index exists: project `home-kc-Projects-UDAN-Digital-Assistant-Server`, status ready, indexed 2026-11-06T07:04Z (codebase-memory `index_status`). | Engine change | MR06.3; issue #16 |
| IDX-03 PARTIAL to UNKNOWN (first F-042 closed) | Freshness is a probe check and probes are off. The first audit resolved it by hand. | Engine change plus probes off (Q1) | MR06.3; issue #16 |
| CMD-02 PARTIAL to UNKNOWN (first F-041 closed) | The first audit overrode the engine's UNKNOWN by reading `build.gradle`. This run accepts the engine's UNKNOWN (Q2). For reference, `check` now covers formatCheck, typecheck and test in `build.gradle`. | Engine limit | issue #15 (open) |
| SEC-02 PASS to FAIL (new F-004) | `scripts/ci/smoke-test.sh:67` holds the dummy bearer value `sanity.check.invalid`, sent on purpose to prove a malformed token is refused. Not a secret. | Engine false positive on a sentinel token | issue #10 (open; MR05 fixed it only in part) |
| HYG-01 PASS to FAIL (new F-012) | `README.md` is 89 lines. The word "placeholder" appears in one configuration-table row. | New engine defect | issue #51 (opened here) |
| HYG-11 FAIL to PASS (first F-035 closed; state: deferred) | `docs/branch-protection.md` now declares branch protection. The item stays deferred because the settings are applied outside the repository. | Repo change | none |

## Blocker verification

Each blocker in the second report was read again against the pinned tree, by hand.

| Check | Engine verdict | Independent result |
| --- | --- | --- |
| IDX-01 | FAIL | Confirmed: no `.mcp.json`, `.cursor/` or other project-level file. `AGENTS.md:114` mentions the MCP in prose only. A real gap once user configuration is ignored. |
| EXEC-03 | FAIL | Engine wrong: `AGENTS.md` section 2 has "Setup (non-interactive, resolves locked dependencies): `./gradlew assemble`". The engine wants a fenced block or a setup verb name. Issue #8 (open). The state calls this item `done`. |
| EXEC-04 | FAIL | Engine wrong on the file: `scripts/ci/env.example` lists AWS_ROLE_ARN, INSTANCE_ID and SSM_DOCUMENT and says SECONDS is not inventory. The engine finds no template below the root. ADVISORIES and PACKAGES are script-local, IMAGE_TAR is set in a workflow. Issue #52 (opened here); #13 for builtins. The state calls this item `done`. |
| SEC-02 | FAIL | False positive, see above. Line 67 has no credential. Issue #10. |
| EXEC-02 | PARTIAL | `gradle.lockfile` is committed (528 lines) and `dependencyLocking { lockAllConfigurations() }` is set in `build.gradle:32-33`. Consistency needs a gate run, which probes-off does not allow. No finding raised. |
| CMD-02 | UNKNOWN | Not decided by the engine; see above. |

## Findings unconfirmed by owner

All 15 findings in the second report are `pending`. The four blockers are F-001 (EXEC-03), F-002 (EXEC-04),
F-003 (IDX-01) and F-004 (SEC-02). The owner confirms, rejects or defers them with `report.py set-disposition`
and then finalises. Where the independent reading disagrees with the engine (EXEC-03, EXEC-04, SEC-02, HYG-01), the
owner may reject the finding in Stage 4. F-015 (CI-04) was added by the agent check.

## Issues the owner may close

Fixed by the corrected engine since the first audit (framework branch, not yet pushed): #26 (MR06.4, AGT-08 accepts a
stated harness list) and #50 (MR06.4, TST-04). #8 (MR05, EXEC-06 comment stripping) is fixed for EXEC-06 but its
EXEC-03 wording still reproduces here, so check before closing. Partly addressed, keep open: #10 (MR05), #12 and
issue 20 (MR06.1, MR06.2), issue 16 (MR06.3). Opened by this phase: #51, #52.

## Found on the way

- EXEC-03 and EXEC-04 are `done` in the state yet still FAIL in the engine's view. Playbook fix: add a fenced setup
  block next to the prose line. Engine fix: #8, #52.
- TOOL-04 is `done` in the state yet still PARTIAL: the engine sees no dependency audit in the `security` verb
  because `scripts/ci/scan.sh` is report-only.
- IDX-01: a project-level MCP registration file would close it.

## Tool output

Output of `compare_reports.py` (title line dropped, headings demoted).

- Before: `digital-assistant-server-first-audit-20261028T131409Z.json`
  at `4a48e1976e424f3f424d2510049413c1c3f38dd6`,
  score 0.5437 (6 blockers)
- After: `digital-assistant-server-second-audit-20261106T091756Z.json`
  at `ffce29f9c23a756d98466ff1f5cc41b7a24ebda0`,
  score 0.7262 (7 blockers)
- State: used
- Checks: 3 changed, 29 improved, 3 regressed, 36 unchanged
- Findings: 28 closed, 3 new, 12 unchanged; 2 rejected ignored
- Unexplained moves: 12

### Checks that moved

| Check | Before | After | Move | Label |
| --- | --- | --- | --- | --- |
| AGT-03 | PARTIAL | PASS | improved | explained:done |
| AGT-04 | FAIL | PASS | improved | explained:done |
| AGT-05 | FAIL | PASS | improved | explained:done |
| AGT-07 | PARTIAL | PASS | improved | explained:done |
| AGT-09 | PARTIAL | PASS | improved | explained:done |
| AGT-10 | FAIL | PASS | improved | explained:done |
| AGT-11 | FAIL | PASS | improved | explained:partial |
| BASE-01 | FAIL | PASS | improved | explained:partial |
| BASE-03 | PARTIAL | PASS | improved | explained:partial |
| BASE-04 | FAIL | PARTIAL | improved | explained:partial |
| CMD-01 | FAIL | PASS | improved | explained:partial |
| CMD-02 | PARTIAL | UNKNOWN | changed | unexplained |
| CON-02 | FAIL | PASS | improved | explained:done |
| DOC-01 | FAIL | PASS | improved | explained:done |
| DOC-02 | FAIL | PASS | improved | explained:done |
| DOC-04 | FAIL | PASS | improved | explained:done |
| EXEC-02 | FAIL | PARTIAL | improved | explained:done |
| HYG-01 | PASS | FAIL | regressed | unexplained |
| HYG-02 | FAIL | PASS | improved | explained:done |
| HYG-03 | FAIL | PASS | improved | explained:done |
| HYG-04 | FAIL | PASS | improved | explained:done |
| HYG-06 | FAIL | PASS | improved | explained:done |
| HYG-08 | FAIL | PASS | improved | explained:done |
| HYG-11 | FAIL | PASS | improved | unexplained |
| IDX-01 | PASS | FAIL | regressed | unexplained |
| IDX-02 | PASS | UNKNOWN | changed | unexplained |
| IDX-03 | PARTIAL | UNKNOWN | changed | unexplained |
| NAV-04 | FAIL | PASS | improved | explained:done |
| SEC-02 | PASS | FAIL | regressed | unexplained |
| SEC-03 | PARTIAL | PASS | improved | explained:done |
| TOOL-01 | FAIL | PARTIAL | improved | explained:partial |
| TOOL-02 | FAIL | PARTIAL | improved | explained:partial |
| TOOL-04 | FAIL | PARTIAL | improved | explained:done |
| TOOL-05 | FAIL | PASS | improved | explained:partial |
| TST-05 | FAIL | PASS | improved | explained:partial |

### Findings that moved

| Key | Check | Move | Before | After | Severity | Label |
| --- | --- | --- | --- | --- | --- | --- |
| AGT-03 | AGT-03 | closed | F-007 | - | DEGRADER | explained:done |
| AGT-04 | AGT-04 | closed | F-008 | - | DEGRADER | explained:done |
| AGT-05 | AGT-05 | closed | F-009 | - | DEGRADER | explained:done |
| AGT-07 | AGT-07 | closed | F-010 | - | DEGRADER | explained:done |
| AGT-08:harness-reach | AGT-08 | closed | F-027 | - | COSMETIC | explained:partial |
| AGT-09 | AGT-09 | closed | F-013 | - | DEGRADER | explained:done |
| AGT-10 | AGT-10 | closed | F-014 | - | DEGRADER | explained:done |
| AGT-11 | AGT-11 | closed | F-039 | - | DEGRADER | explained:partial |
| BASE-01 | BASE-01 | closed | F-015 | - | DEGRADER | explained:partial |
| BASE-03 | BASE-03 | closed | F-017 | - | DEGRADER | explained:partial |
| CMD-01 | CMD-01 | closed | F-001 | - | BLOCKER | explained:partial |
| CMD-02 | CMD-02 | closed | F-041 | - | DEGRADER | explained:done |
| CON-02 | CON-02 | closed | F-002 | - | BLOCKER | explained:done |
| DOC-01 | DOC-01 | closed | F-019 | - | DEGRADER | explained:done |
| DOC-02 | DOC-02 | closed | F-020 | - | DEGRADER | explained:done |
| DOC-04 | DOC-04 | closed | F-028 | - | COSMETIC | explained:done |
| EXEC-02:jvm | EXEC-02 | closed | F-003 | - | BLOCKER | explained:done |
| HYG-01:README.md | HYG-01 | new | - | F-012 | COSMETIC | unexplained |
| HYG-02 | HYG-02 | closed | F-029 | - | COSMETIC | explained:done |
| HYG-03 | HYG-03 | closed | F-030 | - | COSMETIC | explained:done |
| HYG-04 | HYG-04 | closed | F-031 | - | COSMETIC | explained:done |
| HYG-06 | HYG-06 | closed | F-032 | - | COSMETIC | explained:done |
| HYG-08 | HYG-08 | closed | F-034 | - | COSMETIC | explained:done |
| HYG-11 | HYG-11 | closed | F-035 | - | COSMETIC | unexplained:deferred |
| IDX-01 | IDX-01 | new | - | F-003 | BLOCKER | unexplained |
| IDX-03 | IDX-03 | closed | F-042 | - | COSMETIC | unexplained:skipped |
| NAV-04 | NAV-04 | closed | F-036 | - | COSMETIC | explained:done |
| SEC-02:scripts/ci/smoke-test.sh | SEC-02 | new | - | F-004 | BLOCKER | unexplained |
| SEC-03 | SEC-03 | closed | F-021 | - | DEGRADER | explained:done |
| TOOL-05 | TOOL-05 | closed | F-025 | - | DEGRADER | explained:partial |
| TST-05 | TST-05 | closed | F-038 | - | COSMETIC | explained:partial |

### Findings still open

| Key | Check | Before | After | Severity | State |
| --- | --- | --- | --- | --- | --- |
| AGT-08:CLAUDE.md>AGENTS.md | AGT-08 | F-011 | F-005 | DEGRADER | skipped |
| AGT-08:vendor-files | AGT-08 | F-012 | F-006 | DEGRADER | partial |
| BASE-02 | BASE-02 | F-016 | F-007 | DEGRADER | partial |
| BASE-04 | BASE-04 | F-018 | F-008 | DEGRADER | partial |
| CI-04 | CI-04 | F-040 | F-015 | DEGRADER | partial |
| EXEC-03 | EXEC-03 | F-004 | F-001 | BLOCKER | done |
| EXEC-04 | EXEC-04 | F-005 | F-002 | BLOCKER | done |
| HYG-07 | HYG-07 | F-033 | F-013 | COSMETIC | deferred |
| TOOL-01 | TOOL-01 | F-022 | F-009 | DEGRADER | partial |
| TOOL-02 | TOOL-02 | F-023 | F-010 | DEGRADER | partial |
| TOOL-04 | TOOL-04 | F-024 | F-011 | DEGRADER | done |
| TOOL-06 | TOOL-06 | F-037 | F-014 | COSMETIC | skipped |
