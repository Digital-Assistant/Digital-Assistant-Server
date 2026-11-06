# Pull request

## What and why

<!-- What changes, and why. Link the issue or finding if there is one. -->

## Checklist

- [ ] PR title is a Conventional Commit (`feat: ...`, `fix(api): ...`). The PR Title workflow checks it.
- [ ] `./gradlew test` passes locally (see `CONTRIBUTING.md`, "Run tests").
- [ ] `./gradlew formatCheck` and `./gradlew lint` pass for the Java files I changed.
- [ ] Docs are updated with the code (`docs/`, `AGENTS.md` and `README.md` where they describe what changed).
- [ ] No password, API key or token is added to the source (use the Quarkus configuration, see `AGENTS.md`, "Secrets Management").
- [ ] `CI Gate` is green before a maintainer merges. This PR is not merged by its author.
