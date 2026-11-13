# Branch protection on `dev`

Reference: what GitHub requires before a change reaches `dev`, as confirmed on 2026-11-13. The rules are declared
as code in `.github/rulesets/`, but GitHub does not apply those files by itself. The live setting can differ from the
files and from this page. Re-check it with the command below.

## What is enforced

| Item | Value |
|---|---|
| Mechanism | Repository ruleset named "Protect dev", id 25616141. Classic branch protection is not used (`/branches/dev/protection` answers 404 "Branch not protected") |
| Enforcement | Active, on `refs/heads/dev` |
| Required status checks | `CI Gate` (the aggregate job in `.github/workflows/ci.yml`) and `Conventional Commit Title` (the job in `.github/workflows/pr-title.yml`) |
| Up to date before merge | Yes (`strict_required_status_checks_policy: true`) |
| Pull request | Required, with 0 required approvals |
| Other rules | Branch deletion blocked; non-fast-forward pushes blocked; merge queue on (grouping `ALLGREEN`, method `SQUASH`) |
| Bypass actors | None. The rules bind administrators too (`current_user_can_bypass` reports `never`) |
| Plan | The repository is public under an Organization owner, so rulesets are enforced on the free plan |
| Confirmed | 2026-11-13, read with `gh api` (read-only) on the owner's behalf; earlier 2026-10-31 |

A second ruleset, "Protect master" (id 25616143), covers `master`. It is active and has no bypass actors. It blocks
branch deletion and non-fast-forward pushes, and it requires a pull request with 0 approvals. It requires no status
check and has no merge queue (read 2026-11-13). Its file adds one check; see [Rules as code](#rules-as-code).

## Rules as code

| File | Ruleset | Same as the live setting? |
|---|---|---|
| `.github/rulesets/dev-protection.json` | Protect dev | Yes, copied on 2026-11-13 |
| `.github/rulesets/master-protection.json` | Protect master | No: it also requires `Check source branch` |

The files are the source of truth for review. Change a rule in the file in a pull request, then ask the owner to apply it.

## Source-branch rule

A pull request into `master` must come from `dev`. Release Please also opens release pull requests from a branch
that starts with `release-please--branches--master`, so that prefix is allowed. The job `Check source branch` in
`.github/workflows/pr-source-branch.yml` fails any other head branch. Its name must not change: the master ruleset
file requires it by name. The live "Protect master" ruleset does not require it until the owner applies the file.

## What this means for CI

- `CI Gate` and `Conventional Commit Title` are the required checks on `dev`. Their job names must not change: a
  rename silently drops the requirement.
- A job gates `CI Gate` only if it is in its `needs` list: `Build and Test`, `Secret Scan`, `CodeQL (Java)`,
  `Workflow Lint` and `Docs Lint`. `Docs Lint` skips its steps when no Markdown changed, and the job still succeeds.
  `Dependency Scan (report-only)` and the image scan in `image.yml` are outside the list. They cannot block a merge.
  The scan is report-only on purpose. Its count (101 advisories in 29 packages) is recorded debt.
  `scripts/ci/scan.sh` fails when the count rises, but only in that job.
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

## Apply a changed file

Only the repository owner runs these commands. GitHub does not apply a file by itself.

```bash
gh api repos/Digital-Assistant/Digital-Assistant-Server/rulesets --jq '.[] | [.id, .name] | @tsv'
gh api -X PUT repos/Digital-Assistant/Digital-Assistant-Server/rulesets/25616143 --input .github/rulesets/master-protection.json
gh api -X PUT repos/Digital-Assistant/Digital-Assistant-Server/rulesets/25616141 --input .github/rulesets/dev-protection.json
```

The first command lists the ruleset ids. Use the second for "Protect master" and the third for "Protect dev".
