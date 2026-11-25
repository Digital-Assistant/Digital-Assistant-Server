# `AGENTS.md` - Operating Manual for AI Contributors

This document is the Operating Manual for AI and agentic coding systems contributing to this repository. It provides the necessary context, commands, patterns, and safety guardrails. All agent-driven contributions are expected to adhere to the standards outlined here.

This file is read by Claude Code (through the `CLAUDE.md` import), opencode, Codex and Cursor.

## 1. High-Level Directive

*   **Golden Rule:** Your primary directive is to **first read and understand the existing code and the project conventions below** before writing new code. Adhere to existing patterns to ensure consistency.
*   **Problem-Solving Approach:** A task may be ambiguous. Ask before you start when the answer changes what you build, or when the change touches a guarded area (section 4). Otherwise take the safest reading, say so, and list the open questions in your summary. Do not invent new requirements.
*   **Strict Scope Adherence:** Your work must be strictly confined to the files and requirements of the assigned task: its GitHub issue, or the task as given when there is none ([`CONTRIBUTING.md`, "With or without an issue"](CONTRIBUTING.md#pull-request-process)). Do not modify unrelated files or refactor code that is outside the explicit scope of the task.

## 2. Command & Tooling Reference

This section points to the commands that build, test and validate work, and to where each one is described.
Run every command from the repository root unless a step says otherwise.

#### **Environment Setup**
The local stack, the server's port, credentials, start-up and infrastructure logs are in
[`README.md`, "Getting Started"](README.md#-getting-started-local-development).

#### **Build & Validation**
Setup is in `README.md`, "Setup (non-interactive)". The verbs are `format`, `format:check` (`formatCheck`), `lint`,
`typecheck`, `test`, `check` and `security`, all on `./gradlew`. What each one does and needs, and the pre-merge gate,
are in [`CONTRIBUTING.md`, "Commands"](CONTRIBUTING.md#commands). The local hooks are in
[`CONTRIBUTING.md`, "Local hooks"](CONTRIBUTING.md#local-hooks).

#### **Baselines and floors**
Existing debt is counted in the quality baseline and cannot grow: no new violations. Never raise a count or lower the
coverage floor yourself; that needs the owner. The counts, the floor and how to lower a count are in
[`CONTRIBUTING.md`, "Baselines and floors"](CONTRIBUTING.md#baselines-and-floors).

#### **Testing**
Running one test class, the stack-free unit tests and the stack the other tests need are in
[`CONTRIBUTING.md`, "Running tests"](CONTRIBUTING.md#running-tests). Where tests live and how they are written is in
[`CONVENTIONS.md` §7](CONVENTIONS.md#7-testing).

#### **Dependency Management**
Where dependencies and their versions are declared is in [`CONVENTIONS.md` §8](CONVENTIONS.md#8-build--dependencies).
Regenerate the lock after a change, as [`CONTRIBUTING.md`, "Commands"](CONTRIBUTING.md#commands) says.

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
    Security-related configuration means `.github/CODEOWNERS`, `.github/rulesets/`, `.gitleaks.toml`,
    `scripts/ci/secrets.sh`, and the `quarkus.oidc.*`, `quarkus.http.auth.*` and `quarkus.http.cors*` keys.
*   **Secrets Management:** You **must not** hardcode a password, API key or token.
    Follow [CONVENTIONS.md §10](CONVENTIONS.md#10-configuration).
*   **Authentication & Authorization:** You **must not** alter or add any logic related to user authentication or
    role-based access control, unless the task explicitly says so.
*   **Dependency Vetting:** You **must not** introduce new third-party libraries or dependencies unless it is an
    explicit requirement of the assigned task. The same rule for people is in
    [`CONTRIBUTING.md`, "Commands"](CONTRIBUTING.md#commands).
*   **Recorded Decisions:** You **must not** change behaviour that a decision record in `docs/adr/` describes, unless
    the task says so. An example is the `UDAN-Realm` tenant selection in `src/main/java/com/nistapp/uda/index/config/CustomTenantResolver.java`. Such a change
    updates or adds the decision record in the same change set.
*   **Tests:** You **must not** edit, skip or delete a test in `src/test/` to make a failing test pass. The rule and
    its exception are in [`CONTRIBUTING.md`, "Definition of Done"](CONTRIBUTING.md#definition-of-done).
*   **Generated Output & Scratch:** You **must not** edit generated output by hand (`build/`, `.gradle/`); regenerate it with `./gradlew assemble`. `artefacts/` is scratch: never commit it, never treat it as a source of truth.
*   **Secret Stores:** You **must not** open, print or commit a secret store (`.env`, `*.jks`, `local-secrets.properties`).

## 5. Development & Contribution Workflow

All contributions must follow this comprehensive, sequential lifecycle:

1.  **Branching:** Create a branch from `dev`, named as [`CONTRIBUTING.md`, "Development Workflow"](CONTRIBUTING.md#development-workflow) says.
2.  **Initial Implementation:** Write the initial, functionally correct code that satisfies the primary objective of the assigned task.
3.  **Identify Workflow Profile:** Read the `workflow:` label on the assigned issue (e.g., `workflow:core`, `workflow:infra`). Without an issue, pick the profile as `CONTRIBUTING.md` says. The profile determines the required quality assurance steps.
4.  **Execute Refinement Loop:** Based on the identified profile, apply the **mandatory** "Quality Assurance & Refinement Prompts" from the corresponding profile in [`docs/workflow-profiles.md`](docs/workflow-profiles.md). This is a required step.
    That file holds the `workflow:` profiles (`core`, `infra`, `docs`) and the six refinement prompts.
5.  **Final Validation:** After the refinement loop is complete, you **must** run all relevant validation commands from Section 2 to ensure the final code is clean, correct, and passes all tests.
6.  **Committing:** Commit your final, validated work with a message conforming to the Conventional Commits specification.
7.  **Pull Request:** Create a Pull Request. Write the description as [`CONTRIBUTING.md`, "Pull Request Process"](CONTRIBUTING.md#pull-request-process) says: `Closes #NUMBER` when an issue exists, and the Workflow Profile you applied.

**Definition of done:** see [`CONTRIBUTING.md`, "Definition of Done"](CONTRIBUTING.md#definition-of-done). It includes updating the affected documentation and the decision records (`docs/adr/`) in the same change set.

**Branches and releases:** Branch from `dev` and open pull requests into `dev`; `master` receives releases only, cut by release-please. An agent may push its branch and open a pull request; it never merges, tags, publishes a release or pushes to `dev` or `master`.

Documentation: `docs/README.md` indexes `docs/`; follow `docs/STYLE_GUIDE.md` and record decisions in `docs/adr/`.
Section 3 links the map of the code.

## Agent notes
- Prefer a code-graph tool (the codebase-memory MCP: `search_graph`, `trace_path`, `get_code_snippet`) over grep for code questions; the index project name is derived from this directory. `.mcp.json` registers the server; a tool that reads another config file copies the `codebase-memory-mcp` entry from it. Keep the index fresh: re-index at the start of a task and after your last commit, and check it with `detect_changes`.
- Do not start the server or start the local infrastructure unless the task needs it; the local infrastructure comes from `udan-workspace` (`./udan up infra`, see `README.md`). To run the Server without the workspace, see `README.md`, "Run the Server alone".
