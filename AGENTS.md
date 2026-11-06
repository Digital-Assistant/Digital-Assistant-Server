# `AGENTS.md` - Operating Manual for AI Contributors

This document is the Operating Manual for AI and agentic coding systems contributing to this repository. It provides the necessary context, commands, patterns, and safety guardrails. All agent-driven contributions are expected to adhere to the standards outlined here.

This file is read by Claude Code (through the `CLAUDE.md` import), opencode, Codex and Cursor.

## 1. High-Level Directive

*   **Golden Rule:** Your primary directive is to **first read and understand the existing code and the project conventions below** before writing new code. Adhere to existing patterns to ensure consistency.
*   **Problem-Solving Approach:** If a task is ambiguous, state your assumptions and ask clarifying questions in your summary. Do not invent new requirements.
*   **Strict Scope Adherence:** Your work must be strictly confined to the files and requirements outlined in the assigned GitHub Issue. Do not modify unrelated files or refactor code that is outside the explicit scope of the task.

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
Gradle is the one runner. Gradle task names cannot contain `:`, so the framework verb `format:check` is `formatCheck`. Run `git fetch origin` first: the formatter checks only files changed since `origin/dev`.
*   **Setup (non-interactive):** see `README.md`, "Setup (non-interactive)" (`./gradlew assemble`, JDK 21)
*   **Build (compile + package):** `./gradlew build`
*   **format** (writes files): `./gradlew format`
*   **format:check** (read-only): `./gradlew formatCheck`
*   **lint** (read-only, PMD): `./gradlew lint`
*   **typecheck** (compile main and test): `./gradlew typecheck`
*   **test:** `./gradlew test` (needs the local stack)
*   **coverage** (needs the local stack): `./gradlew coverage` runs the tests, writes the JaCoCo line-coverage report to `build/reports/jacoco/test/` and fails below the floor. CI runs the same check in the `Coverage floor` step (`scripts/ci/coverage.sh`); `check` does not, so it stays stack-free.
*   **check** (pre-merge gate: formatCheck, typecheck, test, PMD): `./gradlew check`
*   **security** (read-only, needs network): `./gradlew security` runs OSV-Scanner on `gradle.lockfile` through `scripts/ci/scan.sh`. It is report-only: it prints the finding count and exits 0 on findings (existing findings are recorded debt); `SCAN_MODE=block` makes it fail. `./gradlew dependencyCheckAnalyze` is the old OWASP scan, not the verb: it needs an NVD key that is not set up.
*   **Local hooks (optional, once per clone):** install the `lefthook` and `gitleaks` binaries (versions in `CONTRIBUTING.md`, "Local hooks"), then `lefthook install`. The `pre-commit` hook scans the staged change for secrets (`lefthook.yml`). CI is the real gate; do not use `--no-verify`.
*   Style follows `CONVENTIONS.md` (tabs, LF, 120 columns); Spotless enforces it on changed files, PMD baselines old violations in `config/pmd/ruleset.xml`.
*   After changing a dependency or plugin, regenerate the lock: `./gradlew dependencies --write-locks`.

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
*   Merge method: squash into `dev` (the PR title becomes the commit, so it must be a Conventional Commit). Default branch is `dev`; GitHub runs manual (`workflow_dispatch`) workflows only from the default branch.

#### **Dependency Management**
*   **Adding a Java dependency:** edit `build.gradle`. Versions come from the Quarkus BOM (`quarkusVersion` in `gradle.properties`); Flyway is pinned to 9.22.3 in `build.gradle` for MariaDB 10.11 support.

## 3. Architectural Patterns & Code Conventions

Adherence to these patterns is mandatory. Code lives under `src/main/java/com/nistapp/uda/index/`: `config/` (filters, exception mappers, tenant resolver), `models/` (entities), `repository/` (DAOs), `services/` (JAX-RS resources and logic), `users/`, `utils/`.

*   **Database Access Pattern (Java):** All database interactions **must** go through a Repository/DAO class (e.g. `SequenceListDAO`, `UserclicknodesRepository`). New code must go through a Repository/DAO class and must not inject `EntityManager` in a Resource or Service. 14 existing files do not yet (10 in `services/`); find them with `grep -rln 'EntityManager' src/main`. The list may only get shorter.
*   **API Endpoint Pattern (Java):** New endpoints are JAX-RS resources (see `services/Clickevents.java`, `StatusService.java`). Business logic **must** be delegated to an `@ApplicationScoped` service. Resource methods should only handle HTTP concerns.
*   **Configuration Pattern (Java):** All configuration values (URLs, tokens, feature flags) **must** be read from `application.properties` using `@ConfigProperty`. `src/main/resources/local.properties` is tracked and holds local-stack settings only, no passwords or secrets. Local credentials go in the untracked `local-secrets.properties` (template `local-secrets.properties.example`, written by sync-credentials.sh from the sibling microServices checkout); never commit credentials from microServices or real environment credentials. Deployed environments use a mounted `additional.properties` or environment variables. Keys for search index prefix, startup re-index, realm requirement and CORS are in the README, "Configuration".
*   **Code conventions:** Packages are `lowercase`, classes `PascalCase`, methods and variables `camelCase`, constants `UPPER_SNAKE_CASE`. REST paths are lowercase kebab-case plural nouns (`/search/all`, `/user-sessions`); JSON keys are `camelCase`. Database tables are `PascalCase`, columns `lowercase`. Log with SLF4J through a `private static final Logger` and parameterised messages (`logger.info("User {} not found", userId)`), never string concatenation. Write Javadoc for every public class and method; comments explain why, not what. Do not swallow exceptions; use specific ones. Flyway migrations are named `V<VERSION>__<DESCRIPTION>.sql` in `src/main/resources/db/migration`. Conventions for people: `CONVENTIONS.md`.
*   **Base path:** all REST routes are served under `/api` (`RestApplication` has `@ApplicationPath("/api")`), e.g. `/api/clickevents/all`. Resource `@Path` values omit the prefix.
*   **Multi-tenancy:** The OIDC tenant is chosen from the `UDAN-Realm` request header (`config/CustomTenantResolver.java`). Do not change this without an explicit task. There is no default realm: `RequireRealmFilter` returns 400 for `/api/` requests without the header, and CORS must allow the `udan-realm` header (README, "Configuration").
*   **Input safety:** Use `utils/InputSanitizer` for user-supplied text; `config/XssSanitizationFilter` and `SecurityHeadersFilter` apply to all requests.
*   **Error Handling Pattern (Java):** For user-driven errors (4xx), throw a standard JAX-RS `WebApplicationException`. For server-side errors (5xx), create a custom unchecked exception and log it in the Service layer (`LoggerUtil`). `config/GlobalExceptionMapper` formats responses.

## 4. Security & Safety Guardrails

These are strict, non-negotiable rules.

*   **Forbidden Files & Directories:** You **must not** modify any files in the `.github/workflows/`, `.env`, or any security-related configuration files unless the task explicitly instructs you to do so.
*   **Secrets Management:** You **must not** hardcode any passwords, API keys, or tokens in the source code. Use the Quarkus configuration system (`application.properties`).
*   **Authentication & Authorization:** You **must not** alter or add any logic related to user authentication or role-based access control.
*   **Dependency Vetting:** You **must not** introduce new third-party libraries or dependencies unless it is an explicit requirement of the assigned task.
*   **Out-of-Scope Changes:** You **must not** refactor or modify code in files not directly related to the assigned issue, even if you identify potential improvements. All changes must be directly related to fulfilling the issue's requirements.
*   **Tests:** You **must not** edit, skip or delete a test in `src/test/` to make a failing test pass; change a test only together with the code change that needs it.
*   **Generated Output & Scratch:** You **must not** edit generated output by hand (`build/`, `.gradle/`); regenerate it with `./gradlew build`. `artefacts/` is scratch: never commit it, never treat it as a source of truth.
*   **Default Branch & Releases:** You **must not** push to, merge into or force-push `dev` or `master`, and you **must not** tag or publish a release.
*   **Secret Stores:** You **must not** open, print or commit a secret store (`.env`, `*.jks`, `local-secrets.properties`); `local.properties` is tracked on purpose and may hold local-stack settings only.

## 5. Development & Contribution Workflow

All contributions must follow this comprehensive, sequential lifecycle:

1.  **Branching:** Create a new branch from `dev` using the format `feature/issue-NUMBER-short-description`.
2.  **Initial Implementation:** Write the initial, functionally correct code that satisfies the primary objective of the assigned GitHub Issue.
3.  **Identify Workflow Profile:** Check the labels on the assigned GitHub Issue for a `workflow:` label (e.g., `workflow:core`, `workflow:infra`). This label determines the required quality assurance steps.
4.  **Execute Refinement Loop:** Based on the identified profile, apply the **mandatory** "Quality Assurance & Refinement Prompts" from the corresponding profile in [`docs/workflow-profiles.md`](docs/workflow-profiles.md). This is a required step.
5.  **Final Validation:** After the refinement loop is complete, you **must** run all relevant validation commands from Section 2 to ensure the final code is clean, correct, and passes all tests.
6.  **Committing:** Commit your final, validated work with a message conforming to the Conventional Commits specification.
7.  **Pull Request:** Create a Pull Request. The PR description **must** link to the issue it resolves using the keyword `Closes #NUMBER`. In the description, confirm which Workflow Profile was applied.

**Definition of done:** When a change alters a public interface, observable behaviour, the architecture or a decision, update the affected documentation and the decision records (`docs/adr/`) in the same change set.

**Branches and releases:** Branch from `dev` and open pull requests into `dev`; `master` receives releases only, cut by release-please. An agent may push its branch and open a pull request; it never merges, tags, publishes a release or pushes to `dev` or `master`.

Agentic-readiness work: read `docs/agentic-readiness/digital-assistant-server-agentic-readiness-STATE.md` before changing tooling, CI, docs or this file; skipped findings in it are final.

Documentation: `docs/README.md` indexes `docs/`; follow `docs/STYLE_GUIDE.md`, record decisions in `docs/adr/`, and
read `docs/architecture/` for the map of the code.

`artefacts/` is scratch: never commit it and never treat it as a source of truth.

## 6. Quality Assurance Workflow Profiles & Prompts

The `workflow:` profiles (`core`, `infra`, `docs`) and the six refinement prompts are in [`docs/workflow-profiles.md`](docs/workflow-profiles.md).

## Agent notes
- Prefer a code-graph tool (the codebase-memory MCP: `search_graph`, `trace_path`, `get_code_snippet`) over grep for code questions; the index project name is derived from this directory.
- Do not start the server or run `local-up.sh` unless the task needs it; infrastructure lives in the sibling repo `Digital_Assistant_microServices`.
