#!/usr/bin/env bash
# Local commit-msg check: the first line of the message must be a Conventional Commit subject.
# Run by lefthook (`commit-msg` stage). The pattern and the types are the ones the CI job
# `Conventional Commit Title` (.github/workflows/pr-title.yml) applies to the pull-request title.
# Keep the two in step. Git-generated subjects (merge, fixup!, squash!, amend!) pass.
# Usage: scripts/ci/check-commit-subject.sh <commit-message-file>
set -euo pipefail

file="${1:?usage: check-commit-subject.sh <commit-message-file>}"
subject="$(grep -v '^#' "$file" | head -n 1)"

case "$subject" in
  "Merge "*|"fixup! "*|"squash! "*|"amend! "*) exit 0 ;;
esac

pattern='^(feat|fix|perf|refactor|docs|test|build|ci|chore|revert)(\([a-z0-9._/-]+\))?!?: .+'
if [[ ! "$subject" =~ $pattern ]]; then
  echo "commit-msg: not a Conventional Commit subject: '$subject'" >&2
  echo "Use 'feat: add x' or 'fix(api): y'. Types: feat, fix, perf, refactor, docs, test, build, ci, chore, revert." >&2
  exit 1
fi
