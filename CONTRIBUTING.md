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

## Development Workflow

1.  **Create a new branch:** Create a new branch for your feature or bug fix. Use a descriptive name, including the issue number if applicable.
    ```bash
    git checkout -b feature/your-feature-name
    ```
    or
    ```bash
    git checkout -b bugfix/issue-description
    ```
2.  **Make your changes:** Make your changes to the codebase.
3.  **Run tests:** Ensure that all tests pass before submitting a pull request.
    ```bash
    ./gradlew test
    ```
4.  **Lint and format:** Make sure your code adheres to the project's coding standards.
    ```bash
    ./gradlew formatCheck   # read-only: changed files must be formatted (Spotless, ratchet against origin/dev)
    ./gradlew format        # rewrites the changed Java files in place
    ./gradlew lint          # read-only: PMD bug-oriented ruleset
    ./gradlew typecheck     # compiles main and test code
    ./gradlew check         # the pre-merge gate: formatCheck, typecheck, test (needs the local stack), PMD
    ./gradlew security      # scans gradle.lockfile for known vulnerable dependencies (OSV-Scanner, needs network)
    ```
    The formatter only touches files you changed, so the rest of the tree stays as it is. Run `git fetch origin`
    first so `origin/dev` is current.

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

## Definition of Done

A pull request is considered "done" when it meets the following criteria:

*   All tests pass.
*   The code is well-documented.
*   The pull request has been reviewed and approved by at least one other contributor.
*   All feedback has been addressed.
*   The pull request is merged into the `dev` branch.
