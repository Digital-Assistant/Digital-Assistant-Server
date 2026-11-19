# 0003. Database access goes through repository classes

- **Status:** proposed
- **Date:** 2026-10-31
- **Deciders:** owner to fill in

## Context

[`CONVENTIONS.md`, section 6](../../CONVENTIONS.md#6-persistence-jpahibernatepanache) states the rule: all
database access goes through a repository class. Repositories exist in
`src/main/java/com/nistapp/uda/index/repository/` (`SequenceListDAO`, `SequenceVotesDAO`, `UserAuthDataDAO`,
`UserSessionDataDAO`, `UserclicknodesRepository`). Each one implements Panache's `PanacheRepository` or
`PanacheRepositoryBase`, so "DAO" and "Panache repository" name the same thing. Four entities also extend
`PanacheEntityBase` (`ClickTrack`, `SequenceVotes`, `UserAuthData`, `UserSessionData`), which lets code save them
without a repository. The code does not fully follow the rule. The reason for the rule is inferred, not recorded.

## Decision

Database access belongs in the `repository` package. New code does not use `EntityManager` directly. The 14 files
below do so today (measured on `dev` at `30007cb`, unchanged on `9b565e4`) and are tracked as existing debt, not
changed here:

- `src/main/java/com/nistapp/uda/index/Domain.java`
- `src/main/java/com/nistapp/uda/index/config/HealthCheckListener.java`
- `src/main/java/com/nistapp/uda/index/search/StartupReindexer.java`
- `src/main/java/com/nistapp/uda/index/services/Clickevents.java`
- `src/main/java/com/nistapp/uda/index/services/SearchList.java`
- `src/main/java/com/nistapp/uda/index/services/SearchListWithLinks.java`
- `src/main/java/com/nistapp/uda/index/services/SearchTerms.java`
- `src/main/java/com/nistapp/uda/index/services/SearchWithID.java`
- `src/main/java/com/nistapp/uda/index/services/SearchWithIDWithPermissions.java`
- `src/main/java/com/nistapp/uda/index/services/SearchWithPermissions.java`
- `src/main/java/com/nistapp/uda/index/services/SequenceListService.java`
- `src/main/java/com/nistapp/uda/index/services/StatusService.java`
- `src/main/java/com/nistapp/uda/index/services/Votes.java`
- `src/main/java/com/nistapp/uda/index/utils/SequenceListStatus.java`

New code also does not call Panache active-record methods (`entity.persist()`, `Entity.find(...)`) outside
`repository/`. Three calls in resources do so today (measured on `9b565e4`) and are tracked the same way:

- `src/main/java/com/nistapp/uda/index/services/Clickevents.java:228` (`sequenceVotes.persist()`)
- `src/main/java/com/nistapp/uda/index/services/Clickevents.java:328` (`clickTrack.persist()`)
- `src/main/java/com/nistapp/uda/index/services/Votes.java:107` (`vote.persist()`)

Both lists only shrink. Whether to migrate them, and whether search code (Hibernate Search) is exempt, is for the
owner to decide.

## Alternatives considered

| Alternative | Why it was rejected |
|---|---|
| Resources use `EntityManager` directly | Rationale not recorded; owner to fill in |
| Entities save themselves (Panache active record) | Rationale not recorded; owner to fill in |

## Consequences

### Positive

- One place to review and test queries.

### Negative and trade-offs

- The rule and the code disagree until the 14 files and the 3 active-record calls are migrated, or the rule is
  narrowed.
