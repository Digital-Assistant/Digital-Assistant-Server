# API routes and authentication

Every route lives under `/api` and needs a bearer token, except three (plus CORS preflight `OPTIONS`).
This page lists each route and its auth rule. It is a reference page. Behaviour of ownership and legacy login is in
[README.md, "Configuration"](../README.md#configuration).

Sources: the `@Path` classes under `src/main/java/com/nistapp/uda/index/` and the `quarkus.http.auth.permission.*`
keys in `src/main/resources/application.properties`. The OpenAPI files in `src/main/openapi/` hold the schemas.

## Auth rules

| Rule | Where it is set | Effect |
|---|---|---|
| `api` policy `authenticated` on `/api/*` | `quarkus.http.auth.permission.api.*` | Every `/api/` path needs a valid token. An anonymous caller gets `401` |
| `preflight` policy `permit` on `OPTIONS` | `quarkus.http.auth.permission.preflight.*` | CORS preflight needs no token |
| `public` policy `permit` | `quarkus.http.auth.permission.public.paths` | Only `/api/user/getsessionkey`, `/api/user/checkauthid` and `/api/user/checkusersession` |
| `UDAN-Realm` header | `config/RequireRealmFilter.java` | A request without it gets `400` before authentication. Preflight and paths outside `/api/` are exempt |
| `@RolesAllowed("user")` | `users/Users.java` | `/api/users/me` also needs the role `user` |

Paths outside `/api/` (`/q/health`) needs no token and no realm header.

An unknown `/api/` path with a `UDAN-Realm` header gets `401` for an anonymous caller and `404` for an authenticated
one. The anonymous case is derived from the `api` policy, which matches before routing; no test asserts it.
Tests: `ApiAuthenticationTest` (known paths) and `GlobalExceptionMapperTest.test404ReturnsJson` (authenticated `404`).

## Routes

Auth column: "token" means the `api` policy applies and nothing more.

| Method and path | Class | Auth | What it does |
|---|---|---|---|
| `GET /api/domain` | `Domain` | token | Returns the text `Site is up` |
| `GET /api/domain/patterns` | `Domain` | token | Domain patterns for `domain` |
| `GET /api/search/all` | `SearchList` | token | Published search, plus the caller's own drafts |
| `GET /api/search/withPermissions` | `SearchWithPermissions` | token | Search with the caller's permissions |
| `GET /api/search/withPermissions/{id}` | `SearchWithIDWithPermissions` | token | One sequence by id, with permissions |
| `GET /api/search/{id}` | `SearchWithID` | token | One sequence by id; `domain` query parameter is required (`400` without it) |
| `GET /api/search/links` | `SearchListWithLinks` | token | Search that returns sequences with links (see below) |
| `GET /api/searchTerms/all` | `SearchTerms` | token | Search terms from click tracks |
| `POST /api/sequence/update` | `SequenceListService` | token | Update a sequence |
| `POST /api/sequence/reindex/{id}` | `SequenceListService` | token | Re-index one sequence in Elasticsearch |
| `GET /api/clickevents/all` | `Clickevents` | token | List `JavascriptEvents` rows |
| `POST /api/clickevents` | `Clickevents` | token | Store one `JavascriptEvents` row |
| `GET /api/clickevents/fetchbyurl` | `Clickevents` | token | Events by URL path |
| `GET /api/clickevents/fetchrecorddata` | `Clickevents` | token | Click nodes of a session and domain between `start` and `end` |
| `GET /api/clickevents/fetchtimestamp` | `Clickevents` | token | Timestamp as plain text |
| `POST /api/clickevents/clickednode` | `Clickevents` | token | Store a clicked node |
| `POST /api/clickevents/updateclickednode` | `Clickevents` | token | Update a clicked node |
| `POST /api/clickevents/recordsequencedata` | `Clickevents` | token | Store a recorded sequence |
| `POST /api/clickevents/updatesequencedata` | `Clickevents` | token | Update a recorded sequence |
| `POST /api/clickevents/sequence/delete` | `Clickevents` | token | Delete a sequence |
| `GET /api/clickevents/sequence/votes/{id}` | `Clickevents` | token | Votes of a sequence |
| `PUT /api/clickevents/userclick` | `Clickevents` | token | Store a click track row (`ClickTrack`) |
| `GET /api/clickevents/suggested` | `Clickevents` | token | Click nodes picked at random for a domain |
| `GET /api/votes/{id}` | `Votes` | token | Votes of a sequence |
| `GET /api/votes/{id}/{userSessionId}` | `Votes` | token | One vote |
| `POST /api/votes/addVote` | `Votes` | token | Add a vote |
| `DELETE /api/votes/deleteVote/{id}/{userSessionId}` | `Votes` | token | Delete a vote |
| `GET /api/status/all` | `StatusService` | token | Statuses; `isActive` filter, default `true` |
| `GET /api/status/category/{category}` | `StatusService` | token | Statuses of a category |
| `GET /api/status/{id}` | `StatusService` | token | One status |
| `POST /api/status` | `StatusService` | token | Create a status |
| `PUT /api/status/{id}` | `StatusService` | token | Update a status |
| `DELETE /api/status/{id}` | `StatusService` | token | Delete a status |
| `PATCH /api/status/{id}/toggle-active` | `StatusService` | token | Switch a status on or off |
| `GET /api/user/getsessionkey` | `User` | public | Returns a session key and sets the cookie `nist-voice-usersessionid` |
| `POST /api/user/checkauthid` | `User` | public | Caller's own user row (legacy login) |
| `POST /api/user/checkusersession` | `User` | public | Binds a session id to the caller's row (legacy login) |
| `GET /api/users/me` | `Users` | token and role `user` | User name of the caller |
| `GET /api/admin` | `Admin` | token | Returns `granted`; no role check |

### Notes

- **Legacy login.** The two public `POST` routes answer an anonymous caller with a placeholder while
  `udan.auth.legacy-anonymous-login` is `true`. With `false` they return `401`. A caller with a token gets the real
  answer. Details: [README.md, "Configuration"](../README.md#configuration).
- **Session cookie.** `getsessionkey` returns the cookie value if the cookie exists. Otherwise it makes a random
  20-character key and sets the cookie for 365 days (`User.java`).
- **`/search/{id}`.** The path parameter is a `long` (`SearchWithID.java`). `ClickeventsResourceTest` and
  `SearchListResourceTest` cover search routes.
- **`/search/links`.** The route still exists and is covered by `ApiAuthenticationTest`, which expects `401` without
  a token. Known callers (the browser SDK and the browser extension) do not call it.
  Treat it as unused by them. Do not remove it without checking other callers.
- **Ownership.** Writes check the owner from the token and give `403` for someone else's data and `404` for an
  unknown id. See [README.md, "Configuration"](../README.md#configuration).

## See also

- [request-filters.md](request-filters.md): what runs before and after each route.
- [architecture/overview.md](architecture/overview.md): where each class sits.
