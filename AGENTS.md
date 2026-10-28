# `AGENTS.md` - Operating Manual for AI Contributors

This document is the Operating Manual for AI and agentic coding systems contributing to this repository. It provides the necessary context, commands, patterns, and safety guardrails. All agent-driven contributions are expected to adhere to the standards outlined here.

## 1. High-Level Directive & Persona

*   **Persona:** You are an expert Senior Software Engineer specializing in Java/Quarkus (Gradle) back ends.
*   **Core Mission:** Your mission is to write clean, efficient, well-tested, and secure code that solves the specific task assigned to you in a GitHub Issue.
*   **Golden Rule:** Your primary directive is to **first read and understand the existing code and project conventions (`CONVENTIONS.md`)** before writing new code. Adhere to existing patterns to ensure consistency.
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
*   **Build (compile + package):** `./gradlew build`
*   **Dependency vulnerability scan (OWASP):** `./gradlew dependencyCheckAnalyze`
*   There is no separate linter or formatter task; follow `CONVENTIONS.md` (tabs, LF, 120 columns).

#### **Testing**
*   **Run all tests:** `./gradlew test`
*   **Run a single test class:** `./gradlew test --tests 'com.nistapp.uda.index.StatusResourceTest'`
*   Tests live in `src/test/java/com/nistapp/uda/index/` (Quarkus JUnit5 + RestAssured).
*   No known failures; the CI test step is blocking. Tests need MariaDB and Elasticsearch on `127.0.0.1`.
*   Merge method: squash into `dev` (the PR title becomes the commit, so it must be a Conventional Commit). Default branch is `dev`; GitHub runs manual (`workflow_dispatch`) workflows only from the default branch.

#### **Dependency Management**
*   **Adding a Java dependency:** edit `build.gradle`. Versions come from the Quarkus BOM (`quarkusVersion` in `gradle.properties`); Flyway is pinned to 9.22.3 in `build.gradle` for MariaDB 10.11 support.

## 3. Architectural Patterns & Code Conventions

Adherence to these patterns is mandatory. Code lives under `src/main/java/com/nistapp/uda/index/`: `config/` (filters, exception mappers, tenant resolver), `models/` (entities), `repository/` (DAOs), `services/` (JAX-RS resources and logic), `users/`, `utils/`.

*   **Database Access Pattern (Java):** All database interactions **must** go through a Repository/DAO class (e.g. `SequenceListDAO`, `UserclicknodesRepository`). Never inject or use the `EntityManager` directly in a Resource or Service class.
*   **API Endpoint Pattern (Java):** New endpoints are JAX-RS resources (see `services/Clickevents.java`, `StatusService.java`). Business logic **must** be delegated to an `@ApplicationScoped` service. Resource methods should only handle HTTP concerns.
*   **Configuration Pattern (Java):** All configuration values (URLs, tokens, feature flags) **must** be read from `application.properties` using `@ConfigProperty`. `src/main/resources/local.properties` is tracked and holds local-stack settings only, no passwords or secrets. Local credentials go in the untracked `local-secrets.properties` (template `local-secrets.properties.example`, written by sync-credentials.sh from the sibling microServices checkout); never commit credentials from microServices or real environment credentials. Deployed environments use a mounted `additional.properties` or environment variables. Keys for search index prefix, startup re-index, realm requirement and CORS are in the README, "Configuration".
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

## 5. Development & Contribution Workflow

All contributions must follow this comprehensive, sequential lifecycle:

1.  **Branching:** Create a new branch from `dev` using the format `feature/issue-NUMBER-short-description`.
2.  **Initial Implementation:** Write the initial, functionally correct code that satisfies the primary objective of the assigned GitHub Issue.
3.  **Identify Workflow Profile:** Check the labels on the assigned GitHub Issue for a `workflow:` label (e.g., `workflow:core`, `workflow:infra`). This label determines the required quality assurance steps.
4.  **Execute Refinement Loop:** Based on the identified profile, apply the **mandatory** "Quality Assurance & Refinement Prompts" from the corresponding profile in Section 6. This is a required step.
5.  **Final Validation:** After the refinement loop is complete, you **must** run all relevant validation commands from Section 2 to ensure the final code is clean, correct, and passes all tests.
6.  **Committing:** Commit your final, validated work with a message conforming to the Conventional Commits specification.
7.  **Pull Request:** Create a Pull Request. The PR description **must** link to the issue it resolves using the keyword `Closes #NUMBER`. In the description, confirm which Workflow Profile was applied.

## 6. Quality Assurance Workflow Profiles & Prompts

This section defines the different levels of quality assurance required for different types of tasks. The agent **must** use the profile that matches the `workflow:` label on the assigned issue.

**IMPORTANT SCOPE CONSTRAINT:** All refinement actions described below must be strictly limited to the code you have written or modified for the current task. Do not analyze or change any code in unrelated files or functions outside the scope of the assigned issue.

---
### **[PROFILE: `workflow:core`] - Core Backend Logic**
*(Use this profile for tasks involving API endpoints, business logic, services, and database interactions.)*

| Prompt | Status | Description |
| :--- | :--- | :--- |
| `[PROMPT: ERROR_HANDLING]` | **Mandatory** | Ensure robust error handling and logging. |
| `[PROMPT: VALIDATIONS]` | **Mandatory** | Ensure all inputs are strictly validated and sanitized. |
| `[PROMPT: SECURITY]` | **Mandatory** | Perform a thorough security review for vulnerabilities. |
| `[PROMPT: DOCUMENTATION]` | **Mandatory** | Add comprehensive documentation and comments. |
| `[PROMPT: UNIT_TESTS]` | **Mandatory** | Generate a complete suite of unit tests. |
| `[PROMPT: API_TESTS]` | **Mandatory** | Generate API tests for any new or modified endpoints. |

---
### **[PROFILE: `workflow:infra`] - Infrastructure & Operations**
*(Use this profile for tasks involving Dockerfiles, `docker-compose`, CI/CD workflows (`.github/workflows/`), and utility scripts.)*

| Prompt | Status | Description |
| :--- | :--- | :--- |
| `[PROMPT: ERROR_HANDLING]` | **Mandatory** | For scripts, ensure they exit gracefully with clear error messages. |
| `[PROMPT: DOCUMENTATION]` | **Mandatory** | Add comments explaining complex commands or configurations. |
| `[PROMPT: SECURITY]` | **Recommended** | Review for basic security issues (e.g., hardcoded secrets). |
| `[PROMPT: VALIDATIONS]` | N/A | Not applicable to most infrastructure tasks. |
| `[PROMPT: UNIT_TESTS]` | N/A | Not applicable. |
| `[PROMPT: API_TESTS]` | N/A | Not applicable. |

---
### **[PROFILE: `workflow:docs`] - Documentation Only**
*(Use this profile for tasks involving only changes to `.md` files like README, CONTRIBUTING, etc.)*

| Prompt | Status | Description |
| :--- | :--- | :--- |
| `[PROMPT: DOCUMENTATION]` | **Recommended** | Review for clarity, grammar, and formatting. |
| All other prompts | N/A | Not applicable to documentation tasks. |

---
*The full text of each prompt is listed below for reference.*

### 6.1 Error Handling & Logging Review `[PROMPT: ERROR_HANDLING]`

> Assume the role of a seasoned software architect with deep expertise in Java and Quarkus. Your objective is to critically analyze the provided code file with a focused lens on error handling and logging integration. Specifically, assess the following: Whether all edge cases and failure modes are properly anticipated; If error codes and messages are meaningful, consistent, and correctly applied; Whether exceptions are being handled with clear propagation paths and do not result in silent failures; If structured and consistent error-handling patterns are followed; Whether all error scenarios and exceptions are logged using the already implemented logger functionality. Limit your analysis strictly to error handling and logging practices. Your response should contain actionable, architecture-aligned recommendations that improve fault tolerance and observability.

### 6.2 Input Validation Review `[PROMPT: VALIDATIONS]`

> Assume the role of a seasoned software architect specializing in Java and Quarkus. Analyze the provided file with an exclusive focus on input validation, sanitization, and type checking. Ensure that all user inputs and external data sources undergo proper validation. Identify missing or inadequate validation rules and risks related to data integrity. Review how validation errors are integrated into the existing error-handling framework. Limit your analysis strictly to validation, sanitization, and type checking. Provide a detailed report outlining the identified issues and suggested improvements, presenting the final updated code.

### 6.3 Security Hardening Review `[PROMPT: SECURITY]`

> Assume the role of a seasoned software architect specializing in Java and Quarkus security. Analyze the modified functionality with an exclusive focus on security improvements. Identify and mitigate potential vulnerabilities such as injection attacks, insecure data handling, and improper access controls. Review how security-related errors are handled, ensuring they are logged securely without exposing sensitive information. Limit your analysis strictly to security improvements. Provide a detailed report outlining the identified security issues and recommended fixes, presenting the final updated secure code.

### 6.4 Documentation & Comments Generation `[PROMPT: DOCUMENTATION]`

> Assume the role of a senior software architect. Analyze the provided code, which has already been refined for errors, validation, and security. Your sole task is to improve its documentation and inline comments without modifying any functionality. Ensure every function, parameter, return value, and complex logic block is well-documented. Add inline comments explaining conditions, edge cases, and important decisions. Strictly avoid altering existing logic. Present the fully documented code as the final output.

### 6.5 Unit Test Generation `[PROMPT: UNIT_TESTS]`

> Assume the role of a senior software architect. Analyze the provided, fully documented code. Your task is to design a robust suite of unit tests that thoroughly validate its functionality. Leverage the existing documentation and comments to ensure all logic branches, conditions, and edge cases are covered. Write tests that verify expected behaviors, handle erroneous inputs, and validate security checks. Document the unit tests themselves with clear descriptions and comments. Assume the testing environment is already configured. Present the complete set of unit tests as the final output.

### 6.6 API Test Generation `[PROMPT: API_TESTS]`

> Assume the role of a QA Automation Engineer. Analyze the provided API controller/resource file. Your task is to generate a complete suite of API tests. These tests must be black-box and run against a live instance of the service. For each endpoint, generate tests covering the happy path (e.g., 200 OK), input validation (e.g., 422 Unprocessable Entity), and authorization (e.g., 401 Unauthorized). Assert on both the status code and the key fields in the JSON response body.
> **For Java/Quarkus:** Use `@QuarkusTest` and `RestAssured`.
> Present the complete test file as the final output.

## Agent notes
- Prefer a code-graph tool (the codebase-memory MCP: `search_graph`, `trace_path`, `get_code_snippet`) over grep for code questions; the index project name is derived from this directory.
- Do not start the server or run `local-up.sh` unless the task needs it; infrastructure lives in the sibling repo `Digital_Assistant_microServices`.
