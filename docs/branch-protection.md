# Branch protection on `dev`

Reference: what GitHub requires before a change reaches `dev`, as confirmed on 2026-10-31. The setting lives on
GitHub, not in the repository, so this page is a record and can go stale. Re-check it with the command below.

## What is enforced

| Item | Value |
|---|---|
| Mechanism | Repository ruleset named "Protect dev", id 25616141. Classic branch protection is not used (`/branches/dev/protection` answers 404 "Branch not protected") |
| Enforcement | Active, on `refs/heads/dev` |
| Required status check | `CI Gate` (the aggregate job in `.github/workflows/ci.yml`) |
| Up to date before merge | Yes (`strict_required_status_checks_policy: true`) |
| Pull request | Required, with 0 required approvals |
| Other rules | Branch deletion blocked; non-fast-forward pushes blocked; merge queue on (grouping `ALLGREEN`, method `SQUASH`) |
| Bypass actors | None. The rules bind administrators too (`current_user_can_bypass` reports `never`) |
| Plan | The repository is public under an Organization owner, so rulesets are enforced on the free plan |
| Confirmed | 2026-10-31, read with `gh api` by the orchestrator on the owner's behalf |

A second ruleset, "Protect master", covers `master`. Its details are not recorded here.

## What this means for CI

- `CI Gate` is the only required check. Its job name must not change: a rename silently drops the requirement.
- A job counts only if it is in the `needs` list of `CI Gate`. `Dependency Scan (report-only)`, `Docs Lint` and the
  image scan in `image.yml` are outside it and cannot block a merge.
- The merge queue verifies a pull request on a temporary `merge_group` ref, so `ci.yml` keeps the `merge_group`
  trigger.
- One required approval is not configured. A review is a convention here, not a platform rule.

## Re-check

Read-only, needs `gh` logged in with read access to the repository:

```bash
gh api repos/Digital-Assistant/Digital-Assistant-Server/rulesets/25616141
```

Compare `enforcement`, `bypass_actors`, the `required_status_checks` rule and its `strict_required_status_checks_policy`
with the table above. Update this page and its confirmation date when they differ. Only the repository owner changes
the setting.
