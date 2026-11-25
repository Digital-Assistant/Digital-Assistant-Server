# Coding Conventions

These rules are mandatory for new code, for people and agents alike. Why a rule exists is recorded in
[`docs/adr/`](docs/adr/README.md). The map of the code is
[`docs/architecture/overview.md`](docs/architecture/overview.md).

---

## 0. Directory Structure

The folders, the Java packages and what each one holds are in
[`docs/architecture/overview.md`](docs/architecture/overview.md).

---

## 1. Code Formatting

- **Indentation:** Use tabs for indentation.
- **Line Endings:** Use Unix-style line endings (LF).
- **Maximum Line Length:** 120 characters.
- **Braces:** Opening braces go on the same line as the class or method declaration.
- **Whitespace:** Use whitespace for readability (e.g., around operators, after commas).
- **Tools:** Spotless applies these rules with
  [`config/spotless/eclipse-formatter.xml`](config/spotless/eclipse-formatter.xml). PMD lints with
  [`config/pmd/ruleset.xml`](config/pmd/ruleset.xml).

## 2. Naming Conventions

- **Packages:** `lowercase` (e.g., `com.nistapp.uda.index.services`).
- **Classes & Interfaces:** `PascalCase` (e.g., `SearchList`, `UserclicknodesRepository`).
- **Methods:** `camelCase` (e.g., `search`, `getId`).
- **Variables:** `camelCase` (e.g., `userClickNodes`, `correlationId`).
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `MAX_RESULTS`).
- **REST Endpoints:** see [5a. API Endpoint Design](#5a-api-endpoint-design-house-style).
- **Database Tables:** `PascalCase` (e.g., `Userclicknodes`).
- **Database Columns:** `lowercase` (e.g., `sessionid`).

### File Naming
- Java classes: `PascalCase` (e.g., `UserAuthData.java`)
- Configuration files: `lowercase-with-hyphens` (e.g., `local-secrets.properties`)
- SQL migrations: see [6. Persistence](#6-persistence-jpahibernatepanache).

---

## 3. Java Best Practices

- **Immutability:** Prefer immutable objects where possible.
- **Interfaces:** Code to interfaces, not implementations.
- **Null Handling:** Use `Optional` for return types where a value may be absent. Avoid returning `null` from methods.
- **Exception Handling:**
    - Don't swallow exceptions.
    - Use specific exceptions where possible.
    - Document exceptions thrown by methods using `@throws` in Javadoc.
- **Errors:** for a user-driven error (4xx), throw a standard JAX-RS `WebApplicationException`. For a server-side
  error (5xx), create a custom unchecked exception and log it in the service layer ([4. Logging](#4-logging)).
  `config/GlobalExceptionMapper` formats the error responses.
- **Streams:** Use the Java Stream API for processing collections.
- **Comments:**
    - Write Javadoc for all public classes and methods.
    - Avoid commented-out code. Remove it instead.
    - Use comments to explain *why*, not *what*.

## 4. Logging

- **Framework:** Use SLF4J for logging.
- **Instantiation:** Create a `private static final Logger` in each class.
- **Parameterized Logging:** Use parameterized logging messages (e.g., `logger.info("User {} not found", userId);`) instead of string concatenation.
- **Legacy:** `utils/LoggerUtil` takes a finished string, which invites concatenation. Do not use it in new code.
- **Log Levels:**
    - `ERROR`: For serious errors that prevent the application from functioning correctly.
    - `WARN`: For potential problems that don't immediately cause an error.
    - `INFO`: For important application lifecycle events.
    - `DEBUG`: For development and debugging.
    - `TRACE`: For very fine-grained debugging.

## 5. API Design (JAX-RS)

Why: [ADR 0004](docs/adr/0004-resources-handle-http-services-hold-logic.md).

- **Base Path:** `/api`, set once by `@ApplicationPath("/api")` in `RestApplication`. Resource `@Path` values omit
  the prefix: `/clickevents/all` is served at `/api/clickevents/all`.
- **Resources:** new endpoints are JAX-RS resources in `services/`, named `<Topic>Resource` (e.g. `VoteResource`).
  Resource methods handle HTTP only.
- **Business logic:** in an `@ApplicationScoped` class `<Topic>Service` in `services/`, which uses the DAOs in
  `repository/`.
- **Existing names:** the package name is historical. `SequenceListService` and `StatusService` are resources; do not
  rename them for this rule.
- **Data Format:** Use JSON for request and response bodies.
- **DTOs:** Use Data Transfer Objects (DTOs) to decouple the API from the persistence layer.

---

## 5a. API Endpoint Design (House Style)

- Name resources with plural nouns: `/users`, `/sessions`, `/domains`
- Use lowercase and hyphens for paths: `/user-sessions`
- Use HTTP verbs for actions:
  - `GET /users` (list)
  - `POST /users` (create)
  - `GET /users/{id}` (retrieve)
  - `PUT /users/{id}` (update)
  - `DELETE /users/{id}` (delete)
- Use camelCase for JSON keys
- Return standard HTTP status codes
- **Existing paths:** most current paths predate this rule (`/addVote`, `/fetchbyurl`). Do not rename them: the
  SDK and the Client call them. New endpoints follow the rule, even next to an old one.

---

## 6. Persistence (JPA/Hibernate/Panache)

Why: [ADR 0001](docs/adr/0001-flyway-for-schema-migrations.md) (Flyway) and
[ADR 0003](docs/adr/0003-database-access-through-repositories.md) (repositories).

- **Entities:** Entities should be in the `models` package.
- **Repositories:** a DAO is a Panache repository class in `repository/` (`implements PanacheRepository...`), for
  example `SequenceListDAO` or `UserclicknodesRepository`. All data access goes through one.
- **No `EntityManager` in a resource or service:** new code does not inject it there. 14 existing files still do (10 in
  `services/`); find them with `grep -rln 'EntityManager' src/main`. The list only shrinks.
- **No active record outside `repository/`:** some entities extend `PanacheEntityBase`. New code does not call
  `entity.persist()` or `Entity.find(...)` in a resource or service. 3 existing calls do; find them with
  `grep -rn '\.persist()' src/main/java/com/nistapp/uda/index/services`. The list only shrinks.
- **Database Migrations:** Database migrations are managed by Flyway. SQL migration scripts go in `src/main/resources/db/migration`.
- **Migration Script Naming:** `V<VERSION>__<DESCRIPTION>.sql` (e.g., `V1.0.9__ClickNodeOwner.sql`). A new file takes the
  next version after the highest one in the folder.

## 7. Testing

- **Framework:** Use JUnit 5 for unit tests. Tests that need the running application are `@QuarkusTest` classes
  that call the API with RestAssured.
- **Mocking:** Use Mockito for mocking dependencies.
- **Location:** Tests should be in the `src/test/java` directory, under `com/nistapp/uda/index/`.
- **Test Classes:** Test classes should be named `<ClassName>Test` (e.g., `SearchListTest`).
- **Coverage:** Aim for high test coverage.

## 8. Build & Dependencies

- **Build Tool:** The project is built with Gradle.
- **Dependencies:** Dependencies are managed in `build.gradle`.
- **BOM:** Use the Quarkus BOM (Bill of Materials) to ensure compatible dependency versions. The Quarkus version is
  `quarkusVersion` in `gradle.properties`.
- **Pinned versions:** Flyway is forced to 9.22.3 in `build.gradle`, because the BOM's Flyway does not support
  MariaDB 10.11. Why: [ADR 0001](docs/adr/0001-flyway-for-schema-migrations.md).

## 9. Git & Version Control

Branches, commit messages, pull requests and merging are in [CONTRIBUTING.md](CONTRIBUTING.md). They are not restated
here.

## 10. Configuration

Why: [ADR 0005](docs/adr/0005-tracked-settings-untracked-local-secrets.md).

- **Reading values:** read every configuration value (URLs, tokens, API keys, feature flags) from the Quarkus
  configuration with `@ConfigProperty`. Never hardcode one.
- **Secrets:** a password or secret never goes in a tracked file, `application.properties` and the tracked
  `local.properties` included. Local credentials go in the untracked `local-secrets.properties`, which
  `./sync-credentials.sh` writes from `local-secrets.properties.example`. Deployed environments use a mounted
  `additional.properties` or environment variables.
- **Credentials from elsewhere:** never commit a credential from another checkout (for example the
  workspace's `infra/.env`) or from a real environment.
- **Sources and keys:** the load order and every `udan.*` key are in
  [README.md, "Configuration"](README.md#configuration).

## 11. Input Safety

- Clean user-supplied text with `utils/InputSanitizer`.
- `config/XssSanitizationFilter` and `config/SecurityHeadersFilter` apply to every JAX-RS request and response
  outside the `dev` profile. Both are off in `dev`. Details: [docs/request-filters.md](docs/request-filters.md).

---

> This document is the source of truth for code style and repository structure. The Git workflow is in
> [CONTRIBUTING.md](CONTRIBUTING.md).
