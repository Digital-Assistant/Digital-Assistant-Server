# Task: Formalize Database Schema Management Strategy

**Context:**
The current project uses a hybrid approach:
- **Dev Profile (`%dev`)**: Uses Hibernate ORM's `drop-and-create` strategy to auto-generate the schema from Java entities.
- **Prod Profile**: Uses Flyway migration scripts (`src/main/resources/db/migration`).

**Problem:**
This discrepancy creates a risk where schema changes made in Java entities during development (e.g., changing `int` to `Long`) are not reflected in Flyway scripts. If a developer forgets to create the corresponding SQL migration, the production deployment will fail due to schema mismatch.

**Goal:**
Establish a consistent and safe workflow for database schema changes across all environments.

**Options to Consider:**

1.  **Strict Flyway-Only (Recommended for Safety)**
    *   **Configuration:** Set `quarkus.hibernate-orm.database.generation=validate` (or `none`) for *all* profiles.
    *   **Workflow:** Developers must write a new Flyway SQL script for *every* entity change. The application will fail to start locally if the schema (via Flyway) does not match the entity.
    *   **Pros:** Guarantees dev/prod parity; prevents missing migrations; explicit review of schema changes.
    *   **Cons:** Slower development iteration (must write SQL for every change).

2.  **Hybrid Approach (Current State)**
    *   **Configuration:** `drop-and-create` for `%dev`, `validate` for `%prod`.
    *   **Workflow:** Developers iterate fast with auto-generation. Before merging, they must manually create a Flyway script to match their changes.
    *   **Pros:** Rapid prototyping.
    *   **Cons:** High risk of forgetting migration scripts; "It works on my machine" issues.

**Action Items:**
- [ ] Discuss and select the preferred strategy with the team.
- [ ] If **Flyway-Only** is chosen:
    - [ ] Revert `%dev` overrides in `application.properties` (enable Flyway, disable Hibernate auto-generation).
    - [ ] Create a new migration script (e.g., `V1.0.8__update_id_to_bigint.sql`) to align the current `SequenceList` schema (BigInt) with the codebase.
    - [ ] Verify the application starts successfully using *only* Flyway migrations.
- [ ] Update `CONVENTIONS.md` or `README.md` to document the required workflow for schema changes.
