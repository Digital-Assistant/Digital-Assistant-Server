# digital-assistant-server
This is the server for Digital Assistant  

Concept video: https://youtu.be/yEPGWexvpL8 
Here is the tool in action: https://youtu.be/Iz2WgKY0fhc

Telegram: https://t.me/UniDigitalAssistant
Discord: https://discord.gg/92NqDEUbxV

## 🚀 Getting Started (Local Development)

Before starting the Quarkus server, ensure that all background microservices and databases are running in the `Digital_Assistant_microServices` project.

### 1. Synchronize Credentials
If you are setting up for the first time or if the central credentials in `Digital_Assistant_microServices/DOCKER-MAP.md` have changed, synchronize your local configuration:

```bash
./sync-credentials.sh
```
*This creates `src/main/resources/local-secrets.properties` (untracked, from `local-secrets.properties.example`) with the latest passwords and the `backend-service` client secret. Nothing secret is committed.*

### 2. Start the Server
To launch the backend with automatic environmental validation:

```bash
./start-server.sh
```

**The startup sequence will:**
1. **Run Pre-flight Check**: Automatically executes `./pre-flight-check.sh` to:
    * Verify all required Docker containers are running.
    * Test MariaDB & Postgres connectivity.
    * Validate Keycloak OIDC (Discovery & Token Grant).
    * Check Elasticsearch and NestJS API health.
2. **Launch Quarkus**: Starts the application in dev mode (`./gradlew quarkusDev`) only if all checks pass.

### 3. Manual Pre-flight Check (Optional)
If you only want to verify the infrastructure health without starting the server:

```bash
./pre-flight-check.sh
```

## Configuration

Config is read from `application.properties`, the packaged `local.properties` (local-stack settings, no credentials), the untracked `local-secrets.properties` (local credentials, written by `sync-credentials.sh`) and, last, an optional `additional.properties` file or environment variables; later sources win.

| Key | Default | What it does |
|---|---|---|
| `udan.search.index-prefix` | empty | Prefix put in front of every Elasticsearch index name (empty gives `sequencelist`, `clicktrack`, `userclicknodes`). Lets two environments share one Elasticsearch (for example `test_`). Only `a-z`, `0-9`, `_`, `.`, `-` are allowed; anything else stops the start. A new prefix means new, empty indexes |
| `udan.search.reindex-on-startup` | `true` | `true` re-indexes every table at each start. `false` only fills an index that is empty while its table has rows (first start with a new prefix), so restarts are quick and light on a small shared Elasticsearch. Counts and failures are logged at startup |
| `udan.realm.required` | `true` | `/api/` requests without the `UDAN-Realm` header get `400` (`RequireRealmFilter`). Off in the `%test` profile only |
| `udan.auth.legacy-client-ids` | `backend-service` | Comma-separated client ids whose tokens map to the user (`preferred_username`, `google`), which is how today's extension users are stored. A token from any other client maps to (`sub`, `keycloak`). Set it to the confidential client id each environment really uses (`users/CurrentUser.java`) |
| `udan.auth.pkce-client-ids` | `udan-extension` | Comma-separated public clients that sign in with authorization code + PKCE. Their tokens always map to (`sub`, `keycloak`), even when the same id is also in `udan.auth.legacy-client-ids` (a startup warning names such an overlap), because on a public client the user picks `preferred_username`. There is no linking to older `google` users: a PKCE user is a new user and owns their data by the numeric `user_auth_data.id` that `checkauthid` returns as `id`. Not an allow-list: tokens of any client in a known realm are still accepted |
| `udan.auth.legacy-anonymous-login` | `true` | `true`: an anonymous `POST /api/user/checkauthid` or `/checkusersession` (SDK 0.12.0 and the store build call them before they hold a token) gets a placeholder answer (`id` 0) that reads and stores nothing. `false`: `401`. Turn it off once no such client is left |
| `quarkus.http.cors.*` | see below | Browser access |

**Authentication.** Every `/api/` path needs a bearer token (`quarkus.http.auth.permission.*` in `application.properties`), except CORS preflight (`OPTIONS`), `GET /api/user/getsessionkey` and the two legacy login calls above. `/q/health` and `/q/metrics` are outside `/api/` and stay open. Who the caller is comes from the token only (`users/CurrentUser.java`), never from `authid`, `authsource` or `userauthid` in a request body: `checkauthid` returns the caller's own user row, and `checkusersession` binds the session id to the caller's own row (a session id bound to another user is left alone; the caller gets its own binding).

**Ownership.** Recordings, click nodes, votes and click tracks are owned by the caller from the token (`CurrentUser.ownerKey()`): the `authid` for legacy (google) users, which is what existing rows hold, otherwise the numeric `user_auth_data.id`. A stored all-digit owner always means a `user_auth_data.id` (old rows from migration V1.0.3). A `usersessionid` in a request body, query or path is still accepted but ignored. Changing or deleting a recording, click node or vote someone else owns gives `403`; an unknown id gives `404`. `search/all` and `search/withPermissions` add the caller's own drafts by the token, not by the `userSessionId` parameter. Responses show a recording's `usersessionid` to its owner only (the Client uses it to show the owner's controls) and never show voter ids or the email hash.

**Tenant (realm) header.** The OIDC tenant is the value of the request header `UDAN-Realm` (`CustomTenantResolver`). There is no default realm. Each realm needs its own `quarkus.oidc.<realm>` tenant block. The repo defines tenants for the `%dev` profile only (`UDAN`, `UDAN-Plugin`; `local.properties` repeats them). Every deployment must set, in `additional.properties`, at least `quarkus.oidc.UDAN.auth-server-url` and `quarkus.oidc.UDAN-Plugin.auth-server-url` (the extension's realm, where the PKCE client `udan-extension` lives), plus `quarkus.oidc.<realm>.auth-server-url` for any other realm a client sends. A tenant name with no block falls back to the default tenant (`quarkus.oidc.auth-server-url`), so a token of another realm is then rejected (issuer mismatch) instead of accepted.

**CORS.** The shipped config only sets `quarkus.http.cors=true`; origins, methods and headers are set for the `%dev` profile only. Every other deployment must set them (in `additional.properties` or as environment variables), because the browser extension calls from a `chrome-extension://` origin and customer sites call from their own origins:

```
quarkus.http.cors.origins=/.*/
quarkus.http.cors.methods=GET,POST,PUT,PATCH,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization,X-Requested-With,x-sdk-client,udan-realm
quarkus.http.cors.access-control-allow-credentials=true
```

`/.*/` is a regular expression: any origin is allowed and echoed back, which, unlike `*`, also works with credentials. Authentication is a bearer token, not the origin. The header list must be a comma-separated list that includes `udan-realm`, or the browser's preflight rejects every SDK call. Without these keys the server answers `403 CORS Rejected - Invalid origin`. Check with a preflight from an extension origin:

```
curl -s -i -X OPTIONS <server>/api/user/getsessionkey -H 'Origin: chrome-extension://x' \
  -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: content-type,authorization,udan-realm'
```
Expect `200` or `204` with `Access-Control-Allow-Origin` and `udan-realm` in `Access-Control-Allow-Headers`.

**Container.** The image bakes in no database settings. Mount a properties file at `/deployments/config/additional.properties` (read-only) or pass `QUARKUS_DATASOURCE_*` and the other settings as environment variables; without them the container starts but cannot reach a database. Values there win over the packaged ones. See the comments in `src/main/docker/Dockerfile.jvm`.

---
> [!NOTE]  
> **Windows Users**: The current startup and synchronization scripts are written for Bash (Linux/macOS/WSL). Native Windows batch (`.bat`) or PowerShell (`.ps1`) scripts are planned for a future update to ensure cross-platform parity.

---
Note that you will need both the server (this repo) and client (https://github.com/Digital-Assistant/Digital_Assistant_Client) to get the functionality in above video.
Will try to release a docker version of server soon to ease deployment.
