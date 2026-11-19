# Architecture overview

Written from this repository's code only. Dependencies outside the repository are described, not documented.

## Components

One Gradle module (`settings.gradle`, `rootProject.name = 'index'`), one Quarkus application, Java package
`com.nistapp.uda.index` under `src/main/java/`.

| Component | Where | Role |
|---|---|---|
| REST resources | `Domain.java`, `User.java`, `services/`, `users/` | JAX-RS endpoints (14 classes with `@Path`). New business services go in `services/` beside them ([CONVENTIONS.md](../../CONVENTIONS.md#5-api-design-jax-rs)) |
| Users and identity | `users/` | User and admin endpoints, the caller's identity from the token (`CurrentUser`), the account service |
| Filters and tenant | `config/` | Realm check, tenant resolver, security headers, XSS sanitising, error mappers |
| Entities | `models/` | JPA and Hibernate Search entities |
| Repositories | `repository/` | DAO classes (see [ADR 0003](../adr/0003-database-access-through-repositories.md)) |
| Search | `search/` | Elasticsearch analysis, index layout, startup reindex |
| Utilities | `utils/` | Input sanitiser, logger helper, sequence status |
| Converters | `converters/` | JPA attribute converter for click nodes (`UserclicknodeConverter`) |
| Views | `views/` | `SearchListView` marker classes for the search-list views |
| Old SQL | `migration/` | `sql-modifications.sql`, a SQL file in the Java tree; Flyway does not read it |

## Repository layout

| Path | What it holds |
|---|---|
| `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradle/wrapper/` | The Gradle build and its wrapper |
| `config/pmd/`, `config/spotless/` | The PMD ruleset and the formatter settings ([CONVENTIONS.md](../../CONVENTIONS.md#1-code-formatting)) |
| `src/main/docker/` | `Dockerfile.jvm` and `Dockerfile.native` |
| `src/main/resources/` | `application.properties`, `local.properties` and `META-INF/` |
| `src/main/resources/db/migration/` | Flyway migrations for MariaDB; the only folder Flyway reads |
| `src/main/resources/pg/migration/` | Legacy PostgreSQL scripts; no configuration reads them |
| `docs/`, `scripts/ci/`, `.github/` | Documentation, CI scripts, and the workflows and repository rules |
| `AGENTS.md`, `CONTRIBUTING.md`, `CONVENTIONS.md`, `README.md`, `LICENSE` | The root documents and the licence |

## Entry points

- `src/main/java/com/nistapp/uda/index/RestApplication.java`: `@ApplicationPath("/api")`; there is no `@QuarkusMain`.
- REST roots under `/api`: `/domain`, `/search`, `/searchTerms`, `/sequence`, `/clickevents`, `/votes`, `/status`,
  `/user`, `/users`, `/admin`.
- `src/main/resources/application.properties`: configuration. Local secrets come from an untracked
  `local-secrets.properties` (see the root [README.md](../../README.md)).
- `src/main/resources/db/migration/`: Flyway migrations, `V1.0.0` to `V1.0.9` ([ADR 0001](../adr/0001-flyway-for-schema-migrations.md)).
- `start-server.sh` and `pre-flight-check.sh`: local start with environment checks.

## Data stores and external services

- MariaDB through JDBC, schema by Flyway.
- Elasticsearch through Hibernate Search (`quarkus.hibernate-search-orm.*` in `application.properties`).
- Keycloak as OIDC provider; the sibling infrastructure project that runs these services locally is an external
  dependency and is not documented here.

## Tenant and authentication flow

```mermaid
flowchart LR
    C[Client] -->|"/api request with UDAN-Realm header"| F[RequireRealmFilter]
    F -->|header missing| E[400 response]
    F -->|header present| T[CustomTenantResolver]
    T -->|realm name| O[Quarkus OIDC tenant]
    O --> R[REST resource]
    R --> D[Repository or EntityManager]
    D --> M[(MariaDB)]
    D --> S[(Elasticsearch)]
```

The filter runs after CORS and before authentication ([ADR 0002](../adr/0002-tenant-from-udan-realm-header.md)).

## Repository notes

Census of tracked files (`git ls-files`) on `dev` at `30007cb`; nothing was moved.

| Category | Count | Paths | Tag |
|---|---|---|---|
| Files over 512 KB | 0 | none | n/a |
| Binary files | 1 | `gradle/wrapper/gradle-wrapper.jar` | by design (Gradle wrapper) |
| Minified assets | 0 | none | n/a |
| Catch-all directories | 1 | `src/main/java/com/nistapp/uda/index/utils/` (3 main files, 1 test) | by design (small) |

The earlier audit counted an oversized keystore under `config/`; it is no longer tracked.
