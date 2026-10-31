# 0001. Flyway for schema migrations, pinned to 9.22.3

- **Status:** proposed
- **Date:** 2026-10-31
- **Deciders:** owner to fill in

## Context

The MariaDB schema is versioned as SQL files in `src/main/resources/db/migration/` (`V1.0.0__initial-import.sql`
to `V1.0.9__ClickNodeOwner.sql`). Quarkus runs them at startup (`quarkus.flyway.migrate-at-start=true` and
`repair-at-start=true` in `src/main/resources/application.properties`). The comment in `build.gradle` says the
Quarkus-managed Flyway 9.17.0 does not support MariaDB 10.11, so `flyway-core` and `flyway-mysql` are forced to
9.22.3. The reason for choosing Flyway over another tool is inferred, not recorded in the repository.

## Decision

Schema changes are new versioned files in `src/main/resources/db/migration/`; applied files are never edited.
Flyway stays at 9.22.3 until the database server or Quarkus changes (`build.gradle`, `gradle.lockfile`).
Rationale for the tool choice: not recorded; owner to fill in.

## Alternatives considered

| Alternative | Why it was rejected |
|---|---|
| Hibernate schema auto-generation | Rationale not recorded; owner to fill in |
| Liquibase | Rationale not recorded; owner to fill in |

## Consequences

### Positive

- Schema history is in git and replayable on an empty database.

### Negative and trade-offs

- A forced version can drift from the Quarkus BOM and needs a check at each Quarkus upgrade.
- `repair-at-start=true` repairs checksum mismatches silently; the owner may want to review that.
