# Deployment notes: OIDC and the search index

Settings you check before you deploy. This is a how-to and reference page. The full key list is in
[README.md, "Configuration"](../README.md#configuration). Section 1 is advice from the Quarkus OIDC reference
(Quarkus 3.2.10 in `build.gradle`). This repository does not set the two keys, so no test here covers them.

## 1. OIDC

Tenant selection is in [ADR 0002](adr/0002-tenant-from-udan-realm-header.md). Each realm a client sends needs a
`quarkus.oidc.<realm>.auth-server-url` block. `application.properties` only sets the default tenant
(`quarkus.oidc.auth-server-url`) and the `%dev` blocks for `UDAN` and `UDAN-Plugin`.

| Key | Advice |
|---|---|
| `quarkus.oidc.tenant-enabled` | Set it to `false` for the default tenant when every request names a realm that has its own block. Then the default tenant does not contact an identity provider at start. Send a test request with an unknown realm after the change, because the answer for an unknown realm may change |
| `quarkus.oidc.connection-timeout` | Set it above the time the identity provider needs to answer discovery. A short value makes a slow provider look like a failed start. The Quarkus default is 10 seconds |
| `quarkus.oidc.<realm>.connection-timeout` | The same key for a named tenant |

Put these keys in `additional.properties` or in environment variables, not in the tracked file
([ADR 0005](adr/0005-tracked-settings-untracked-local-secrets.md)).

## 2. Hibernate Search (Elasticsearch)

Keys are in `src/main/resources/application.properties` unless noted.

| Key | Value | Meaning |
|---|---|---|
| `quarkus.hibernate-search-orm.schema-management.strategy` | `create-or-validate` | Create a missing index. Validate an existing one against the mapping. A mismatch stops the start |
| `%dev.quarkus.hibernate-search-orm.schema-management.strategy` | `drop-and-create` | Drop and rebuild the indexes at every dev start. The index is empty until the startup re-index fills it |
| `quarkus.hibernate-search-orm.elasticsearch.schema-management.required-status` | `yellow` | Wait for index status `yellow` |
| `udan.search.index-prefix` | empty | Prefix for index names, for a shared Elasticsearch |
| `udan.search.reindex-on-startup` | `true` | Re-index `SequenceList` and `ClickTrack` at each start; `false` fills only an empty index |
| `udan.search.reindex.threads`, `.batch-size`, `.attempts` | `1`, `5`, `2` | Throttle of the startup re-index (`search/StartupReindexer.java`) |

Notes:

- **Two `%dev` values.** `local.properties` sets `%dev...strategy=create-or-validate`, and `application.properties`
  sets `%dev...strategy=drop-and-create`. In `%dev`, `drop-and-create` applies. The located `local.properties` gets
  the same ordinal (250) as `application.properties`, which declares it, and on a tie the source loaded first wins:
  `application.properties`. The second copy of `local.properties` that the provider adds has ordinal 100 and loses.
  The `local.properties` line has no effect.
- **Reindex load.** The code comment in `StartupReindexer` says some documents are large and that a small shared
  Elasticsearch trips its circuit breaker under the default parallel load. That is why the re-index uses one
  loading thread and small batches. A failed run or a short count repeats once after five seconds. The repository
  sets no heap size for Elasticsearch or the server. Size the Elasticsearch heap for the full re-index when many
  environments share one cluster, or set `udan.search.reindex-on-startup=false` there.
- **Reading the result.** The re-index logs one line per entity (`SequenceList`, `ClickTrack`) with the row and
  document counts. An `ERROR` line means the re-index failed or the counts differ.

## 3. Logging in `prod`

`%prod.quarkus.log.console.json=true` switches the console log to JSON. The default
(`quarkus.log.console.json=false`) is plain text with a correlation id. The `prod` level for `com.nistapp.uda` is
`INFO`; `dev` uses `DEBUG`.
