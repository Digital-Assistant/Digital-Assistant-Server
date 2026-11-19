# `AGENTS.md` - Operating Manual for AI Contributors

This document is the Operating Manual for AI and agentic coding systems contributing to this repository. It provides the necessary context, commands, patterns, and safety guardrails. All agent-driven contributions are expected to adhere to the standards outlined here.

This file is read by Claude Code (through the `CLAUDE.md` import), opencode, Codex and Cursor.

## 1. High-Level Directive

*   **Golden Rule:** Your primary directive is to **first read and understand the existing code and the project conventions below** before writing new code. Adhere to existing patterns to ensure consistency.
*   **Problem-Solving Approach:** A task may be ambiguous. Ask before you start when the answer changes what you build, or when the change touches a guarded area (section 4). Otherwise take the safest reading, say so, and list the open questions in your summary. Do not invent new requirements.
*   **Strict Scope Adherence:** Your work must be strictly confined to the files and requirements of the assigned task: its GitHub issue, or the task as given when there is none ([`CONTRIBUTING.md`, "With or without an issue"](CONTRIBUTING.md#pull-request-process)). Do not modify unrelated files or refactor code that is outside the explicit scope of the task.

## 2. Command & Tooling Reference

This is the definitive list of commands required to build, test, and validate work in this project. All commands should be run from the repository root unless otherwise specified.

#### **Environment Setup**
This repo is only the Quarkus server (Gradle, Java, port **11081**). MariaDB, Elasticsearch, Keycloak, Postgres and Nginx live in the sibling repo `Digital_Assistant_microServices`.
*   **Start infrastructure (in the microServices repo):** `./local-up.sh`; stop with `./local-down.sh`
*   **Sync credentials into `src/main/resources/local-secrets.properties` (untracked):** `./sync-credentials.sh`
*   **Check infrastructure health:** `./pre-flight-check.sh`
*   **Start the server in dev mode (runs the pre-flight check first):** `./start-server.sh` (wraps `./gradlew quarkusDev --no-daemon`)
*   **View infrastructure logs:** `docker logs -f <container>` (e.g. `keycloak-local`, `mariadb-local`)

#### **Build & Validation**
Setup is in `README.md`, "Setup (non-interactive)". The verbs are `format`, `format:check` (`formatCheck`), `lint`,
`typecheck`, `test`, `check` and `security`, all on `./gradlew`. What each one does and needs, and the pre-merge gate,
are in [`CONTRIBUTING.md`, "Commands"](CONTRIBUTING.md#commands). The local hooks are in
[`CONTRIBUTING.md`, "Local hooks"](CONTRIBUTING.md#local-hooks).

#### **Baselines and floors**
Existing debt is counted and cannot grow. `./gradlew formatCheck` runs `scripts/ci/check-baseline.sh` (no new violations: it fails when a count goes up) against the quality baseline `docs/agentic-readiness/digital-assistant-server-quality-baseline.json`. The baselines are: the PMD exclude list in `config/pmd/ruleset.xml`, the Spotless `ratchetFrom 'origin/dev'` (changed files only) with the full-tree count in the baseline file, the OSV-Scanner advisory count (compared by `scripts/ci/scan.sh` in the CI scan job), the image-scan count and the skip-marker count (0), all in the baseline file. Coverage floor: 70% line coverage, enforced by `jacocoTestCoverageVerification` in `build.gradle` and run by the CI `Coverage floor` step. **A baseline only goes down and a coverage floor only goes up; raising a count or lowering the floor needs the owner.** Lower a count (or raise the floor) in the baseline file and `build.gradle` in the same change that earns it.

#### **Testing**
*   **Run all tests:** `./gradlew test`
*   **Run a single test class:** `./gradlew test --tests 'com.nistapp.uda.index.StatusResourceTest'`
*   Tests live in `src/test/java/com/nistapp/uda/index/` (Quarkus JUnit5 + RestAssured).
*   No known failures; the CI test step is blocking. Tests need MariaDB and Elasticsearch on `127.0.0.1`.
*   **Unit-test subset (no stack needed):** 5 of the 20 test classes are not `@QuarkusTest` (`FieldLimitValidationTest`, `PrefixIndexLayoutStrategyTest`, `CurrentUserTest`, `InputSanitizerTest`, `XssSanitizationFilterTest`). The other 15 need the live MariaDB and Elasticsearch from the local stack; there is no Testcontainers profile (recorded as debt):
    ```bash
    ./gradlew test --tests '*FieldLimitValidationTest' --tests '*PrefixIndexLayoutStrategyTest' --tests '*CurrentUserTest' --tests '*InputSanitizerTest' --tests '*XssSanitizationFilterTest'
    ```
*   **Last full run (2026-11-06, local stack):** 167 tests, 0 failures, 0 skipped; no `@Disabled`/`@Ignore` markers.

#### **Dependency Management**
*   **Adding a Java dependency:** edit `build.gradle`. Versions come from the Quarkus BOM (`quarkusVersion` in `gradle.properties`); Flyway is pinned to 9.22.3 in `build.gradle` for MariaDB 10.11 support.

## 3. Architectural Patterns & Code Conventions

The rules for code are the same for people and agents. They live outside this file:

*   **How to write code:** [`CONVENTIONS.md`](CONVENTIONS.md) covers naming, formatting, logging, errors, API design,
    data access, configuration and input safety. Its rules are mandatory for new code.
*   **Why the code is shaped this way:** see the decision records in [`docs/adr/`](docs/adr/README.md). Examples are
    the tenant header (ADR 0002) and data access through repositories (ADR 0003).
*   **Where things are:** [`docs/architecture/overview.md`](docs/architecture/overview.md) maps the packages, entry
    points, data stores and the tenant flow.
*   **Configuration sources and keys:** [`README.md`, "Configuration"](README.md#configuration).

## How to write for people

This covers chat replies, status reports, pull request descriptions and commit messages. Rules for pages under
`docs/` are in `docs/STYLE_GUIDE.md`.

* **Reader:** write for a new engineer who knows Git and Java but not this repository or its terms.
* **Bottom line first:** open with the answer, the result or the decision you need.
* **Plain language:** short sentences (at most 25 words), active voice, one term per meaning.
* **Reports:** a status report says what is done, not done, blocked and next. A done report has three headings:
  what changed, how it was checked, what was not done. Mark each claim as checked or inferred.
* **Decisions:** give the context, two to four options with pros and cons, your recommendation and the default.

## 4. Security & Safety Guardrails

These are strict, non-negotiable rules.

*   **Forbidden Files & Directories:** You **must not** modify any files in the `.github/workflows/`, `.env`, or any security-related configuration files unless the task explicitly instructs you to do so.
*   **Secrets Management:** You **must not** hardcode a password, API key or token.
    Follow [CONVENTIONS.md §10](CONVENTIONS.md#10-configuration).
*   **Authentication & Authorization:** You **must not** alter or add any logic related to user authentication or role-based access control.
*   **Dependency Vetting:** You **must not** introduce new third-party libraries or dependencies unless it is an explicit requirement of the assigned task.
*   **Recorded Decisions:** You **must not** change behaviour that a decision record in `docs/adr/` describes, unless
    the task says so. An example is the `UDAN-Realm` tenant selection in `config/CustomTenantResolver.java`. Such a change
    updates or adds the decision record in the same change set.
*   **Tests:** You **must not** edit, skip or delete a test in `src/test/` to make a failing test pass; change a test only together with the code change that needs it.
*   **Generated Output & Scratch:** You **must not** edit generated output by hand (`build/`, `.gradle/`); regenerate it with `./gradlew build`. `artefacts/` is scratch: never commit it, never treat it as a source of truth.
*   **Secret Stores:** You **must not** open, print or commit a secret store (`.env`, `*.jks`, `local-secrets.properties`).

## 5. Development & Contribution Workflow

All contributions must follow this comprehensive, sequential lifecycle:

1.  **Branching:** Create a branch from `dev`, named as [`CONTRIBUTING.md`, "Development Workflow"](CONTRIBUTING.md#development-workflow) says.
2.  **Initial Implementation:** Write the initial, functionally correct code that satisfies the primary objective of the assigned task.
3.  **Identify Workflow Profile:** Read the `workflow:` label on the assigned issue (e.g., `workflow:core`, `workflow:infra`). Without an issue, pick the profile as `CONTRIBUTING.md` says. The profile determines the required quality assurance steps.
4.  **Execute Refinement Loop:** Based on the identified profile, apply the **mandatory** "Quality Assurance & Refinement Prompts" from the corresponding profile in [`docs/workflow-profiles.md`](docs/workflow-profiles.md). This is a required step.
5.  **Final Validation:** After the refinement loop is complete, you **must** run all relevant validation commands from Section 2 to ensure the final code is clean, correct, and passes all tests.
6.  **Committing:** Commit your final, validated work with a message conforming to the Conventional Commits specification.
7.  **Pull Request:** Create a Pull Request. Write the description as [`CONTRIBUTING.md`, "Pull Request Process"](CONTRIBUTING.md#pull-request-process) says: `Closes #NUMBER` when an issue exists, and the Workflow Profile you applied.

**Definition of done:** see [`CONTRIBUTING.md`, "Definition of Done"](CONTRIBUTING.md#definition-of-done). It includes updating the affected documentation and the decision records (`docs/adr/`) in the same change set.

**Branches and releases:** Branch from `dev` and open pull requests into `dev`; `master` receives releases only, cut by release-please. An agent may push its branch and open a pull request; it never merges, tags, publishes a release or pushes to `dev` or `master`.

Documentation: `docs/README.md` indexes `docs/`; follow `docs/STYLE_GUIDE.md`, record decisions in `docs/adr/`, and
read `docs/architecture/` for the map of the code.

## 6. Quality Assurance Workflow Profiles & Prompts

The `workflow:` profiles (`core`, `infra`, `docs`) and the six refinement prompts are in [`docs/workflow-profiles.md`](docs/workflow-profiles.md).

## Agent notes
- Prefer a code-graph tool (the codebase-memory MCP: `search_graph`, `trace_path`, `get_code_snippet`) over grep for code questions; the index project name is derived from this directory. `.mcp.json` registers the server; a tool that reads another config file copies the `codebase-memory-mcp` entry from it. Keep the index fresh: re-index at the start of a task and after your last commit, and check it with `detect_changes`.
- Do not start the server or run `local-up.sh` unless the task needs it; infrastructure lives in the sibling repo `Digital_Assistant_microServices`.
