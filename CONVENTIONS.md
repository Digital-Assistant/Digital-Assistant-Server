# Repository Conventions

This document outlines the conventions for directory structure, naming, code style, and API design to ensure consistency and maintainability across the project.

## 1. Directory Structure

The repository follows a standard Quarkus project structure.

```
.
├── config/                  # Configuration files (e.g., Keycloak)
├── gradle/                  # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── java/            # Java source code
│   │   ├── docker/          # Dockerfiles for different environments
│   │   └── resources/       # Application resources (e.g., properties, database migrations)
│   └── test/
│       └── java/            # Java test source code
├── .gitignore               # Git ignore rules
├── build.gradle             # Project build script
├── gradlew                  # Gradle wrapper script (Linux/macOS)
├── gradlew.bat              # Gradle wrapper script (Windows)
└── README.md                # Project overview
```

- **`quarkus-app/`**: Main application source code. (Note: This directory does not exist yet, but is planned).
- **`embedding-service/`**: Source code for the embedding service. (Note: This directory does not exist yet, but is planned).
- **`scripts/`**: Utility and automation scripts. (Note: This directory does not exist yet, but is planned).


## 2. Naming Conventions

### Git Branches

Branch names should be descriptive and follow this pattern:

- **`feature/<short-description>`**: For new features (e.g., `feature/user-authentication`).
- **`bugfix/<short-description>`**: For bug fixes (e.g., `bugfix/login-form-validation`).
- **`chore/<short-description>`**: For maintenance tasks (e.g., `chore/update-dependencies`).
- **`docs/<short-description>`**: For documentation changes (e.g., `docs/add-api-conventions`).

### Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. Each commit message should consist of a **type**, a **scope** (optional), and a **description**.

**Format:** `type(scope): description`

- **Types**: `feat`, `fix`, `build`, `chore`, `ci`, `docs`, `style`, `refactor`, `perf`, `test`.
- **Example**: `feat(auth): add JWT-based authentication`

### Java Files and Variables

- **Classes and Interfaces**: `PascalCase` (e.g., `UserService`, `UserRepository`).
- **Methods and Variables**: `camelCase` (e.g., `getUserById`, `userName`).
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRIES`).
- **Packages**: `lowercase` (e.g., `com.nistapp.voice.service`).

## 3. Code Style Guidelines

### Java

We adhere to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- An IDE formatter configuration file (e.g., for IntelliJ or Eclipse) will be added to the repository to automate compliance.
- Future work will involve integrating a static analysis tool like Checkstyle into the build process.

### Python

(If Python scripts are added under `scripts/`)
- We will use the [Black](https://github.com/psf/black) code style.
- A `pyproject.toml` file will be added to configure Black.

## 4. API Endpoint Design

We follow RESTful principles for API design.

### URI Naming

- **Use nouns, not verbs**: Endpoints should represent resources.
  - **Good**: `/users`, `/users/{userId}/posts`
  - **Bad**: `/getUsers`, `/createUserPost`
- **Use kebab-case**: For multi-word resource names.
  - **Good**: `/shopping-carts`
  - **Bad**: `/shoppingCarts`, `/shopping_carts`
- **Be consistent**: Use plural nouns for collections (e.g., `/users`).

### HTTP Verbs

Use the appropriate HTTP method for the action:

- **`GET`**: Retrieve a resource or a collection of resources.
- **`POST`**: Create a new resource.
- **`PUT`**: Update an existing resource completely.
- **`PATCH`**: Partially update an existing resource.
- **`DELETE`**: Delete a resource.

### Status Codes

Use standard HTTP status codes to indicate the outcome of a request:

- **`2xx` (Success)**:
  - `200 OK`: Request succeeded.
  - `201 Created`: Resource was successfully created.
  - `204 No Content`: Request succeeded, but there is no content to return.
- **`4xx` (Client Errors)**:
  - `400 Bad Request`: The server cannot process the request due to a client error.
  - `401 Unauthorized`: Authentication is required.
  - `403 Forbidden`: The client does not have permission.
  - `404 Not Found`: The requested resource could not be found.
- **`5xx` (Server Errors)**:
  - `500 Internal Server Error`: A generic error message for an unexpected condition.
