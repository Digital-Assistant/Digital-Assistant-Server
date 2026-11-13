# Quality assurance workflow profiles and prompts

Moved from `AGENTS.md` section 6 without rewriting. In `AGENTS.md` section 5 (steps 3 and 4), the agent reads the
`workflow:` label on the issue. It then applies the matching profile from this page.

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
*(Use this profile for tasks involving only changes to `.md` files such as README and CONTRIBUTING.)*

| Prompt | Status | Description |
| :--- | :--- | :--- |
| `[PROMPT: DOCUMENTATION]` | **Recommended** | Review for clarity, grammar, and formatting. |
| All other prompts | N/A | Not applicable to documentation tasks. |

---
*The full text of each prompt is listed below for reference.*

### 6.1 Error Handling & Logging Review `[PROMPT: ERROR_HANDLING]`

<!-- prose-check off -->

> Assume the role of a seasoned software architect with deep expertise in Java and Quarkus. Your objective is to critically analyze the provided code file with a focused lens on error handling and logging integration. Specifically, assess the following: Whether all edge cases and failure modes are properly anticipated; If error codes and messages are meaningful, consistent, and correctly applied; Whether exceptions are being handled with clear propagation paths and do not result in silent failures; If structured and consistent error-handling patterns are followed; Whether all error scenarios and exceptions are logged using the already implemented logger functionality. Limit your analysis strictly to error handling and logging practices. Your response should contain actionable, architecture-aligned recommendations that improve fault tolerance and observability.

<!-- prose-check on -->

### 6.2 Input Validation Review `[PROMPT: VALIDATIONS]`

<!-- prose-check off -->

> Assume the role of a seasoned software architect specializing in Java and Quarkus. Analyze the provided file with an exclusive focus on input validation, sanitization, and type checking. Ensure that all user inputs and external data sources undergo proper validation. Identify missing or inadequate validation rules and risks related to data integrity. Review how validation errors are integrated into the existing error-handling framework. Limit your analysis strictly to validation, sanitization, and type checking. Provide a detailed report outlining the identified issues and suggested improvements, presenting the final updated code.

<!-- prose-check on -->

### 6.3 Security Hardening Review `[PROMPT: SECURITY]`

<!-- prose-check off -->

> Assume the role of a seasoned software architect specializing in Java and Quarkus security. Analyze the modified functionality with an exclusive focus on security improvements. Identify and mitigate potential vulnerabilities such as injection attacks, insecure data handling, and improper access controls. Review how security-related errors are handled, ensuring they are logged securely without exposing sensitive information. Limit your analysis strictly to security improvements. Provide a detailed report outlining the identified security issues and recommended fixes, presenting the final updated secure code.

<!-- prose-check on -->

### 6.4 Documentation & Comments Generation `[PROMPT: DOCUMENTATION]`

<!-- prose-check off -->

> Assume the role of a senior software architect. Analyze the provided code, which has already been refined for errors, validation, and security. Your sole task is to improve its documentation and inline comments without modifying any functionality. Ensure every function, parameter, return value, and complex logic block is well-documented. Add inline comments explaining conditions, edge cases, and important decisions. Strictly avoid altering existing logic. Present the fully documented code as the final output.

<!-- prose-check on -->

### 6.5 Unit Test Generation `[PROMPT: UNIT_TESTS]`

<!-- prose-check off -->

> Assume the role of a senior software architect. Analyze the provided, fully documented code. Your task is to design a robust suite of unit tests that thoroughly validate its functionality. Leverage the existing documentation and comments to ensure all logic branches, conditions, and edge cases are covered. Write tests that verify expected behaviors, handle erroneous inputs, and validate security checks. Document the unit tests themselves with clear descriptions and comments. Assume the testing environment is already configured. Present the complete set of unit tests as the final output.

<!-- prose-check on -->

### 6.6 API Test Generation `[PROMPT: API_TESTS]`

<!-- prose-check off -->

> Assume the role of a QA Automation Engineer. Analyze the provided API controller/resource file. Your task is to generate a complete suite of API tests. These tests must be black-box and run against a live instance of the service. For each endpoint, generate tests covering the happy path (e.g., 200 OK), input validation (e.g., 422 Unprocessable Entity), and authorization (e.g., 401 Unauthorized). Assert on both the status code and the key fields in the JSON response body.
> **For Java/Quarkus:** Use `@QuarkusTest` and `RestAssured`.
> Present the complete test file as the final output.

<!-- prose-check on -->
