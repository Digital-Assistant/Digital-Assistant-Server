# 0004. Resources handle HTTP; business logic lives in services

- **Status:** proposed
- **Date:** 2026-11-19
- **Deciders:** repository owner

## Context

`src/main/java/com/nistapp/uda/index/services/` holds 10 JAX-RS resources: every class there has `@Path`, and none
is `@ApplicationScoped`. The package name is historical. Two of the resources are named like services
(`SequenceListService`, `StatusService`). All 10 keep their logic in the resource methods and use `EntityManager`
directly. One business service exists, `users/UserAccountService`, beside the user resources in `users/`.

## Decision

A new endpoint is a JAX-RS resource in `services/`, named `<Topic>Resource`. Its methods handle HTTP only: paths,
request parsing and status codes. Its business logic goes in an `@ApplicationScoped` class `<Topic>Service`, also in
`services/`, which reads and writes data through the repositories ([ADR 0003](0003-database-access-through-repositories.md)).
Existing classes keep their names and are not moved for this rule. The rule is stated in
[`CONVENTIONS.md`, section 5](../../CONVENTIONS.md#5-api-design-jax-rs).

Rationale (the owner's decision of 2026-11-19): it keeps HTTP concerns apart from the logic. The logic can be
unit-tested without a running server, and more than one resource can reuse it.

## Alternatives considered

| Alternative | Why it was rejected |
|---|---|
| Logic stays in the resource methods, as today | It mixes HTTP and logic, and the logic cannot be tested without the server |
| A package per feature, as `users/` does | It adds a package per feature; it was not chosen on 2026-11-19. Further reason not recorded |

## Consequences

### Positive

- New logic can be unit-tested without the local stack.
- Resources stay thin and easy to review.

### Negative and trade-offs

- Two naming schemes live side by side until the old classes are renamed, and the package name `services/` holds
  resources.
- The existing resources still hold their logic; this decision does not migrate them.
