# Release process

Releases are cut by Release Please, from the `master` branch. Feature work never goes to `master` directly.

## How a release happens

1. Changes land on `dev` as squash commits. Each commit message is the pull request title, so it must be a
   Conventional Commit (`feat: ...`, `fix: ...`). The `PR Title` workflow checks it.
2. When `dev` is ready to ship, a maintainer opens a pull request from `dev` into `master` and merges it.
3. A push to `master` runs `.github/workflows/release.yml`. The job runs Release Please with
   `release-please-config.json` (release type `simple`, one package at the repository root). Release Please reads the
   Conventional Commit titles since the last release to choose the version bump. It then opens or updates the
   release pull request. When that pull request is merged, it creates the tag `vX.Y.Z` and the GitHub release.
4. Release Please writes `CHANGELOG.md` and updates the version in `.release-please-manifest.json`, `version.txt` and
   `build.gradle` (listed in `extra-files`). These changes come through the release pull request, not by hand.
5. When a release is created, the same workflow opens a back-merge pull request from `master` into `dev`. The version
   and the changelog then return to `dev`.

## Who does what

- **Maintainers** merge `dev` into `master` and merge the release pull request.
- **Agents never tag, create or move a release, or merge into `master`.** Agents may push their own branch and open a
  pull request into `dev`. This is the rule in `AGENTS.md`.
- **The release workflow** is the only thing that creates tags and releases. It needs a GitHub App token, not the
  default token, so the pull requests it opens start the required checks.

## Checking a release

- The tag is `vX.Y.Z` and matches the version in `build.gradle`.
- `CHANGELOG.md` on `master` lists the changes of that release.
- The `Image` workflow builds the container image on each push to `dev` and on each release tag. The deploy workflows
  are started by hand (`workflow_dispatch`): the test deploy takes an image digest or the newest `dev` image.

## Related

- Contribution flow and PR title rule: [`CONTRIBUTING.md`](../CONTRIBUTING.md)
- Agent rules for branches and releases: [`AGENTS.md`](../AGENTS.md)
- Protected-branch settings: [`branch-protection.md`](branch-protection.md)
