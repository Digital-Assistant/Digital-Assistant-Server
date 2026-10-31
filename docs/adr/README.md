# Decision records

One file per decision, numbered `NNNN-short-title.md`, started from [0000-template.md](0000-template.md).
Status stays `proposed` until the owner accepts it. The first three records were written from the code in
2026-10 and their rationale is inferred unless the page says otherwise.

| Number | Title | Status | Summary |
|---|---|---|---|
| [0000](0000-template.md) | Template | n/a | Starting point for new records |
| [0001](0001-flyway-for-schema-migrations.md) | Flyway for schema migrations, pinned to 9.22.3 | proposed | Versioned SQL migrations run at startup |
| [0002](0002-tenant-from-udan-realm-header.md) | Tenant (OIDC realm) comes from the `UDAN-Realm` header | proposed | No default realm; missing header gives 400 |
| [0003](0003-database-access-through-repositories.md) | Database access goes through repository classes | proposed | Rule exists; 14 files still break it |
