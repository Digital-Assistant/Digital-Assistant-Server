# 0002. Tenant (OIDC realm) comes from the `UDAN-Realm` header

- **Status:** proposed
- **Date:** 2026-10-31
- **Deciders:** owner to fill in

## Context

The server authenticates against Keycloak, with more than one realm in use (`application.properties` configures
the `UDAN` and `UDAN-Plugin` tenants). Quarkus must pick the tenant per request. The reason for a header over
other options is inferred from the code, not recorded.

## Decision

`CustomTenantResolver` (`src/main/java/com/nistapp/uda/index/config/CustomTenantResolver.java`) uses the value of
the `UDAN-Realm` request header as the tenant. There is no default realm. `RequireRealmFilter`
(`src/main/java/com/nistapp/uda/index/config/RequireRealmFilter.java`) answers 400 for any `/api/` request without
the header, except `OPTIONS` preflight, before authentication runs. The filter can be turned off with
`udan.realm.required=false`. Rationale: not recorded; owner to fill in.

## Alternatives considered

| Alternative | Why it was rejected |
|---|---|
| A default realm when the header is missing | The code comment says there is no default realm; reason not recorded; owner to fill in |
| Realm taken from the token issuer or the host name | Rationale not recorded; owner to fill in |

## Consequences

### Positive

- A client cannot fall into the wrong realm by omission.

### Negative and trade-offs

- Every client must send the header, and the browser must be allowed to send it (CORS).
- The header is client-supplied; the token must still validate against the realm it names.
