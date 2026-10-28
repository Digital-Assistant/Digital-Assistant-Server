# Agentic readiness audit: digital-assistant-server

> [!NOTE]
> This audit reports; it does not gate. A check that could not be settled is UNKNOWN: it earns no credit and is never shown as a pass.

## Provenance

| Field | Value |
|---|---|
| Target | `/home/kc/Projects/UDAN/Digital-Assistant-Server` |
| Commit | `4a48e1976e42` on `dev` — clean working tree |
| Status | final |
| Started | `2026-10-28T12:49:11+00:00` |
| Finalised | `2026-10-28T13:14:09+00:00` |
| Recipe | `brownfield-audit` 0.1.0, catalogue `0.2.2` |
| Engine | `python-agentic-audit` 0.1.0, ruleset `2026-10-19.2`, ruleset hash `970e6792f98eeeb5`, scanned `2026-10-28T12:49:15+00:00` |
| Framework revision | `b4bf27df7915` |
| Agent (self-reported) | `Claude Code` with `claude-sonnet-5-5` |
| Probes | none: no command of the target was run |
| Report schema | `0.1` |

## Readiness summary

- **Phase-1 score: 0.54**, over 57 scored checks
- **6 blockers remaining: 5 FAIL, 1 PARTIAL**
- **Unattested:** 5 requirements that no check settles, listed below

2 of the 57 scored checks are UNKNOWN. An UNKNOWN check earns no credit, so the score understates what is true of the repository and does not certify it. No agent finding is waiting for a person's decision.

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
| PASS | 30 | 1 | The requirement is met. |
| PARTIAL | 9 | 0.5 | Part of the requirement is met. |
| FAIL | 29 | 0 | The requirement is not met. |
| UNKNOWN | 3 | 0 | The evidence could not be gathered. It is never read as a pass. |
| NOT_APPLICABLE | 0 | outside the score | The check does not apply to this repository. |
| **Total** | **71** |  |  |

| Severity | Weight | Meaning | Scored checks | Not met |
|---|---:|---|---:|---:|
| BLOCKER | 3 | Agent work is impossible, unsafe or unverifiable. Fix before onboarding agents. | 16 | 6 |
| DEGRADER | 2 | Agent work is possible but unreliable or wasteful. | 37 | 23 |
| COSMETIC | 1 | Hygiene. Fix when convenient. | 4 | 3 |

## Command surface

The framework's seven verbs, resolved verb by verb on the target's runners — CMD-01's structured payload; CMD-02, AGT-05 and CI-02 read the same surface.

2 verbs of 7 verbs resolve. Missing: `format`, `format:check`, `lint`, `typecheck`, `security`.

| Verb | Resolved | Runner | Command |
|---|:---:|---|---|
| `format` | **no** | — | — |
| `format:check` | **no** | — | — |
| `lint` | **no** | — | — |
| `typecheck` | **no** | — | — |
| `test` | yes | `build.gradle` | `./gradlew test` |
| `check` | yes | `build.gradle` | `./gradlew check` |
| `security` | **no** | — | — |

## Credential-shaped lines

1 credential-shaped line across 1 file in tracked text files. The file-by-file detail is in the attention tables above; the report never records the value itself.

## Missing secret-shape ignore rules

The secret shapes SEC-01 and SEC-03 check for, and whether the repository's ignore rules cover each one. The suggested entry is paste-ready into `.gitignore` for a shape that is missing.

| Shape | Status | Suggested `.gitignore` entry |
|---|:---:|---|
| dotenv | covered | `.env` |
| dotenv variant | covered | `.env.*` |
| ssh private key | **missing** | `id_rsa*` |
| PEM | covered | `*.pem` |
| key | covered | `*.key` |
| PKCS#12 | covered | `*.p12` |
| PFX | **missing** | `*.pfx` |
| keystore | **missing** | `*.keystore` |
| certificate (.crt) | **missing** | `*.crt` |
| certificate (.cer) | **missing** | `*.cer` |
| AWS credentials | **missing** | `.aws/*` |

## Checks that need attention

Each row is a finding, or a check that has none. A check is listed when it is not met, was overridden, was judged by the agent, or raised a finding. Evidence is grouped by who captured it, and a quote or command output is under Evidence in full. The decision on a finding is one of these:

- **interactive**: Decided by the person, on this finding alone.
- **batch**: Decided by the person, as one of a group of findings that are not blockers.
- **policy**: Set by the confirmation policy, not by a person.

### Blocker checks

6 of the 16 scored BLOCKER checks are not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `CMD-01` Seven verbs resolvable on one runner<br>finding `F-001` | **FAIL** | An agent cannot run the standardized command surface literally because 5 of 7 framework verbs are not defined on any runner (format, format:check, lint, typecheck, security). | **tool** `build.gradle` | Define every verb (format, format:check, lint, typecheck, test, check, security) as a script/target so an agent never has to invent a command. | **Confirmed** (interactive)<br>Note: Owner decision (Stage 4) |
| `CMD-02` check composes the read-only verbs<br>finding `F-041`, severity DEGRADER | **PARTIAL**<br>engine: UNKNOWN | An agent cannot treat ./gradlew check as the pre-merge gate because it composes only test: no format-check or typecheck task exists (same root cause as the missing verbs in CMD-01).<br>**Overridden.** The engine's verdict was UNKNOWN. Reason: Engine returned UNKNOWN because check is a Gradle lifecycle task; the stack profile asks the agent to read what the applied plugins make check depend on. build.gradle applies java, io.quarkus and org.owasp.dependencycheck and registers no task, so check depends on test (and assemble-side verification) only. Rubric PARTIAL: one of the three concerns. | **agent** `build.gradle:1-5` (plugins applied; no tasks.register/task block anywhere in the file; quote in Evidence in full)<br>`build.gradle:1-5` (plugins java, io.quarkus, org.owasp.dependencycheck; no task registered)<br>**searches** `grep` for `tasks.register\|^task \|dependsOn\|finalizedBy` in `build.gradle`: no hits | After CMD-01 is fixed, make check depend on format:check and a typecheck task so it composes all three concerns.<br>Playbook `define-command-surface` · Effort S | **Confirmed** (batch) |
| `CON-02` Test directories protected<br>finding `F-002` | **FAIL** | An agent cannot trust that a failing test is not rewritten to pass because the prohibitions section of AGENTS.md does not name any test directory. | **tool** `AGENTS.md:40` (3. Architectural Patterns & Code Conventions) | Name src/test/ in a code span in the prohibitions section of AGENTS.md, so test edits are bounded and reviewed. | **Confirmed** (interactive)<br>Note: Owner decision (Stage 4) |
| `EXEC-02` Lockfile committed and consistent<br>finding `F-003` | **FAIL** | An agent cannot install identical dependencies on every run because the jvm project has no lockfile (gradle.lockfile), so a fresh install resolves whatever versions are current. | We did not find any evidence. | Generate the lockfile with the project's package manager and commit it. | **Confirmed** (interactive)<br>Note: Owner decision (Stage 4) |
| `EXEC-03` Non-interactive setup path<br>finding `F-004` | **FAIL** | An agent cannot prepare the environment non-interactively because no devcontainer, compose file, Dockerfile, setup target/script, or documented install command exists. | We did not find any evidence. | Add a committed setup path (a devcontainer, compose file, or a documented install command in README.md) that runs with no prompts. | **Confirmed** (interactive)<br>Note: Owner decision (Stage 4) |
| `EXEC-04` Env-var inventory complete<br>finding `F-005` | **FAIL** | An agent cannot know the CI variables the scripts need because AWS_ROLE_ARN, INSTANCE_ID, SSM_DOCUMENT (repository variables), IMAGE_TAR and CACHE_DIR (optional script inputs) are documented only in scripts/ci/README.md and script headers, not in a committed example file (SECONDS, also reported by the engine, is a bash builtin and is not a gap). | **agent** `scripts/ci/README.md:1` (deploy-test.sh row documents AWS_ROLE_ARN, INSTANCE_ID, SSM_DOCUMENT, AWS_REGION)<br>`scripts/ci/image.sh:5-6` (header comment documents IMAGE_TAR and CACHE_DIR)<br>`scripts/ci/deploy-test.sh:67` (SECONDS is a bash builtin used as a timer; quote in Evidence in full)<br>**searches** `glob` for `.env.example\|.env.sample\|.env.template\|env.example` in `.`: no hits | Add a committed example file (for example scripts/ci/env.example) listing the CI variables and the QUARKUS\_\* overrides the README documents.<br>Playbook `env-inventory-and-secrets` · Effort S | **Confirmed** (interactive)<br>Note: Owner decision (Stage 4) |
| `SEC-02` No key-shaped strings in tracked files<br>finding `F-006` | **PASS**<br>engine: FAIL | An agent cannot keep credentials out of the repository and its history because scripts/ci/smoke-test.sh contains 1 credential-shaped line(s).<br>**Overridden.** The engine's verdict was FAIL. Reason: Engine matched a fake Bearer token used to assert a 401 response; owner rejected F-006. | **tool** `scripts/ci/smoke-test.sh:55` (credential-shaped string)<br>**agent** `scripts/ci/smoke-test.sh:55` | Remove the value from the file and its history, load it from the environment, and rotate the credential. The report never records the value itself. | **Rejected** (interactive)<br>Note: Owner: sentinel token in 401 smoke test |

### Degrader checks

23 of the 37 scored DEGRADER checks are not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `IDX-03` Index freshness<br>finding `F-042`, severity COSMETIC | **PARTIAL**<br>engine: UNKNOWN | An agent cannot be sure the index matches HEAD because the index predates the HEAD commit by one commit (CI files only, no indexed symbols affected).<br>**Overridden.** The engine's verdict was UNKNOWN. Reason: Engine returned UNKNOWN (probes off, store has no revision). index_status gives indexed_at; detect_changes failed ('base_branch or HEAD is not a commit'), so freshness was derived from git log --since the index time and its --stat: 3 non-Java files, no indexed symbols affected. | **agent** `index_status project=home-kc-Projects-UDAN-Digital-Assistant-Server` through `codebase-memory-mcp` (status ready, indexed_at 2026-10-28T09:43:48Z, 1292 nodes, 3855 edges)<br>command `git log --since=2026-10-28T09:43:48Z --stat (read-only, run by the agent)` (exit 0; output in Evidence in full)<br>`index_status` through `codebase-memory-mcp` (indexed_at 2026-10-28T09:43:48Z, HEAD commit 4a48e19 at 11:27Z) | Re-run index_repository before agent work; the store records no revision, so freshness cannot be compared mechanically.<br>Playbook `register-index` · Effort S | **Confirmed** (batch) |
| `AGT-03` Documentation contract named<br>finding `F-007` | **PARTIAL** | An agent cannot know where permanent and scratch documentation live because AGENTS.md does not name docs/STYLE_GUIDE.md, artefacts/. | **tool** `AGENTS.md:107` (docs/) | Name docs/ as the canonical home for permanent knowledge, docs/STYLE_GUIDE.md as the authoring rules, and artefacts/ as off-limits scratch. | **Confirmed** (batch) |
| `AGT-04` Definition of done stated<br>finding `F-008` | **FAIL** | An agent cannot keep code and its documentation in step because no definition-of-done sentence ties a documentation trigger to the same change set. | We did not find any evidence. | State that a change altering a public interface, observable behaviour, architecture, a check's semantics or an ADR updates the affected docs and the ADR index in the same change set. | **Confirmed** (batch) |
| `AGT-05` Seven verbs named<br>finding `F-009` | **FAIL** | An agent cannot use the project's own commands instead of inventing them because not named: format:check, lint, typecheck; not resolved: format, format:check, lint, typecheck, security. | We did not find any evidence. | List all seven verbs in AGENTS.md and define each on one runner so the instruction and the manifest agree. | **Confirmed** (batch) |
| `AGT-07` Do-not-do / deny rules present<br>finding `F-010` | **PARTIAL** | An agent cannot rely on an explicit do-not list because AGENTS.md does not prohibit edits to tests, generated files. | **tool** `AGENTS.md:46` (the default branch)<br>`AGENTS.md:46` (secrets) | Add an explicit prohibition for each of: tests, generated files, the default branch and secrets. | **Confirmed** (batch) |
| `AGT-08` No competing instruction files, and the reach matrix is stated<br>finding `F-011` | **FAIL** | An agent cannot be sure that both files are read because CLAUDE.md shadows AGENTS.md for the harness that defines the pair, so only one of the two is ever loaded. | **tool** `CLAUDE.md`<br>`AGENTS.md` | Reconcile the two files, or delete the shadowed one — a file nobody reads still gets edited. | **Confirmed** (batch) |
| `AGT-08` No competing instruction files, and the reach matrix is stated<br>finding `F-012` | **FAIL** | An agent cannot know which instruction file is authoritative because 2 vendor-specific instruction files exist alongside the canonical one (CLAUDE.md, CONVENTIONS.md). | **tool** `CLAUDE.md`<br>`CONVENTIONS.md` | Keep AGENTS.md canonical; reduce each vendor file to a pointer at it, or delete it if the harness reads AGENTS.md natively. | **Confirmed** (batch) |
| `AGT-08` No competing instruction files, and the reach matrix is stated<br>finding `F-027`, severity COSMETIC | **FAIL** | An agent cannot assume every harness on the team sees the project rules because 21 of 27 known harnesses reach an instruction file; Amazon Q Developer, Continue, Google Gemini CLI, Qwen Code, Replit Agent, Void reach nothing. | **tool** `CLAUDE.md`<br>`AGENTS.md`<br>`CLAUDE.md`<br>`CONVENTIONS.md` | Either state the supported harnesses in AGENTS.md, or add the vendor pointer files for the harnesses in use. | **Confirmed** (batch) |
| `AGT-09` Branch and release boundary declared<br>finding `F-013` | **PARTIAL** | An agent cannot know which branch to work on and whether it may cut a release because the branch model is stated but nothing says whether an agent may tag or release. | **tool** `AGENTS.md:20` | State the branches that exist (for example main and dev) and whether an agent may tag or publish a release. | **Confirmed** (batch) |
| `AGT-10` Instruction indirection resolved<br>finding `F-014` | **FAIL** | An agent cannot trust that the referenced rules are actually loaded because the instruction file defers its content to CONVENTIONS.md and no harness configuration names that file, so loading depends on the agent choosing to follow the pointer. | **tool** `AGENTS.md` (deferral found: CONVENTIONS.md) | Inline the rules that must always apply, or name the referenced file in each harness's config (aider read:, opencode.json instructions, Codex model_instructions_file). | **Confirmed** (batch) |
| `DOC-01` docs/ exists<br>finding `F-019` | **FAIL** | An agent cannot find the project's documentation because no docs/ directory exists in the repository. | We did not find any evidence. | Create docs/ as the canonical home for permanent knowledge, with docs/STYLE_GUIDE.md holding the authoring rules. | **Confirmed** (batch) |
| `DOC-02` docs/STYLE_GUIDE.md exists<br>finding `F-020` | **FAIL** | An agent cannot find the canonical authoring rules for documentation because docs/STYLE_GUIDE.md does not exist. | We did not find any evidence. | Create docs/STYLE_GUIDE.md and name it from the instruction file so writers have one set of authoring rules. | **Confirmed** (batch) |
| `TOOL-01` Formatter configured<br>finding `F-022` | **FAIL** | An agent cannot format a change set the same way every time because no formatter is configured and no format verb is defined. | **tool** `build.gradle` | Add a formatter config and a format write verb (prettier, black, ruff format, …). | **Confirmed** (batch) |
| `TOOL-02` Linter configured<br>finding `F-023` | **FAIL** | An agent cannot catch lint regressions before they land because no linter is configured and no lint verb is defined. | **tool** `build.gradle` | Add a linter config and a lint read-only verb (eslint, ruff, flake8, golangci-lint, …). | **Confirmed** (batch) |
| `TOOL-03` Type checker configured | **UNKNOWN** | no recognised strict type-check configuration and no ecosystem this pack knows one for, so type-checking cannot be judged | We did not find any evidence. | — | — |
| `TOOL-04` Dependency / security scan verb<br>finding `F-024` | **FAIL** | An agent cannot know whether a dependency carries a known vulnerability because no resolvable verb runs a dependency-audit check. | **tool** `build.gradle` | Add a dependency-audit verb (npm audit, pip-audit, osv-scanner, cargo audit, govulncheck, snyk). | **Confirmed** (batch) |
| `TOOL-05` Local hook chain active<br>finding `F-025` | **FAIL** | An agent cannot rely on local hooks to run before a commit because no hook manager is configured. | **tool** `build.gradle` | Adopt a hook manager (pre-commit, lefthook, husky) and wire it so checks run before a commit rather than only in CI. | **Confirmed** (batch) |
| `BASE-01` Baseline artifact exists<br>finding `F-015` | **FAIL** | An agent cannot distinguish pre-existing debt from new debt because no baseline or suppression artifact is committed. | We did not find any evidence. | Commit a baseline/suppression artifact, or document the equivalent mechanism in \`AGENTS.md\`. | **Confirmed** (batch) |
| `BASE-02` Current violation counts measured<br>finding `F-016` | **PARTIAL** | An agent cannot state how many violations exist today because no violation-counting tooling, budget or artifact is configured. | — | Add linters/type-checkers/security scanners and commit the count they produce, so debt is measured rather than guessed. | **Confirmed** (batch) |
| `BASE-03` No-new-violations mechanism<br>finding `F-017` | **PARTIAL** | An agent cannot stop the violation count from growing because no baseline, budget or comparison step exists, so the count is not guarded. | — | Once a baseline exists, wire a comparison step (the audit's own \`--baseline REPORT.JSON\` is the reference pattern) so new findings fail the build. | **Confirmed** (batch) |
| `BASE-04` Coverage floor configured<br>finding `F-018` | **FAIL** | An agent cannot know when coverage drops below an agreed floor because no coverage threshold is configured in the test runner. | We did not find any evidence. | Configure a coverage floor for the test runner so a drop fails the gate. | **Confirmed** (batch) |
| `TST-02` Suite status known | **UNKNOWN** | probe not run: probes disabled (pass --run-gates) | We did not find any evidence. | — | — |
| `TST-04` Fast path documented<br>finding `F-026` | **PASS**<br>engine: FAIL | An agent cannot run one test or a subset without the whole suite because none of the entry documents shows a single-test or subset invocation (scope: README.md, AGENTS.md, CLAUDE.md, CONTRIBUTING.md).<br>**Overridden.** The engine's verdict was FAIL. Reason: Engine looked for a fenced block; the rubric accepts an inline-code single-test command. Owner rejected F-026. | **tool** `README.md`<br>`AGENTS.md`<br>`CLAUDE.md`<br>`CONTRIBUTING.md`<br>**agent** `AGENTS.md:32` | Document a fast path (for example \`pytest -k \<name>\`, \`npm test -- \<name>\`, or \`go test -run \<name>\`) in a fenced shell block. | **Rejected** (batch) |
| `SEC-03` Ignore patterns cover secret shapes<br>finding `F-021` | **PARTIAL** | An agent cannot trust that secret shapes cannot be committed because ignore rules do not cover ssh private key, PFX, keystore, certificate (.crt), certificate (.cer), AWS credentials. | **tool** `.gitignore` (.project)<br>`.gitignore` (.classpath)<br>`.gitignore` (.settings)<br>`.gitignore` (bin)<br>`.gitignore` (.idea)<br>`.gitignore` (\*.ipr)<br>`.gitignore` (\*.iml)<br>`.gitignore` (\*.iws) | Add the missing shapes to .gitignore — for example \`.env.\*\`, \`\*.pem\`, \`\*.key\`, \`\*.p12\`, \`\*.pfx\`, \`\*.keystore\`, \`id_rsa\*\`, \`\*.crt\`, \`\*.cer\`, \`.aws/\*\`. | **Confirmed** (batch) |
| `AGT-11` Instructions are specific and not inferable<br>finding `F-039` · agent, confidence medium | **FAIL** | An agent cannot rely on AGENTS.md being specific because about 62% of its rule lines are generic (section 6 role-play prompts, persona lines) and its EntityManager rule is contradicted by the code in several services. | **agent** `AGENTS.md:74-143` (section 6 generic prompts and profile tables)<br>`AGENTS.md:44` (EntityManager rule)<br>`src/main/java/com/nistapp/uda/index/services/Votes.java:28` (EntityManager injected in a service) | Cut or move section 6 prompts out of AGENTS.md, keep the verified commands and boundary rules, and either fix the EntityManager rule or mark it as target state with the known exceptions.<br>Playbook `adapt-agents-md` · Effort S | **Confirmed** (batch) |
| `CI-04` CI enforces the same gate as the local check<br>finding `F-040` · agent, confidence medium | **PARTIAL** | An agent cannot trust a green CI because the dependency scan job is a placeholder that always passes, the image scan is continue-on-error, and no committed evidence shows the CI Gate is a required status check. | **agent** `.github/workflows/ci.yml:94` (scan job runs a script that only prints skipped)<br>`.github/workflows/image.yml:173` (continue-on-error: true on the image scan) | Make the scan step real or drop it from the gate, make the image scan fail on fixable HIGH findings, and record branch protection as code or in a runbook the audit can cite.<br>Playbook `add-ci-parity` · Effort S | **Confirmed** (batch) |

### Cosmetic checks

3 of the 4 scored COSMETIC checks are not met.

| Check | Verdict | What we found | Evidence | Fix | Decision |
|---|---|---|---|---|---|
| `DOC-04` ADR directory and index<br>finding `F-028` | **FAIL** | An agent cannot find the decisions of record because no ADR directory (docs/architecture/adrs/ or docs/adr/) exists. | We did not find any evidence. | Add docs/architecture/adrs/ with NNNN-short-title.md files and a README index that lists them. | **Confirmed** (batch) |
| `TOOL-06` Commit convention enforced<br>finding `F-037` | **FAIL** | An agent cannot rely on a consistent commit-message convention because no commitlint config, commit-msg hook, or equivalent exists. | **tool** `build.gradle` | Add a commitlint config or a commit-msg hook so commit messages follow one convention. | **Confirmed** (batch) |
| `TST-05` Coverage configuration present<br>finding `F-038` | **FAIL** | An agent cannot see how much of the code the suite actually covers because no coverage configuration or coverage-invoking command is present. | We did not find any evidence. | Configure coverage for the test runner (a \`coverage\` block, \`.coveragerc\`/\`.nycrc\`, \`--coverage\`/\`--cov\`, or \`go test -cover\`). | **Confirmed** (batch) |
| `NAV-04` Architecture orientation artifact<br>finding `F-036` | **FAIL**<br>not scored | An agent cannot orient itself from a map instead of exploring blindly because no docs/architecture/, ADR directory or diagram exists. | We did not find any evidence. | Add a short architecture page (with a diagram) so an agent can orient before it explores. | **Confirmed** (batch) |
| `NAV-05` Public-symbol documentation coverage | **UNKNOWN**<br>not scored | no recognised source language, so 'public' cannot be defined | We did not find any evidence. | — | — |
| `HYG-02` CODEOWNERS present<br>finding `F-029` | **FAIL**<br>not scored | An agent cannot know who owns a path before changing it because no CODEOWNERS file exists at the root, under .github/, or under docs/. | We did not find any evidence. | Add .github/CODEOWNERS mapping paths to owners so review routing is machine-readable. | **Confirmed** (batch) |
| `HYG-03` Pull-request template present<br>finding `F-030` | **FAIL**<br>not scored | An agent cannot file a change with the information reviewers need because no .github/pull_request_template file exists. | We did not find any evidence. | Add .github/pull_request_template.md describing what a good change records. | **Confirmed** (batch) |
| `HYG-04` Issue templates present<br>finding `F-031` | **FAIL**<br>not scored | An agent cannot report a bug or request in a structured way because no .github/ISSUE_TEMPLATE entry exists. | We did not find any evidence. | Add at least one file under .github/ISSUE_TEMPLATE/ (a bug report and a feature request are a good start). | **Confirmed** (batch) |
| `HYG-06` SECURITY present<br>finding `F-032` | **FAIL**<br>not scored | An agent cannot report a vulnerability responsibly because no SECURITY document exists at the root, under .github/, or under docs/. | We did not find any evidence. | Add SECURITY.md with a private disclosure channel and a response expectation. | **Confirmed** (batch) |
| `HYG-07` CHANGELOG present and recent<br>finding `F-033` | **FAIL**<br>not scored | An agent cannot see what changed between two versions because no CHANGELOG (or CHANGES/HISTORY) document exists. | We did not find any evidence. | Add CHANGELOG.md and record notable changes under a version heading. | **Confirmed** (batch) |
| `HYG-08` Release process documented<br>finding `F-034` | **FAIL**<br>not scored | An agent cannot know how a release is cut because no text or markdown document describing the release process exists. | We did not find any evidence. | Add docs/releasing.md (or RELEASE.md) describing how a version is cut and published. | **Confirmed** (batch) |
| `HYG-11` Branch protection declared as code<br>finding `F-035` | **FAIL**<br>not scored | An agent cannot reproduce the protected-branch rules as code because the github provider is detected but no branch-protection configuration exists. | We did not find any evidence. | Declare branch protection in code (a Terraform resource or .github/branch-protection) so it is reviewed like any other change. | **Confirmed** (batch) |

## Overrides of the engine's verdicts

Where this report's verdict differs from the engine's, the row of the check above shows both, and the agent's counter-evidence is in its Evidence column or under Evidence in full.

| Check | Engine's verdict | This report's verdict | Reason |
|---|---|---|---|
| `IDX-03` | UNKNOWN | PARTIAL | Engine returned UNKNOWN (probes off, store has no revision). index_status gives indexed_at; detect_changes failed ('base_branch or HEAD is not a commit'), so freshness was derived from git log --since the index time and its --stat: 3 non-Java files, no indexed symbols affected. |
| `CMD-02` | UNKNOWN | PARTIAL | Engine returned UNKNOWN because check is a Gradle lifecycle task; the stack profile asks the agent to read what the applied plugins make check depend on. build.gradle applies java, io.quarkus and org.owasp.dependencycheck and registers no task, so check depends on test (and assemble-side verification) only. Rubric PARTIAL: one of the three concerns. |
| `TST-04` | FAIL | PASS | Engine looked for a fenced block; the rubric accepts an inline-code single-test command. Owner rejected F-026. |
| `SEC-02` | FAIL | PASS | Engine matched a fake Bearer token used to assert a 401 response; owner rejected F-006. |

## Evidence in full

Quotes and command output that the tables above point to, each under the check that cites it.

### CMD-02: check composes the read-only verbs

- `build.gradle:1-5` — plugins applied; no tasks.register/task block anywhere in the file · captured by agent
  ```text
  id 'java'  id 'io.quarkus' version '3.2.10.Final'  id "org.owasp.dependencycheck" version "9.0.9"
  ```

### EXEC-04: Env-var inventory complete

- `scripts/ci/deploy-test.sh:67` — SECONDS is a bash builtin used as a timer · captured by agent
  ```text
  started=${SECONDS}
  ```

### IDX-03: Index freshness

- Command `git log --since=2026-10-28T09:43:48Z --stat (read-only, run by the agent)` (exit 0) · captured by agent
  ```text
  4a48e19 fix(ci): ... (#98): .github/workflows/deploy-test.yml, scripts/ci/README.md, scripts/ci/deploy-test.sh
  ```

### AGT-11: Instructions are specific and not inferable

- `AGENTS.md:44` — specific but NOT observably true: says never inject EntityManager in a Resource or Service, yet services/Votes.java:28 injects it (also SearchList, StatusService, SequenceListService, SearchWithID) · captured by agent
  ```text
  Never inject or use the `EntityManager` directly in a Resource or Service class
  ```
- `src/main/java/com/nistapp/uda/index/services/Votes.java:28` — EntityManager em field in a service/resource class · captured by agent
  ```text
  EntityManager em;
  ```

### CI-04: CI enforces the same gate as the local check

- `.github/workflows/ci.yml:94` — scan job runs scripts/ci/scan.sh, which prints that the scan is skipped and exits 0 (scripts/ci/scan.sh) · captured by agent
  ```text
  run: scripts/ci/scan.sh
  ```
- `.github/workflows/image.yml:173` — Scan image and write SBOM step is non-gating · captured by agent
  ```text
  continue-on-error: true
  ```
- `.github/workflows/ci.yml:172` — comment says CI Gate is the single required status check for branch protection; platform settings are not in the repo and were not read · captured by agent
  ```text
  Single required status check for branch protection
  ```

## Coverage

**Examined:** nothing is recorded

No scope was skipped.

**Files:** 125 files inspected, 2 skipped; the scan was not cut short.

## Stack and components

| Stack | Detected |
|---|---|
| Ecosystems | `jvm` |
| Package managers | — |
| CI providers | `github` |
| Test frameworks | `junit` |
| Hook managers | — |

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
| Stack profile | — |

## Appendix — all checks

All 71 checks, grouped by family. The command surface, the credential-shaped-lines total and the missing secret-shape ignore rules above are rendered from the engine's structured payloads; other payloads (env-key inventories, harness-reach matrices, and so on) are in the JSON report only, for now.

### IDX

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `IDX-01` | codebase-memory-mcp registered for this repository | BLOCKER | automated | yes | PASS | PASS | codebase-memory-mcp registered in: /home/kc/.config/opencode/opencode.jsonc (user) |
| `IDX-02` | An index exists for this checkout | BLOCKER | automated | yes | PASS | PASS | index store /home/kc/.cache/codebase-memory-mcp contains a project rooted at /home/kc/Projects/UDAN/Digital-Assistant-Server |
| `IDX-03` | Index freshness | DEGRADER | automated | yes | PARTIAL | UNKNOWN | Index built 2026-10-28T09:43:48Z; HEAD 4a48e19 (committed 11:27Z) is the one later commit, touching only a workflow, a CI script and a CI README. |

### AGT

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `AGT-01` | Instruction file present, canonical, and non-trivial | BLOCKER | automated | yes | PASS | PASS | — |
| `AGT-02` | Component coverage by instruction files | DEGRADER | automated | yes | PASS | PASS | single-root repository: no component-level instruction files required |
| `AGT-03` | Documentation contract named | DEGRADER | automated | yes | PARTIAL | PARTIAL | does not name docs/STYLE_GUIDE.md, artefacts/ |
| `AGT-04` | Definition of done stated | DEGRADER | automated | yes | FAIL | FAIL | no definition-of-done sentence ties a documentation trigger to the same change set |
| `AGT-05` | Seven verbs named | DEGRADER | automated | yes | FAIL | FAIL | not named: format:check, lint, typecheck; not resolved: format, format:check, lint, typecheck, security |
| `AGT-06` | Architectural boundaries declared | DEGRADER | automated | yes | PASS | PASS | 3 boundary rules reference real paths |
| `AGT-07` | Do-not-do / deny rules present | DEGRADER | automated | yes | PARTIAL | PARTIAL | not covered: tests, generated files |
| `AGT-08` | No competing instruction files, and the reach matrix is stated | DEGRADER | automated | yes | FAIL | FAIL | 22/28 harnesses reached |
| `AGT-09` | Branch and release boundary declared | DEGRADER | automated | yes | PARTIAL | PARTIAL | the branch model is stated but nothing says whether an agent may tag or release |
| `AGT-10` | Instruction indirection resolved | DEGRADER | automated | yes | FAIL | FAIL | — |
| `AGT-11` | Instructions are specific and not inferable | DEGRADER | agent | yes | FAIL | — | About 29 of 77 rule lines (38%) in AGENTS.md are specific; section 6 (31 lines of generic role-play prompts and profile tables) and section 1 persona lines are generic, and the EntityManager rule does not hold in the code. |

### DOC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `DOC-01` | docs/ exists | DEGRADER | automated | yes | FAIL | FAIL | no docs/ directory |
| `DOC-02` | docs/STYLE_GUIDE.md exists | DEGRADER | automated | yes | FAIL | FAIL | docs/STYLE_GUIDE.md is not present |
| `DOC-03` | artefacts/ exists and is ignored | DEGRADER | automated | yes | PASS | PASS | no scratch directory in use |
| `DOC-04` | ADR directory and index | COSMETIC | automated | yes | FAIL | FAIL | no ADR directory |

### CMD

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CMD-01` | Seven verbs resolvable on one runner | BLOCKER | automated | yes | FAIL | FAIL | format -> missing; format:check -> missing; lint -> missing; typecheck -> missing; test -> build.gradle; check -> build.gradle; security -> missing |
| `CMD-02` | check composes the read-only verbs | BLOCKER | automated | yes | PARTIAL | UNKNOWN | Gradle lifecycle check composes only test (plugins java and io.quarkus); no format-check or typecheck concern exists to compose. |
| `CMD-03` | No divergent second definition | DEGRADER | automated | yes | PASS | PASS | no runner defines check, so there is nothing to diverge |
| `CMD-04` | Write verbs separated from read-only verbs | DEGRADER | automated | yes | PASS | PASS | no read-only verb invokes a write verb |

### TOOL

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `TOOL-01` | Formatter configured | DEGRADER | automated | yes | FAIL | FAIL | no formatter config and no format verb |
| `TOOL-02` | Linter configured | DEGRADER | automated | yes | FAIL | FAIL | no linter config and no lint verb |
| `TOOL-03` | Type checker configured | DEGRADER | automated | yes | UNKNOWN | UNKNOWN | no recognised strict type-check configuration and no ecosystem this pack knows one for, so type-checking cannot be judged |
| `TOOL-04` | Dependency / security scan verb | DEGRADER | automated | yes | FAIL | FAIL | no verb runs a dependency audit |
| `TOOL-05` | Local hook chain active | DEGRADER | automated | yes | FAIL | FAIL | no hook chain configured |
| `TOOL-06` | Commit convention enforced | COSMETIC | automated | yes | FAIL | FAIL | no commit-message rule |

### CI

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CI-01` | Pipeline present, triggered on PRs | BLOCKER | automated | yes | PASS | PASS | PR-triggered: .github/workflows/ci.yml (github); .github/workflows/image.yml (github); .github/workflows/pr-title.yml (github) |
| `CI-02` | Local↔CI parity | BLOCKER | automated | yes | PASS | PASS | Engine saw no verb in workflows because CI calls scripts/ci/\*.sh; those scripts run ./gradlew clean build -x test and ./gradlew test, the same test verb that resolves locally. |
| `CI-03` | CI-only steps flagged | DEGRADER | automated | yes | PASS | PASS | no CI-only steps |
| `CI-04` | CI enforces the same gate as the local check | DEGRADER | agent | yes | PARTIAL | — | Jobs build-test, secrets, codeql, workflow-lint gate the PR (no continue-on-error or swallowed exit); the scan job is a no-op that always passes; the image scan is continue-on-error; branch-protection wiring of the CI Gate cannot be confirmed from the repo. |

### BASE

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `BASE-01` | Baseline artifact exists | DEGRADER | automated | yes | FAIL | FAIL | no baseline or suppression artifact |
| `BASE-02` | Current violation counts measured | DEGRADER | automated | yes | PARTIAL | PARTIAL | no violation-count mechanism found; method: static configuration scan; the target's own tools are never run |
| `BASE-03` | No-new-violations mechanism | DEGRADER | automated | yes | PARTIAL | PARTIAL | no ratchet and no baseline yet, so there is nothing to compare against |
| `BASE-04` | Coverage floor configured | DEGRADER | automated | yes | FAIL | FAIL | no coverage threshold configured |

### CON

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `CON-01` | Allow/deny artifact present | BLOCKER | automated | yes | PASS | PASS | AGENTS.md section 4 (Security & Safety Guardrails) names .github/workflows/ and .env as off limits; .github/workflows/ exists. The engine credited section 3 (architecture), which is not a prohibitions section. |
| `CON-02` | Test directories protected | BLOCKER | automated | yes | FAIL | FAIL | the prohibitions section names none of the test directories (src/test/) |
| `CON-03` | artefacts/ excluded from index and agent reads | DEGRADER | automated | yes | PASS | PASS | no scratch or agent-state directory in use |
| `CON-04` | Draft allow/deny emitted | COSMETIC | automated | no | PASS | PASS | draft deny list written: do-not-do.draft.md |

### EXEC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `EXEC-01` | Toolchain pinned / wrapper committed | BLOCKER | automated | yes | PASS | PASS | pinned: jvm (gradlew) |
| `EXEC-02` | Lockfile committed and consistent | BLOCKER | automated | yes | FAIL | FAIL | — |
| `EXEC-03` | Non-interactive setup path | BLOCKER | automated | yes | FAIL | FAIL | no setup path found |
| `EXEC-04` | Env-var inventory complete | BLOCKER | automated | yes | FAIL | FAIL | 6 referenced key(s) are absent from any committed example file: AWS_ROLE_ARN, CACHE_DIR, IMAGE_TAR, INSTANCE_ID, SECONDS, SSM_DOCUMENT |
| `EXEC-05` | No generated artifacts committed in-tree | DEGRADER | automated | yes | PASS | PASS | no tracked generated or vendored paths |
| `EXEC-06` | Documented commands resolve | DEGRADER | automated | yes | PASS | PASS | no fenced shell commands documented |
| `EXEC-07` | No competing configs for one concern | DEGRADER | automated | yes | PASS | PASS | no concern configs found |

### TST

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `TST-01` | Test verb exists, suite non-empty | BLOCKER | automated | yes | PASS | PASS | test verb on build.gradle (./gradlew test); 14 test file(s) |
| `TST-02` | Suite status known | DEGRADER | automated | yes | UNKNOWN | UNKNOWN | probe not run: probes disabled (pass --run-gates) |
| `TST-03` | Skip / xfail census | DEGRADER | automated | yes | PASS | PASS | skip/xfail census across 14 test file(s): no markers; total 0 |
| `TST-04` | Fast path documented | DEGRADER | automated | yes | PASS | FAIL | AGENTS.md shows a single-test invocation as inline code: ./gradlew test --tests with a class name. |
| `TST-05` | Coverage configuration present | COSMETIC | automated | yes | FAIL | FAIL | no coverage configuration |

### NAV

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `NAV-01` | Declared component boundaries | DEGRADER | automated | yes | PASS | PASS | single-root repository declares build.gradle |
| `NAV-02` | Obvious entry points | DEGRADER | automated | yes | PASS | PASS | entry point(s): build.gradle |
| `NAV-03` | Structural red flags | COSMETIC | automated | yes | PASS | PASS | oversized(>524288 B): 0; catch-all dirs(>50 files): 0; binaries: 0; minified: 0 |
| `NAV-04` | Architecture orientation artifact | COSMETIC | automated | no | FAIL | FAIL | no architecture orientation artifact |
| `NAV-05` | Public-symbol documentation coverage | COSMETIC | automated | no | UNKNOWN | UNKNOWN | no recognised source language, so 'public' cannot be defined |

### SEC

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `SEC-01` | .env untracked and ignored | BLOCKER | automated | yes | PASS | PASS | no tracked .env and ignore rules cover the shape |
| `SEC-02` | No key-shaped strings in tracked files | BLOCKER | automated | yes | PASS | FAIL | The flagged line is a deliberately invalid sentinel token in a 401 smoke test; no real credential is tracked. |
| `SEC-03` | Ignore patterns cover secret shapes | DEGRADER | automated | yes | PARTIAL | PARTIAL | covered: dotenv, dotenv variant, PEM, key, PKCS#12; missing: ssh private key, PFX, keystore, certificate (.crt), certificate (.cer), AWS credentials |

### HYG

| Check | Title | Severity | Method | Scored | Verdict | Engine | Summary |
|---|---|---|---|---|---|---|---|
| `HYG-01` | README present and non-placeholder | COSMETIC | automated | no | PASS | PASS | README present and non-placeholder |
| `HYG-02` | CODEOWNERS present | COSMETIC | automated | no | FAIL | FAIL | no CODEOWNERS file |
| `HYG-03` | Pull-request template present | COSMETIC | automated | no | FAIL | FAIL | no pull-request template |
| `HYG-04` | Issue templates present | COSMETIC | automated | no | FAIL | FAIL | no issue templates |
| `HYG-05` | CONTRIBUTING present | COSMETIC | automated | no | PASS | PASS | CONTRIBUTING present at CONTRIBUTING.md |
| `HYG-06` | SECURITY present | COSMETIC | automated | no | FAIL | FAIL | no SECURITY document |
| `HYG-07` | CHANGELOG present and recent | COSMETIC | automated | no | FAIL | FAIL | no CHANGELOG document |
| `HYG-08` | Release process documented | COSMETIC | automated | no | FAIL | FAIL | no release document |
| `HYG-09` | Dependency update automation configured | COSMETIC | automated | no | PASS | PASS | dependency automation configured at .github/dependabot.yml |
| `HYG-10` | License present | COSMETIC | automated | no | PASS | PASS | license present at LICENSE |
| `HYG-11` | Branch protection declared as code | COSMETIC | automated | no | FAIL | FAIL | github offers branch protection as code, none found |

---

Rendered from the JSON report by `render_report.py`. The JSON report is the record.
