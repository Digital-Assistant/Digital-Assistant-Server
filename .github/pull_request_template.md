# Pull request

## What and why

<!-- What changes, and why. Link the issue or finding if there is one. -->

## Checklist

- [ ] PR title is a Conventional Commit (`feat: ...`, `fix(api): ...`). The PR Title workflow checks it.
- [ ] `./gradlew test` passes locally (see `CONTRIBUTING.md`, "Running tests").
- [ ] `./gradlew formatCheck` and `./gradlew lint` pass for the Java files I changed.
- [ ] Docs are updated with the code where they describe what changed: `docs/`, `README.md`, `CONTRIBUTING.md`,
  `CONVENTIONS.md` and `AGENTS.md`.
- [ ] No password, API key or token is added to the source (Quarkus configuration only: `CONVENTIONS.md`, section 10).
- [ ] `CI Gate` is green before a maintainer merges. This PR is not merged by its author.
