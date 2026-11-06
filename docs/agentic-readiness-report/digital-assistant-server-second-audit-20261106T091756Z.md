# Agentic readiness audit: digital-assistant-server

> [!NOTE]
> **Draft.** This report is not final and can still change. 15 findings await a decision.

> [!NOTE]
> This audit reports; it does not gate. A check that could not be settled is UNKNOWN: it earns no credit and is never shown as a pass.

## Provenance

| Field | Value |
|---|---|
| Target | `/tmp/ir14-2/server-audit` |
| Commit | `ffce29f9c23a` — clean working tree |
| Status | draft |
| Started | `2026-11-06T09:17:56+00:00` |
| Finalised | — |
| Recipe | `brownfield-audit` 0.1.0, catalogue `0.2.8` |
| Engine | `python-agentic-audit` 0.1.0, ruleset `2026-11-06.1`, ruleset hash `970e6792f98eeeb5`, scanned `2026-11-06T09:18:01+00:00` |
| Framework revision | `c132d974bdc5` |
| Agent (self-reported) | `Claude Code` with `claude-sonnet-5-5` |
| Probes | none: no command of the target was run |
| Report schema | `0.1` |

## Readiness summary

- **Phase-1 score: 0.73**, over 57 scored checks
- **7 blockers remaining: 4 FAIL, 1 PARTIAL, 2 UNKNOWN**
- **Unattested:** 5 requirements that no check settles, listed below

5 of the 57 scored checks are UNKNOWN. An UNKNOWN check earns no credit, so the score understates what is true of the repository and does not certify it. 1 agent finding still awaits a person's decision.

The score is the weighted share of the scored checks that the repository meets. The weights are BLOCKER 3, DEGRADER 2, COSMETIC 1; PASS earns full credit, PARTIAL half, and FAIL and UNKNOWN none. The framework sets no pass mark, and a score says nothing about whether agents will write correct code.

### Unattested requirements

A person's review or the owner's declaration verifies these requirements, so no check gives them a verdict, and they are outside the score. One leaves this list only when a person records an outcome or the owner declares it.

| Requirement | Method | Property | Why |
|---|---|---|---|
| `REQ-SH-004` | attested | `index-in-use` | Whether the codebase index is actually used during agent work. |
| `REQ-BF-004` | attested | `baseline-adequacy` | Whether the committed baseline or budget is adequate. |
| `REQ-SH-050` | attested | `suite-trustworthiness` | Whether the passing test suite actually catches regressions. |
| `REQ-SH-020` | attested | `doc-accuracy` | Whether the documentation matches the code it describes. |
| `REQ-SH-026` | attested | `gate-honoured` | Whether the gate is actually run and honoured before merge. |

## Checks by verdict and severity

The first table counts every check in the report. The second counts the scored checks only.

| Verdict | Checks | Credit | Meaning |
|---|---:|---:|---|
| PASS | 50 | 1 | The requirement is met. |
| PARTIAL | 7 | 0.5 | Part of the requirement is met. |
| FAIL | 8 | 0 | The requirement is not met. |
| UNKNOWN | 6 | 0 | The evidence could not be gathered. It is never read as a pass. |
| NOT_APPLICABLE | 0 | outside the score | The check does not apply to this repository. |
| **Total** | **71** |  |  |

| Severity | Weight | Meaning | Scored checks | Not met |
|---|---:|---|---:|---:|
| BLOCKER | 3 | Agent work is impossible, unsafe or unverifiable. Fix before onboarding agents. | 16 | 7 |
| DEGRADER | 2 | Agent work is possible but unreliable or wasteful. | 37 | 10 |
| COSMETIC | 1 | Hygiene. Fix when convenient. | 4 | 1 |

## Command surface

The framework's seven verbs, resolved verb by verb on the target's runners — CMD-01's structured payload; CMD-02, AGT-05 and CI-02 read the same surface.

7 verbs of 7 verbs resolve. Every verb resolves.

| Verb | Resolved | Runner | Command |
|---|:---:|---|---|
| `format` | yes | `build.gradle` | `./gradlew format` |
| `format:check` | yes | `build.gradle` | `./gradlew formatCheck` |
| `lint` | yes | `build.gradle` | `./gradlew lint` |
| `typecheck` | yes | `build.gradle` | `./gradlew typecheck` |
| `test` | yes | `build.gradle` | `./gradlew test` |
| `check` | yes | `build.gradle` | `./gradlew check` |
| `security` | yes | `build.gradle` | `./gradlew security` |

## Credential-shaped lines

1 credential-shaped line across 1 file in tracked text files. The file-by-file detail is in the attention tables above; the report never records the value itself.

## Missing secret-shape ignore rules

The secret shapes SEC-01 and SEC-03 check for, and whether the repository's ignore rules cover each one. The suggested entry is paste-ready into `.gitignore` for a shape that is missing.

| Shape | Status | Suggested `.gitignore` entry |
|---|:---:|---|
| dotenv | covered | `.env` |
| dotenv variant | covered | `.env.*` |
| ssh private key | covered | `id_rsa*` |
| PEM | covered | `*.pem` |
| key | covered | `*.key` |
| PKCS#12 | covered | `*.p12` |
| PFX | covered | `*.pfx` |
| keystore | covered | `*.keystore` |
| certificate (.crt) | covered | `*.crt` |
| certificate (.cer) | covered | `*.cer` |
| AWS credentials | covered | `.aws/*` |

## Checks that need attention

Each row is a finding, or a check that has none. A check is listed when it is not met, was overridden, was judged by the agent, or raised a finding. Evidence is grouped by who captured it, and a quote or command output is under Evidence in full. The decision on a finding is one of these:

- **interactive**: Decided by the person, on this finding alone.
- **batch**: Decided by the person, as one of a group of findings that are not blockers.
- **policy**: Set by the confirmation policy, not by a person.

### Blocker checks

7 of the 16 scored BLOCKER checks are not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `IDX-01` codebase-memory-mcp registered for this repository<br>finding `F-003` | **FAIL** | An agent cannot discover the codebase through an MCP index because no MCP configuration names codebase-memory-mcp for this repository. | We did not find any evidence. | Register the server in a project config (for example \`opencode.json\`) or a user config (\`~/.config/opencode/opencode.json\`). | **Awaiting a decision** |
| `IDX-02` An index exists for this checkout | **UNKNOWN** | index store could not be located; searched: user-level locations (not read: --no-user-config), /tmp/ir14-2/server-audit/.codebase-memory/graph.db.zst | We did not find any evidence. | — | — |
| `CMD-02` check composes the read-only verbs | **UNKNOWN** | the build.gradle check is a build lifecycle task, and what it composes is set by the build's task graph, which the engine does not read | We did not find any evidence. | — | — |
| `EXEC-02` Lockfile committed and consistent | **PARTIAL** | consistency unverified: gradle.lockfile | — | — | — |
| `EXEC-03` Non-interactive setup path<br>finding `F-001` | **FAIL** | An agent cannot prepare the environment non-interactively because no devcontainer, compose file, Dockerfile, setup target/script, or documented install command exists. | We did not find any evidence. | Add a committed setup path (a devcontainer, compose file, or a documented install command in README.md) that runs with no prompts. | **Awaiting a decision** |
| `EXEC-04` Env-var inventory complete<br>finding `F-002` | **FAIL** | An agent cannot know every variable the code needs before running it because 8 referenced key(s) are absent from any committed example file: ADVISORIES, AWS_ROLE_ARN, CACHE_DIR, IMAGE_TAR, INSTANCE_ID, PACKAGES, SECONDS, SSM_DOCUMENT. | We did not find any evidence. | Add the missing keys to .env.example (values redacted) so the environment can be provisioned without reading the source. | **Awaiting a decision** |
| `SEC-02` No key-shaped strings in tracked files<br>finding `F-004` | **FAIL** | An agent cannot keep credentials out of the repository and its history because scripts/ci/smoke-test.sh contains 1 credential-shaped line(s). | **tool** `scripts/ci/smoke-test.sh:67` (credential-shaped string) | Remove the value from the file and its history, load it from the environment, and rotate the credential. The report never records the value itself. | **Awaiting a decision** |

### Degrader checks

10 of the 37 scored DEGRADER checks are not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `IDX-03` Index freshness | **UNKNOWN** | index freshness is a Tier-C probe; re-run with --run-gates to compare the indexed revision against HEAD | We did not find any evidence. | — | — |
| `AGT-08` No competing instruction files, and the reach matrix is stated<br>finding `F-005` | **FAIL** | An agent cannot be sure that both files are read because CLAUDE.md shadows AGENTS.md for the harness that defines the pair, so only one of the two is ever loaded. | **tool** `CLAUDE.md`<br>`AGENTS.md` | Reconcile the two files, or delete the shadowed one — a file nobody reads still gets edited. | **Awaiting a decision** |
| `AGT-08` No competing instruction files, and the reach matrix is stated<br>finding `F-006` | **FAIL** | An agent cannot know which instruction file is authoritative because 2 vendor-specific instruction files exist alongside the canonical one (CLAUDE.md, CONVENTIONS.md). | **tool** `CLAUDE.md`<br>`CONVENTIONS.md` | Keep AGENTS.md canonical; reduce each vendor file to a pointer at it, or delete it if the harness reads AGENTS.md natively. | **Awaiting a decision** |
| `TOOL-01` Formatter configured<br>finding `F-009` | **PARTIAL** | An agent cannot format a change set the same way every time because a format verb exists but no formatter configuration file is present. | **tool** `build.gradle` (./gradlew format) | Commit a formatter config (for example \`.prettierrc.json\`) so the tool's behaviour does not depend on defaults. | **Awaiting a decision** |
| `TOOL-02` Linter configured<br>finding `F-010` | **PARTIAL** | An agent cannot catch lint regressions before they land because a lint verb exists but no linter configuration file is present. | **tool** `build.gradle` (./gradlew lint) | Commit a linter config (for example \`.eslintrc.json\` or \`\[tool.ruff\]\`) so the rule set does not depend on defaults. | **Awaiting a decision** |
| `TOOL-03` Type checker configured | **UNKNOWN** | no recognised strict type-check configuration and no ecosystem this pack knows one for, so type-checking cannot be judged | We did not find any evidence. | — | — |
| `TOOL-04` Dependency / security scan verb<br>finding `F-011` | **PARTIAL** | An agent cannot know whether a dependency carries a known vulnerability because a security verb exists but it does not run a dependency-audit check. | **tool** `build.gradle` (./gradlew security) | Make the security verb run a dependency audit (npm audit, pip-audit, osv-scanner, cargo audit, govulncheck, snyk). | **Awaiting a decision** |
| `BASE-02` Current violation counts measured<br>finding `F-007` | **PARTIAL** | An agent cannot state how many violations exist today because a counting surface exists but no numeric violation budget or count is committed. | **tool** `build.gradle` (lint)<br>`build.gradle` (security)<br>`build.gradle` (typecheck)<br>`docs/agentic-readiness/digital-assistant-server-quality-baseline.json` | Commit the count or configure a budget (\`--max-warnings N\`, \`max-issues\`, or a \`violation budget\`) so a regression has a baseline number to compare against. | **Awaiting a decision** |
| `BASE-04` Coverage floor configured<br>finding `F-008` | **PARTIAL** | An agent cannot know when coverage drops below an agreed floor because a coverage threshold is described in prose but not configured in the test runner. | **tool** `AGENTS.md`<br>`AGENTS.md` | Move the threshold into test-runner configuration (\`coverage.fail_under\`, \`--cov-fail-under\`, \`coverageThreshold\`, or a vitest/jest \`thresholds\` block). | **Awaiting a decision** |
| `TST-02` Suite status known | **UNKNOWN** | probe not run: probes disabled (pass --run-gates) | We did not find any evidence. | — | — |
| `AGT-11` Instructions are specific and not inferable<br>agent, confidence medium | **PASS** | About 63 of 74 rule lines (85%) in AGENTS.md are specific (verified commands, real paths, prohibitions, non-default conventions); 11 are generic (the preamble and section 1 maxims, section 5 steps 1-4). All sampled specific lines resolve; section 6 is now one pointer line, no overview section dominates. | **agent** `AGENTS.md:14-44` (specific: Section 2 commands. Sampled gradlew verbs resolve to tasks registered in build.gradle:62-129; scripts/ci/coverage.sh, check-baseline.sh, scan.sh, lefthook.yml, sync-credentials.sh, pre-flight-check.sh, start-server.sh all exist)<br>`AGENTS.md:53-62` (specific: Section 3 boundary rules name config/CustomTenantResolver, RequireRealmFilter, XssSanitizationFilter, SecurityHeadersFilter, GlobalExceptionMapper, SequenceListDAO, UserclicknodesRepository, Clickevents, StatusService; each exists once under src/main/java/com/nistapp/uda/index/)<br>`AGENTS.md:64-75` (specific: Section 4 prohibitions (tests, generated output, secret stores, default branch); docs/workflow-profiles.md, docs/STYLE_GUIDE.md, docs/adr and docs/architecture exist)<br>`AGENTS.md:1-10` (generic: preamble and section 1 (golden rule, problem-solving approach, strict scope adherence) are maxims with no project-specific content) | — | — |
| `CI-04` CI enforces the same gate as the local check<br>finding `F-015` · agent, confidence medium | **PARTIAL** | An agent cannot be sure a green CI run means every verb passed because the dependency scan (the security verb) and the docs lint run outside the CI Gate job, the image scan step is continue-on-error, and the required-status-check wiring is not readable from the repository. | **agent** `.github/workflows/ci.yml:243-262` (gate needs build-test, secrets, codeql, workflow-lint only)<br>`.github/workflows/ci.yml:97-108` (scan job is report-only by design (recorded debt))<br>`.github/workflows/image.yml:174` (quote in Evidence in full)<br>**searches** `grep` for `continue-on-error\|[\|][\|] true\|set [+]e` in `.github/workflows scripts/ci`: 2 hits | Confirm on the platform that CI Gate is the required status check; decide whether the report-only scan and docs lint should join the gate once their baselines are at zero. | **Awaiting a decision** |

### Cosmetic checks

1 of the 4 scored COSMETIC checks is not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `TOOL-06` Commit convention enforced<br>finding `F-014` | **FAIL** | An agent cannot rely on a consistent commit-message convention because no commitlint config, commit-msg hook, or equivalent exists. | **tool** `build.gradle` | Add a commitlint config or a commit-msg hook so commit messages follow one convention. | **Awaiting a decision** |
| `NAV-05` Public-symbol documentation coverage | **UNKNOWN**<br>not scored | no recognised source language, so 'public' cannot be defined | We did not find any evidence. | — | — |
| `HYG-01` README present and non-placeholder<br>finding `F-012` | **FAIL**<br>not scored | An agent cannot learn what this project is because README.md exists but is a placeholder — contains the placeholder marker 'placeholder'. | **tool** `README.md` | Replace the stub with a description, a quick-start, and a link to the documentation. | **Awaiting a decision** |
| `HYG-07` CHANGELOG present and recent<br>finding `F-013` | **FAIL**<br>not scored | An agent cannot see what changed between two versions because no CHANGELOG (or CHANGES/HISTORY) document exists. | We did not find any evidence. | Add CHANGELOG.md and record notable changes under a version heading. | **Awaiting a decision** |

## Overrides of the engine's verdicts

No engine verdict was overridden.

## Evidence in full

Quotes and command output that the tables above point to, each under the check that cites it.

### CI-04: CI enforces the same gate as the local check

- `.github/workflows/ci.yml:97-108` — scan job is named report-only and is not in the gate's needs list · captured by agent
  ```text
  name: Dependency Scan (report-only)
  ```
- `.github/workflows/image.yml:174` — image scan and SBOM step is non-gating · captured by agent
  ```text
  continue-on-error: true
  ```
- `.github/workflows/ci.yml:241` — comment says CI Gate is the single required status check for branch protection; platform settings are not in the repository and were not read · captured by agent
  ```text
  Single required status check for branch protection
  ```
- `.github/workflows/image.yml:174` · captured by agent
  ```text
  continue-on-error: true
  ```

## Coverage

**Examined:** nothing is recorded

No scope was skipped.

**Files:** 175 files inspected, 1 skipped; the scan was not cut short.

## Stack and components

| Stack | Detected |
|---|---|
| Ecosystems | `jvm` |
| Package managers | `gradle` |
| CI providers | `github` |
| Test frameworks | `junit` |
| Hook managers | `lefthook` |

| Component | Path | Declared | Declared by | Manifest | AGENTS.md |
|---|---|---|---|---|---|
| (root) | `.` | yes | implicit | `build.gradle` | present |

## Run context

| Field | Value |
|---|---|
| Evaluation mode | no |
| Confirmation policy | `interactive` |
| Provider exposure acknowledged | yes: the model provider receives what the agent reads from the target |
| Code index | `mcp` `codebase-memory-mcp` |
| Excluded | `build/*`, `.gradle/*` |
| Known components | none |
| Stack profile | `java`, shipped, confirmed by the user |

## Appendix — all checks

All 71 checks, grouped by family. The command surface, the credential-shaped-lines total and the missing secret-shape ignore rules above are rendered from the engine's structured payloads; other payloads (env-key inventories, harness-reach matrices, and so on) are in the JSON report only, for now.

### IDX

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `IDX-01` | codebase-memory-mcp registered for this repository | BLOCKER | automated | yes | FAIL | FAIL | no project-level MCP configuration found (user-level configuration not read: --no-user-config) |
| `IDX-02` | An index exists for this checkout | BLOCKER | automated | yes | UNKNOWN | UNKNOWN | index store could not be located; searched: user-level locations (not read: --no-user-config), /tmp/ir14-2/server-audit/.codebase-memory/graph.db.zst |
| `IDX-03` | Index freshness | DEGRADER | automated | yes | UNKNOWN | UNKNOWN | index freshness is a Tier-C probe; re-run with --run-gates to compare the indexed revision against HEAD |

### AGT

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `AGT-01` | Instruction file present, canonical, and non-trivial | BLOCKER | automated | yes | PASS | PASS | — |
| `AGT-02` | Component coverage by instruction files | DEGRADER | automated | yes | PASS | PASS | single-root repository: no component-level instruction files required |
| `AGT-03` | Documentation contract named | DEGRADER | automated | yes | PASS | PASS | docs/, the style guide and the scratch contract are named |
| `AGT-04` | Definition of done stated | DEGRADER | automated | yes | PASS | PASS | definition of done ties a doc trigger to the same change set |
| `AGT-05` | Seven verbs named | DEGRADER | automated | yes | PASS | PASS | all 7 verbs named in AGENTS.md and resolved on a runner |
| `AGT-06` | Architectural boundaries declared | DEGRADER | automated | yes | PASS | PASS | 11 boundary rules reference real paths |
| `AGT-07` | Do-not-do / deny rules present | DEGRADER | automated | yes | PASS | PASS | prohibitions cover tests, generated files, the default branch and secrets |
| `AGT-08` | No competing instruction files, and the reach matrix is stated | DEGRADER | automated | yes | FAIL | FAIL | 22/28 harnesses reached; AGENTS.md:5 states the supported harnesses (Anthropic Claude Code, Cursor, OpenAI Codex CLI, opencode) |
| `AGT-09` | Branch and release boundary declared | DEGRADER | automated | yes | PASS | PASS | the branch model and the release boundary are stated |
| `AGT-10` | Instruction indirection resolved | DEGRADER | automated | yes | PASS | PASS | — |
| `AGT-11` | Instructions are specific and not inferable | DEGRADER | agent | yes | PASS | — | About 63 of 74 rule lines (85%) in AGENTS.md are specific (verified commands, real paths, prohibitions, non-default conventions); 11 are generic (the preamble and section 1 maxims, section 5 steps 1-4). All sampled specific lines resolve; section 6 is now one pointer line, no overview section dominates. |

### DOC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `DOC-01` | docs/ exists | DEGRADER | automated | yes | PASS | PASS | 16 markdown file(s) under docs/ |
| `DOC-02` | docs/STYLE_GUIDE.md exists | DEGRADER | automated | yes | PASS | PASS | docs/STYLE_GUIDE.md present and named in AGENTS.md |
| `DOC-03` | artefacts/ exists and is ignored | DEGRADER | automated | yes | PASS | PASS | no scratch directory in use |
| `DOC-04` | ADR directory and index | COSMETIC | automated | yes | PASS | PASS | 3 ADR(s) indexed in docs/adr/README.md |

### CMD

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CMD-01` | Seven verbs resolvable on one runner | BLOCKER | automated | yes | PASS | PASS | format -> build.gradle; format:check -> build.gradle; lint -> build.gradle; typecheck -> build.gradle; test -> build.gradle; check -> build.gradle; security -> build.gradle |
| `CMD-02` | check composes the read-only verbs | BLOCKER | automated | yes | UNKNOWN | UNKNOWN | the build.gradle check is a build lifecycle task, and what it composes is set by the build's task graph, which the engine does not read |
| `CMD-03` | No divergent second definition | DEGRADER | automated | yes | PASS | PASS | no runner defines check, so there is nothing to diverge |
| `CMD-04` | Write verbs separated from read-only verbs | DEGRADER | automated | yes | PASS | PASS | no read-only verb invokes a write verb |

### TOOL

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `TOOL-01` | Formatter configured | DEGRADER | automated | yes | PARTIAL | PARTIAL | format verb on build.gradle but no formatter config file |
| `TOOL-02` | Linter configured | DEGRADER | automated | yes | PARTIAL | PARTIAL | lint verb on build.gradle but no linter config file |
| `TOOL-03` | Type checker configured | DEGRADER | automated | yes | UNKNOWN | UNKNOWN | no recognised strict type-check configuration and no ecosystem this pack knows one for, so type-checking cannot be judged |
| `TOOL-04` | Dependency / security scan verb | DEGRADER | automated | yes | PARTIAL | PARTIAL | security verb on build.gradle runs no dependency audit (./gradlew security) |
| `TOOL-05` | Local hook chain active | DEGRADER | automated | yes | PASS | PASS | wired: lefthook.yml |
| `TOOL-06` | Commit convention enforced | COSMETIC | automated | yes | FAIL | FAIL | no commit-message rule |

### CI

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CI-01` | Pipeline present, triggered on PRs | BLOCKER | automated | yes | PASS | PASS | PR-triggered: .github/workflows/ci.yml (github); .github/workflows/image.yml (github); .github/workflows/pr-title.yml (github) |
| `CI-02` | Local↔CI parity | BLOCKER | automated | yes | PASS | PASS | CI verbs: none; local read-only verbs not exercised: check, format:check, lint, security, test, typecheck |
| `CI-03` | CI-only steps flagged | DEGRADER | automated | yes | PASS | PASS | no CI-only steps |
| `CI-04` | CI enforces the same gate as the local check | DEGRADER | agent | yes | PARTIAL | — | build-test, secrets, codeql and workflow-lint gate the pull request through the CI Gate job (no continue-on-error, no swallowed exit); the dependency scan (the security verb) is report-only by design and sits outside the gate, docs-lint is also outside it, the image scan step is continue-on-error; required-status-check wiring on the platform cannot be read from the repository. |

### BASE

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `BASE-01` | Baseline artifact exists | DEGRADER | automated | yes | PASS | PASS | baseline/suppression mechanism present: docs/agentic-readiness/digital-assistant-server-quality-baseline.json |
| `BASE-02` | Current violation counts measured | DEGRADER | automated | yes | PARTIAL | PARTIAL | counting surface present but no committed count/budget: build.gradle:lint, build.gradle:security, build.gradle:typecheck, docs/agentic-readiness/digital-assistant-server-quality-baseline.json; method: static configuration scan; the target's own tools are never run |
| `BASE-03` | No-new-violations mechanism | DEGRADER | automated | yes | PASS | PASS | ratchet present: baseline compared in CI (.github/workflows/ci.yml) |
| `BASE-04` | Coverage floor configured | DEGRADER | automated | yes | PARTIAL | PARTIAL | coverage threshold stated in prose only: AGENTS.md:\* \*\*coverage\*\* (needs the local stack): \`./gradlew coverage\` runs the tests, w; AGENTS.md:Existing debt is counted and cannot grow. \`./gradlew formatCheck\` runs \`scripts/ |

### CON

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CON-01` | Allow/deny artifact present | BLOCKER | automated | yes | PASS | PASS | the prohibitions section of AGENTS.md names paths that exist: src/main/java/com/nistapp/uda/index/, config/, models/, repository/, services/, users/, utils/, StatusService.java (+8 more) |
| `CON-02` | Test directories protected | BLOCKER | automated | yes | PASS | PASS | the prohibitions section of AGENTS.md names the test directories (src/test/) |
| `CON-03` | artefacts/ excluded from index and agent reads | DEGRADER | automated | yes | PASS | PASS | no scratch or agent-state directory in use |
| `CON-04` | Draft allow/deny emitted | COSMETIC | automated | no | PASS | PASS | draft deny list written: /tmp/ir14-2/out-server/do-not-do.draft.md |

### EXEC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `EXEC-01` | Toolchain pinned / wrapper committed | BLOCKER | automated | yes | PASS | PASS | pinned: jvm (gradlew) |
| `EXEC-02` | Lockfile committed and consistent | BLOCKER | automated | yes | PARTIAL | PARTIAL | consistency unverified: gradle.lockfile |
| `EXEC-03` | Non-interactive setup path | BLOCKER | automated | yes | FAIL | FAIL | no setup path found |
| `EXEC-04` | Env-var inventory complete | BLOCKER | automated | yes | FAIL | FAIL | 8 referenced key(s) are absent from any committed example file: ADVISORIES, AWS_ROLE_ARN, CACHE_DIR, IMAGE_TAR, INSTANCE_ID, PACKAGES, SECONDS, SSM_DOCUMENT |
| `EXEC-05` | No generated artifacts committed in-tree | DEGRADER | automated | yes | PASS | PASS | no tracked generated or vendored paths |
| `EXEC-06` | Documented commands resolve | DEGRADER | automated | yes | PASS | PASS | 4 documented command(s) resolve |
| `EXEC-07` | No competing configs for one concern | DEGRADER | automated | yes | PASS | PASS | no concern configs found |

### TST

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `TST-01` | Test verb exists, suite non-empty | BLOCKER | automated | yes | PASS | PASS | test verb on build.gradle (./gradlew test); 20 test file(s) |
| `TST-02` | Suite status known | DEGRADER | automated | yes | UNKNOWN | UNKNOWN | probe not run: probes disabled (pass --run-gates) |
| `TST-03` | Skip / xfail census | DEGRADER | automated | yes | PASS | PASS | skip/xfail census across 20 test file(s): no markers; total 0 |
| `TST-04` | Fast path documented | DEGRADER | automated | yes | PASS | PASS | fast path documented: AGENTS.md:51 \`./gradlew test --tests '\*FieldLimitValidationTest' --tests '\*PrefixIndexLayoutStrategyTest' --tests '\*CurrentUserTest' --tests '\*InputSanitizerTest' --tests '\*XssSanitizationFilterTest'\` (scope: README.md, AGENTS.md, CLAUDE.md, CONTRIBUTING.md) |
| `TST-05` | Coverage configuration present | COSMETIC | automated | yes | PASS | PASS | coverage configured: build.gradle |

### NAV

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `NAV-01` | Declared component boundaries | DEGRADER | automated | yes | PASS | PASS | single-root repository declares build.gradle |
| `NAV-02` | Obvious entry points | DEGRADER | automated | yes | PASS | PASS | entry point(s): build.gradle |
| `NAV-03` | Structural red flags | COSMETIC | automated | yes | PASS | PASS | oversized(>524288 B): 0; catch-all dirs(>50 files): 0; binaries: 0; minified: 0 |
| `NAV-04` | Architecture orientation artifact | COSMETIC | automated | no | PASS | PASS | docs/architecture/; an ADR directory |
| `NAV-05` | Public-symbol documentation coverage | COSMETIC | automated | no | UNKNOWN | UNKNOWN | no recognised source language, so 'public' cannot be defined |

### SEC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `SEC-01` | .env untracked and ignored | BLOCKER | automated | yes | PASS | PASS | no tracked .env and ignore rules cover the shape |
| `SEC-02` | No key-shaped strings in tracked files | BLOCKER | automated | yes | FAIL | FAIL | 1 credential-shaped line(s) across 1 file(s) in tracked text files |
| `SEC-03` | Ignore patterns cover secret shapes | DEGRADER | automated | yes | PASS | PASS | covered: dotenv, dotenv variant, ssh private key, PEM, key, PKCS#12, PFX, keystore, certificate (.crt), certificate (.cer), AWS credentials |

### HYG

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `HYG-01` | README present and non-placeholder | COSMETIC | automated | no | FAIL | FAIL | README.md is a placeholder — contains the placeholder marker 'placeholder' |
| `HYG-02` | CODEOWNERS present | COSMETIC | automated | no | PASS | PASS | CODEOWNERS present at .github/CODEOWNERS |
| `HYG-03` | Pull-request template present | COSMETIC | automated | no | PASS | PASS | pull-request template present at .github/pull_request_template.md |
| `HYG-04` | Issue templates present | COSMETIC | automated | no | PASS | PASS | 2 issue template(s) present |
| `HYG-05` | CONTRIBUTING present | COSMETIC | automated | no | PASS | PASS | CONTRIBUTING present at CONTRIBUTING.md |
| `HYG-06` | SECURITY present | COSMETIC | automated | no | PASS | PASS | SECURITY present at SECURITY.md |
| `HYG-07` | CHANGELOG present and recent | COSMETIC | automated | no | FAIL | FAIL | no CHANGELOG document |
| `HYG-08` | Release process documented | COSMETIC | automated | no | PASS | PASS | release document present at docs/release-process.md |
| `HYG-09` | Dependency update automation configured | COSMETIC | automated | no | PASS | PASS | dependency automation configured at .github/dependabot.yml |
| `HYG-10` | License present | COSMETIC | automated | no | PASS | PASS | license present at LICENSE |
| `HYG-11` | Branch protection declared as code | COSMETIC | automated | no | PASS | PASS | branch protection declared as code at docs/branch-protection.md |

---

Rendered from the JSON report by `render_report.py`. The JSON report is the record.
