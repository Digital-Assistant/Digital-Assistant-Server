# Pre-flight check

`pre-flight-check.sh` tells you whether the local stack is ready before you start the server. This page explains
what each check does and what its message means. It is a reference page. To run it, see
[README.md, "Getting Started"](../README.md#-getting-started-local-development). `start-server.sh` runs it first.

The script reads the workspace path from `UDAN_WORKSPACE` (default `..`, the parent directory of this repository; a
relative value is relative to the directory you run the script from). It takes the certificate path from
`$UDAN_WORKSPACE/infra` and stops with exit code 1 and a pointer to
[udan-workspace](https://github.com/Digital-Assistant/udan-workspace) when that folder is missing. There is no other
place it looks.

It then reads `src/main/resources/local.properties` and the untracked `local-secrets.properties`. It stops with exit
code 1 if either file is missing, or if a required value is empty (the message lists the key names). Run
`./sync-credentials.sh` to create the second file.

## What it checks

| Step | Check | Fails the script when |
|---|---|---|
| 1 | The five containers in `CONTAINERS` run: `mariadb-local`, `postgres-local`, `elasticsearch-local`, `keycloak-local`, `nginx-local` | A container is not running. The script stops here, before any later check |
| 1.5 | The local-dev TLS certificate (see below) | The certificate file is missing. The script stops here |
| 2 | MariaDB login and `SELECT 1` | Login or TLS fails, or the port is closed |
| 3 | Postgres login | Login fails or the port is closed |
| 4 | Elasticsearch answers `200` on port 9200 | Any other status |
| 5 | Keycloak, for each of the realms `UDAN` and `UDAN-Plugin` | Discovery does not answer `200`. For `UDAN`, also when the client-credentials token request for `backend-service` returns no token. `UDAN-Plugin` is checked by discovery only |
| 6 | The Nginx gateway on `https://127.0.0.1:443` answers | The connection fails (HTTP status `000`). Any other status counts as up |

The script runs steps 2 to 6 to the end and then prints how many failed. It exits 0 when every check passes, even if
warnings were printed. The script checks no other port and no other service.

## MariaDB: TLS without verification

The probe asks for TLS but does not verify the server certificate. It matches the JDBC URL in
`application.properties`, which sets `useSSL=true&trustServerCertificate=true`. The `mariadb` client gets
`--ssl --ssl-verify-server-cert=OFF`. The `mysql` client gets `--ssl-mode=REQUIRED`. Without a client, the script
only tests that port 3306 is open and prints a warning that the credentials were not checked.

Verification stays off on purpose. An expired self-signed development certificate would fail a verifying probe
while the server connects fine.

## `AUTH FAILED` and `TLS ERROR`

| Message | Meaning | What to do |
|---|---|---|
| `AUTH FAILED (rejected user/password)` | The client output contains "access denied" | Run `./sync-credentials.sh`, or check the database user |
| `TLS ERROR` | The client output mentions a certificate, SSL or TLS, and it is not an access error | In the workspace run `scripts/infra-certs.sh --force`, then restart the infra. This is not a credential problem |
| `FAILED` | Any other error | Read the printed client message |

The script tests for "access denied" first. A message that holds both words counts as `AUTH FAILED`.

## Certificate check

The script checks two things, and neither one fails the run except a missing file.

1. **The certificate file on disk.** The path is in the variable `SSL_CERT` in the script. It is the self-signed
   certificate that the workspace infrastructure uses for the gateway, Keycloak and MariaDB
   (`infra/nginx/localhost/localhost.crt` in the workspace).
   - Missing: the script fails.
   - Expired: a warning, with the fix text.
   - Expires within 7 days: a warning.
   - Unreadable (for example no `openssl`, or the file is not a PEM certificate): the script prints `UNREADABLE` and
     counts a warning.
2. **The certificate MariaDB serves.** The script opens a TLS session to `127.0.0.1:3306` with `openssl s_client`
   and checks the certificate it gets back. If that one is expired, you get a warning. A container reads its
   certificate at start, so a renewed file does nothing until the container restarts.

Expiry is a warning because Quarkus still connects with `trustServerCertificate=true`. Other host tools (the
`mariadb` client with verification, `curl`, browsers) fail on an expired certificate.
