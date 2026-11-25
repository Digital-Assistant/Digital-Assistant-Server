# Server LikeC4 model

`model.c4` describes the Server's components and what they call. It is self-contained and holds public facts only.
It is a reference for readers who want a diagram of [architecture/overview.md](../architecture/overview.md).

## Views

| View id | Shows |
|---|---|
| `serverInternals` | The Server's components (resources, repositories, filters, tenant resolver, status util, search setup) and their links to MariaDB, Elasticsearch and Keycloak |
| `serverDependencies` | The Server as one box with its callers and three external services |

## Validate and view

Run both commands from this folder. They need Node 22 and network access for `npx`.

```bash
npx --yes likec4@1.61.1 validate
npx --yes likec4@1.61.1 start
```

`start` opens a local viewer. Do not commit generated output such as `.likec4/`.

## Keep it true

The model shows the main classes only; small helpers such as `OwnerView` and `SearchListView` are left out.
Every relation in `model.c4` matches code in `src/main/java/com/nistapp/uda/index/`. For example, `Clickevents`
injects `EntityManager` directly, and the search resources call `Search.session(em)`. Change the model in the same
pull request as a change that adds or removes such a dependency.
