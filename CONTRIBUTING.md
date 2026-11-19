# Contributing to Digital Assistant Server

First off, thank you for considering contributing to the Digital Assistant Server! It's people like you that make this such a great project.

Following these guidelines helps to communicate that you respect the time of the developers managing and developing this open source project. In return, they should reciprocate that respect in addressing your issue or assessing patches and features.

## Getting Started

1.  **Fork the repository:** Start by forking the [main repository](https://github.com/Digital-Assistant/Digital-Assistant-Server) to your own GitHub account.
2.  **Clone your fork:** Clone your forked repository to your local machine:
    ```bash
    git clone https://github.com/YOUR_USERNAME/Digital-Assistant-Server.git
    ```
3.  **Navigate to the project directory:**
    ```bash
    cd Digital-Assistant-Server
    ```

## Commands

Gradle is the one runner. Run every command from the repository root. Gradle task names cannot contain `:`, so the
verb `format:check` is spelled `formatCheck`.

```bash
./gradlew build         # compiles, packages and runs check, tests included (needs the local stack)
./gradlew format        # rewrites the changed Java files in place
./gradlew formatCheck   # read-only: changed files must be formatted, and no baseline count may go up
./gradlew lint          # read-only: PMD bug-oriented ruleset
./gradlew typecheck     # compiles main and test code
./gradlew test          # runs all tests (needs the local stack)
./gradlew coverage      # runs the tests, then fails below the coverage floor (needs the local stack)
./gradlew check         # the pre-merge gate: formatCheck, typecheck, test, PMD (needs the local stack)
./gradlew security      # scans gradle.lockfile for known vulnerable dependencies (needs the network)
```

* **The local stack** is MariaDB and Elasticsearch on `127.0.0.1`, started from the sibling repository
  `Digital_Assistant_microServices`. Without it, run `./gradlew formatCheck typecheck lint`: the gate minus the tests.
* **Format:** Spotless follows `CONVENTIONS.md` (tabs, LF, 120 columns) and only touches files you changed. Run
  `git fetch origin` first, so the formatter compares against a current `origin/dev`.
* **Coverage:** the report goes to `build/reports/jacoco/test/`. `check` does not run the coverage floor; CI runs it
  in its `Coverage floor` step (`scripts/ci/coverage.sh`).
* **Security:** OSV-Scanner runs through `scripts/ci/scan.sh`. It is report-only: it prints the finding count and
  exits 0, because the existing findings are recorded debt. `SCAN_MODE=block` makes it fail.
  `./gradlew dependencyCheckAnalyze` is the old OWASP scan, not this verb; it needs an NVD key that is not set up.
* **Lint:** files that violated PMD when it was added are listed in the exclude patterns of
  `config/pmd/ruleset.xml`. That list only shrinks; new files are checked in full.
* **Dependencies:** after you change a dependency or a plugin, regenerate the lock with
  `./gradlew dependencies --write-locks`.

## Development Workflow

1.  **Create a branch from `dev`:** name it `<type>/<topic>`. `<type>` is the Conventional Commit type of the change:
    `feat`, `fix`, `docs`, `ci` and so on. `<topic>` is a few words in kebab-case. Add the issue number when there is
    one: `fix/123-session-timeout`.

    ```bash
    git switch -c feat/limit-request-payload-size dev
    ```
2.  **Make your changes:** Make your changes to the codebase.
3.  **Run tests:** Ensure that all tests pass before submitting a pull request.
    ```bash
    ./gradlew test
    ```
4.  **Run the gate:** run `./gradlew check` before you open a pull request. [Commands](#commands) lists every verb
    and what it needs.

### Local hooks

Three hooks run on your commits. They are a fast safety net: CI (`CI Gate`) is the real gate, and they do not
replace `./gradlew formatCheck` before you push.

* `pre-commit` scans the staged change for secrets.
* `pre-commit` also refuses a commit on `dev` or `master`. Run `git switch -c <type>/<topic>` and commit again; the
  staged change comes with you.
* `commit-msg` refuses a subject that is not a Conventional Commit (`scripts/ci/check-commit-subject.sh`). It uses
  the same pattern and types as the PR title check below. Merge, `fixup!`, `squash!` and `amend!` subjects pass.

Setup, once per clone:

1.  Install the [lefthook](https://github.com/evilmartians/lefthook) (v2.2.2) and [gitleaks](https://github.com/gitleaks/gitleaks) (v8.30.1) binaries and put them on your `PATH`.
2.  Run `lefthook install` in the repository. Run it again after a pull that changes `lefthook.yml`.

When a hook fails, fix the cause. Do not bypass it with `--no-verify`.

## Pull Request Process

How a change travels from your branch to a release:
```mermaid
flowchart LR
    A["Fork, or branch from dev"] --> B["Commit your change<br/>run ./gradlew test"]
    B --> C["PR into dev<br/>title: Conventional Commit, e.g. fix: ..."]
    C --> D{"CI Gate green<br/>and branch up to date?"}
    D -- no --> B
    D -- yes --> E["Squash merge by a maintainer<br/>PR title becomes the commit message"]
    E --> F["Image built and pushed to GHCR"]
    F --> G["Maintainers deploy it to TEST<br/>and check it"]
    G --> H["Release: dev into master,<br/>Release Please tags vX.Y.Z"]
```

1.  **Create a pull request:** Once your changes are ready, push your branch to your fork and create a pull request to the `dev` branch of the original repository. The PR title must follow [Conventional Commits](https://www.conventionalcommits.org/) (for example `fix: handle a missing session id`), because it becomes the commit message and drives the changelog. The `PR Title` workflow (`.github/workflows/pr-title.yml`, job `Conventional Commit Title`) rejects other titles. Allowed types: `feat`, `fix`, `perf`, `refactor`, `docs`, `test`, `build`, `ci`, `chore`, `revert`, with an optional scope and `!` for a breaking change (`fix(api): ...`). The repository squash-merges, so the PR title is the only message that lands on `dev`. CI checks only the title; the local `commit-msg` hook checks each commit subject before you push.
2.  **Write a good description:** Provide a clear and concise description of your changes in the pull request. Explain the "what" and "why" of your contribution.
3.  **Tag reviewers:** Tag the appropriate reviewers for your pull request.
4.  **Address feedback:** Be responsive to any feedback or questions from the reviewers.

**With or without an issue.** When an issue exists, link it in the description with `Closes #N`. Without one, the
task as given sets the scope: a request, a review comment or an audit finding. Say it in the description. Also name
the [workflow profile](docs/workflow-profiles.md) you applied: the issue's `workflow:` label, or `core` unless the
change is infra or docs only.

**Default branch.** `dev` is the default branch, so GitHub runs manual (`workflow_dispatch`) workflows only from `dev`.

## Definition of Done

A pull request is considered "done" when it meets the following criteria:

*   All tests pass.
*   The code is well-documented.
* When the change alters a public interface, observable behaviour, the architecture or a decision, the affected
  documentation and the decision records (`docs/adr/`) are updated in the same change set.
*   A maintainer has reviewed the pull request. No approval is required: review is a convention here, not a platform
    rule ([branch protection](docs/branch-protection.md)).
*   All feedback has been addressed.
*   The pull request is merged into the `dev` branch.
