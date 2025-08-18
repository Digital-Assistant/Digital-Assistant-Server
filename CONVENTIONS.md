# Coding Conventions

This document outlines the coding conventions for the Digital Assistant Server project. Adhering to these conventions is crucial for maintaining code quality, readability, and consistency, especially when working with AI coding assistants.

## 1. Code Formatting

- **Indentation:** Use tabs for indentation.
- **Line Endings:** Use Unix-style line endings (LF).
- **Maximum Line Length:** 120 characters.
- **Braces:** Opening braces go on the same line as the class or method declaration.
- **Whitespace:** Use whitespace for readability (e.g., around operators, after commas).

## 2. Naming Conventions

- **Packages:** `lowercase` (e.g., `com.nistapp.uda.index.services`).
- **Classes & Interfaces:** `PascalCase` (e.g., `SearchList`, `UserclicknodesRepository`).
- **Methods:** `camelCase` (e.g., `search`, `findbynodeid`).
- **Variables:** `camelCase` (e.g., `searchresults`, `queryFunction`).
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `MAX_RESULTS`).
- **REST Endpoints:** `lowercase` and `kebab-case` (e.g., `/search/all`).
- **Database Tables:** `PascalCase` (e.g., `Userclicknodes`).
- **Database Columns:** `lowercase` (e.g., `sessionid`).

## 3. Java Best Practices

- **Immutability:** Prefer immutable objects where possible.
- **Interfaces:** Code to interfaces, not implementations.
- **Null Handling:** Use `Optional` for return types where a value may be absent. Avoid returning `null` from methods.
- **Exception Handling:**
    - Don't swallow exceptions.
    - Use specific exceptions where possible.
    - Document exceptions thrown by methods using `@throws` in Javadoc.
- **Streams:** Use the Java Stream API for processing collections.
- **Comments:**
    - Write Javadoc for all public classes and methods.
    - Avoid commented-out code. Remove it instead.
    - Use comments to explain *why*, not *what*.

## 4. Logging

- **Framework:** Use SLF4J for logging.
- **Instantiation:** Create a `private static final Logger` in each class.
- **Parameterized Logging:** Use parameterized logging messages (e.g., `logger.info("User {} not found", userId);`) instead of string concatenation.
- **Log Levels:**
    - `ERROR`: For serious errors that prevent the application from functioning correctly.
    - `WARN`: For potential problems that don't immediately cause an error.
    - `INFO`: For important application lifecycle events.
    - `DEBUG`: For development and debugging.
    - `TRACE`: For very fine-grained debugging.

## 5. API Design (JAX-RS)

- **Base Path:** `/api`.
- **Resources:** Resources should be in the `services` package.
- **Resource Paths:** Use plural nouns for resource paths (e.g., `/users`, `/orders`).
- **HTTP Methods:** Use standard HTTP methods: `GET`, `POST`, `PUT`, `DELETE`.
- **Status Codes:** Use appropriate HTTP status codes.
- **Data Format:** Use JSON for request and response bodies.
- **DTOs:** Use Data Transfer Objects (DTOs) to decouple the API from the persistence layer.

## 6. Persistence (JPA/Hibernate/Panache)

- **Entities:** Entities should be in the `models` package.
- **Repositories:** Repositories should be in the `repository` package.
- **Panache:** Use Panache repositories for standard CRUD operations.
- **Database Migrations:** Database migrations are managed by Flyway. SQL migration scripts go in `src/main/resources/db/migration`.
- **Migration Script Naming:** `V<VERSION>__<DESCRIPTION>.sql` (e.g., `V1.0.6__Add_user_preferences.sql`).

## 7. Testing

- **Framework:** Use JUnit 5 for unit tests.
- **Mocking:** Use Mockito for mocking dependencies.
- **Location:** Tests should be in the `src/test/java` directory.
- **Test Classes:** Test classes should be named `<ClassName>Test` (e.g., `SearchListTest`).
- **Coverage:** Aim for high test coverage.

## 8. Build & Dependencies

- **Build Tool:** The project is built with Gradle.
- **Dependencies:** Dependencies are managed in `build.gradle`.
- **BOM:** Use the Quarkus BOM (Bill of Materials) to ensure compatible dependency versions.

## 9. Git & Version Control

- **Branching Model:** TBD (e.g., GitFlow).
- **Commit Messages:** Follow the [Conventional Commits](https.conventionalcommits.org/) specification (e.g., `feat: add user profile endpoint`).
- **Pull Requests:** Require at least one approval before merging.
