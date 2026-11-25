# digital-assistant-server
This is the server for Digital Assistant  

Concept video: https://youtu.be/yEPGWexvpL8 
Here is the tool in action: https://youtu.be/Iz2WgKY0fhc

Telegram: https://t.me/UniDigitalAssistant
Discord: https://discord.gg/92NqDEUbxV

## 🚀 Getting Started (Local Development)

This repository is only the Quarkus server (Gradle, Java). It listens on port **11081**.
This Server is one part of UDAN, together with the Client (browser extension) and the SDK. The full local setup
(MariaDB, Postgres, Keycloak, Elasticsearch, Nginx, the Client and the SDK) is in
[`udan-workspace`](https://github.com/Digital-Assistant/udan-workspace) (branch `dev`).

The Server also works alone, against your own MariaDB, Elasticsearch and Keycloak. Then skip the workspace steps and
read [Run the Server alone](#run-the-server-alone).

### With the workspace

Clone `udan-workspace` and, from its directory:

```bash
./udan bootstrap     # clone and build the repositories, this one included
./udan up infra      # MariaDB, Postgres, Keycloak, Elasticsearch, Nginx
./udan doctor        # check that everything is ready
```

`./udan bootstrap` clones this repository into `Digital-Assistant-Server/` inside the workspace. Run the next
commands in that copy, from its root:

```bash
cd Digital-Assistant-Server
./sync-credentials.sh
./start-server.sh
```

`./udan help` lists every workspace command. `sync-credentials.sh` and `pre-flight-check.sh` (which `start-server.sh`
runs) read the workspace path from `UDAN_WORKSPACE`. The default is `..`: the workspace is the parent directory of
this checkout. A relative value is relative to the directory you run the command from, not to this repository. They
stop with a pointer to the workspace when it is missing. Run `start-server.sh` from the repository root: it calls
`./pre-flight-check.sh` by a relative path.

To use a checkout you already have instead of the cloned copy, list it in the workspace's local manifest
(`repos.local.yaml`; see the workspace `README.md`). If that checkout is not inside the workspace directory, set
`UDAN_WORKSPACE=/path/to/udan-workspace` when you run the scripts.

### Setup (non-interactive)

Needs JDK 21 on `JAVA_HOME`. This resolves the locked dependencies (`gradle.lockfile`) and compiles the project.
It asks no questions and needs no running services:

```bash
./gradlew assemble
```

This is the one setup command for people and for coding agents. `AGENTS.md` points here and does not repeat it.

Steps 1 to 4 below use the workspace infrastructure. Without it, see [Run the Server alone](#run-the-server-alone).

### 1. Synchronize Credentials
Run this the first time, and again when the workspace credentials change:

```bash
./sync-credentials.sh
```
*This creates `src/main/resources/local-secrets.properties` (untracked, from `local-secrets.properties.example`)
with the passwords from the workspace `infra/.env` and the `backend-service` client secret from its realm file.
Nothing secret is committed. `./sync-credentials.sh --dry-run` prints the key names it would set and writes nothing.*

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
    * Check Elasticsearch health.
2. **Launch Quarkus**: Starts the application in dev mode (`./gradlew quarkusDev --no-daemon`) only if all checks pass.

### 3. Manual Pre-flight Check (Optional)
If you only want to verify the infrastructure health without starting the server:

```bash
./pre-flight-check.sh
```

### 4. Infrastructure Logs (Optional)
To follow the logs of one container, for example `keycloak-local` or `mariadb-local`:

```bash
docker logs -f <container>
```

### Run the Server alone

Without the workspace, start Quarkus directly with `./gradlew quarkusDev`. Do not use `./start-server.sh`: it runs
`pre-flight-check.sh` first, which needs the workspace and its containers and stops without them.

The `%dev` profile expects these services. The versions are the ones the workspace runs.

| Service | Default address and names | Notes |
|---|---|---|
| MariaDB 10.11 | `127.0.0.1:3306`, database `udan`, user `udan` | TLS must be on: the JDBC URLs set `useSSL=true` (the certificate is not checked). Flyway creates the tables at start |
| Elasticsearch 7 (7.17) | `127.0.0.1:9200` | Hibernate Search is set to version 7 |
| Keycloak 23 | `127.0.0.1:8080`, realms `UDAN` and `UDAN-Plugin` | Confidential client `backend-service` (`quarkus.oidc.client-id`). The extension signs in to `UDAN-Plugin` with the public client `udan-extension` |

1. Copy `src/main/resources/local-secrets.properties.example` to `src/main/resources/local-secrets.properties` and
   fill in `quarkus.datasource.password` and `quarkus.oidc.credentials.secret`. The `preflight.*` keys are used by
   `pre-flight-check.sh` only.
2. If your services use other hosts, ports, a different database or user, set environment variables. Do not put
   them in `local-secrets.properties` or `additional.properties`: those files rank below `application.properties`
   and `local.properties` (see [Configuration](#configuration)), so a host, database or user written there is ignored.
3. Start: `./gradlew quarkusDev`.

```bash
export QUARKUS_DATASOURCE_JDBC_URL='jdbc:mariadb://db.example:3306/udan?useSSL=true&trustServerCertificate=true'
export QUARKUS_FLYWAY_JDBC_URL="$QUARKUS_DATASOURCE_JDBC_URL"
export QUARKUS_DATASOURCE_USERNAME=udan
export QUARKUS_HIBERNATE_SEARCH_ORM_ELASTICSEARCH_HOSTS=es.example:9200
export QUARKUS_OIDC_AUTH_SERVER_URL=http://kc.example:8080/realms/UDAN
export QUARKUS_OIDC_UDAN_AUTH_SERVER_URL=http://kc.example:8080/realms/UDAN
export QUARKUS_OIDC_UDAN_PLUGIN_AUTH_SERVER_URL=http://kc.example:8080/realms/UDAN-Plugin
./gradlew quarkusDev
```

The ranking comes from the config code (SmallRye Config 3.3.2, Quarkus 3.2). This path has not been verified by a
full start outside the workspace. Not verified: that `./gradlew quarkusDev -D<key>=<value>` passes a system property
to the application; use environment variables.

## Configuration

Config comes from several sources. When a key is set in more than one, the source with the higher ordinal wins:

| Source | Ordinal |
|---|---|
| Java system properties (`-D`) | 400 |
| Environment variables, and files named in a `QUARKUS_CONFIG_LOCATIONS` variable (the container) | 300 |
| `config/application.properties` in the working directory | 260 |
| `application.properties` and `local.properties` (local-stack settings, no credentials; named in `quarkus.config.locations`) | 250. For a key in both, `application.properties` wins |
| `local-secrets.properties` (untracked local credentials, written by `sync-credentials.sh`) and a classpath `additional.properties`, both loaded by `LocalPropertiesConfigSourceProvider` | 100. They only add keys that the sources above do not set |

A plain key from a higher source also beats a `%dev.` key from a lower one. For example, the environment variable
`QUARKUS_DATASOURCE_USERNAME` (300) beats `%dev.quarkus.datasource.username` in `application.properties` (250).

| Key | Default | What it does |
|---|---|---|
| `udan.search.index-prefix` | empty | Prefix put in front of every Elasticsearch index name (empty gives `sequencelist`, `clicktrack`, `userclicknodes`). Lets two environments share one Elasticsearch (for example `test_`). Only `a-z`, `0-9`, `_`, `.`, `-` are allowed; anything else stops the start. A new prefix means new, empty indexes |
| `udan.search.reindex-on-startup` | `true` | `true` re-indexes every table at each start. `false` only fills an index that is empty while its table has rows (first start with a new prefix), so restarts are quick and light on a small shared Elasticsearch. Counts and failures are logged at startup |
| `udan.realm.required` | `true` | `/api/` requests without the `UDAN-Realm` header get `400` (`RequireRealmFilter`). Off in the `%test` profile only |
| `udan.auth.legacy-client-ids` | `backend-service` | Comma-separated client ids whose tokens map to the user (`preferred_username`, `google`), which is how today's extension users are stored. A token from any other client maps to (`sub`, `keycloak`). Set it to the confidential client id each environment really uses (`users/CurrentUser.java`) |
| `udan.auth.pkce-client-ids` | `udan-extension` | Comma-separated public clients that sign in with authorization code + PKCE. Their tokens always map to (`sub`, `keycloak`), even when the same id is also in `udan.auth.legacy-client-ids` (a startup warning names such an overlap), because on a public client the user picks `preferred_username`. There is no linking to older `google` users: a PKCE user is a new user and owns their data by the numeric `user_auth_data.id` that `checkauthid` returns as `id`. Not an allow-list: tokens of any client in a known realm are still accepted |
| `udan.auth.legacy-anonymous-login` | `true` | `true`: an anonymous `POST /api/user/checkauthid` or `/checkusersession` (SDK 0.12.0 and the store build call them before they hold a token) gets a placeholder answer (`id` 0) that reads and stores nothing. `false`: `401`. Turn it off once no such client is left |
| `quarkus.http.cors.*` | see below | Browser access |

**Authentication.** Every `/api/` path needs a bearer token (`quarkus.http.auth.permission.*` in `application.properties`), except CORS preflight (`OPTIONS`), `GET /api/user/getsessionkey` and the two legacy login calls above. `/q/health` is outside `/api/` and stays open. Who the caller is comes from the token only (`users/CurrentUser.java`), never from `authid`, `authsource` or `userauthid` in a request body: `checkauthid` returns the caller's own user row, and `checkusersession` binds the session id to the caller's own row (a session id bound to another user is left alone; the caller gets its own binding).

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

**Container.** The image bakes in no database settings. Mount a properties file at `/deployments/config/additional.properties` (read-only) or pass `QUARKUS_DATASOURCE_*` and the other settings as environment variables; without them the container starts but cannot reach a database. The image names that file in the `QUARKUS_CONFIG_LOCATIONS` environment variable, so it ranks with the environment variables (300) and wins over the packaged files. See the comments in `src/main/docker/Dockerfile.jvm`. Every variable name (CI, scripts and these overrides) is listed in `scripts/ci/env.example`.

---
> [!NOTE]  
> **Windows Users**: The current startup and synchronization scripts are written for Bash (Linux/macOS/WSL). Native Windows batch (`.bat`) or PowerShell (`.ps1`) scripts are planned for a future update to ensure cross-platform parity.

---
Note that you will need both the server (this repo) and client (https://github.com/Digital-Assistant/Digital_Assistant_Client) to get the functionality in above video.
The server image is published to `ghcr.io/digital-assistant/udan-server` on each push to `dev` (tag `sha-<commit>`)
and on each release (also tagged `X.Y.Z`). To configure it, see "Container" under [Configuration](#configuration).
