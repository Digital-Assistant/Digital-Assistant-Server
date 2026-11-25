# 0005. Tracked settings, untracked local secrets, mounted overrides in deployments

- **Status:** proposed
- **Date:** 2026-11-19
- **Deciders:** repository owner

## Context

The server reads its configuration from several files. `application.properties` and `local.properties` are tracked.
Before pull request #100, the tracked files also held the local datasource password, the OIDC client secret and the
Postgres password. The local stack itself runs from `udan-workspace`, which
holds the real local credentials.

## Decision

- Tracked files hold settings only: `application.properties` and `local.properties` never hold a password or secret.
- Local credentials go in the untracked `local-secrets.properties` (listed in `.gitignore`). `./sync-credentials.sh`
  writes it from `local-secrets.properties.example` and the `udan-workspace` checkout.
- `config/LocalPropertiesConfigSourceProvider` loads `additional.properties`, `local.properties` and
  `local-secrets.properties` when each is present. The secrets file is absent in CI and in images.
- Deployed environments mount `/deployments/config/additional.properties` or set environment variables. Which value
  wins follows each source's ordinal, not the order above.
- Code reads every value with `@ConfigProperty`
  ([`CONVENTIONS.md`, section 10](../../CONVENTIONS.md#10-configuration)). The load order and the keys are in
  [`README.md`, "Configuration"](../../README.md#configuration).

Rationale, from pull request #100 ("keep local credentials out of the repo"): credentials stay out of the
repository. The local stack still works after one run of `./sync-credentials.sh`.

## Alternatives considered

| Alternative | Why it was rejected |
|---|---|
| Environment variables only, also for local development | Rationale not recorded; owner to fill in |
| A `.env` file | Rationale not recorded; owner to fill in |

## Consequences

### Positive

- No credential is committed, and CI and images run without the secrets file.

### Negative and trade-offs

- Each clone needs one run of `./sync-credentials.sh`, which needs the workspace checkout and `jq`.
- The old local-stack values stay in the Git history (pull request #100).
