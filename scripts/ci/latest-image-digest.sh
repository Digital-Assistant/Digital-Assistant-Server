#!/usr/bin/env bash
# Print the image digest pushed by the newest successful Image run on dev.
#   scripts/ci/latest-image-digest.sh
# The Image workflow uploads the digest it pushed as artifact `image-digest` (digest.txt). This reads
# that artifact from the newest successful push run on dev, so the result is the newest image built
# from dev (not necessarily the dev head: a commit that changes no image file builds nothing).
# Used by the manual Deploy to TEST workflow when no digest is given. Prints only the digest on
# stdout; messages go to stderr.
# Needs: gh with GH_TOKEN (actions: read), GITHUB_REPOSITORY (<owner>/<repo>).
# Optional env: IMAGE_WORKFLOW (default image.yml), IMAGE_BRANCH (default dev).
set -euo pipefail

WORKFLOW="${IMAGE_WORKFLOW:-image.yml}"
BRANCH="${IMAGE_BRANCH:-dev}"
: "${GITHUB_REPOSITORY:?set GITHUB_REPOSITORY=<owner>/<repo>}"

run_id="$(gh run list --repo "${GITHUB_REPOSITORY}" --workflow "${WORKFLOW}" --branch "${BRANCH}" \
  --event push --status success --limit 1 --json databaseId --jq '.[0].databaseId // empty')"
[ -n "${run_id}" ] || { echo "No successful ${WORKFLOW} push run on ${BRANCH}; pass a digest explicitly" >&2; exit 1; }

dir="$(mktemp -d)"
trap 'rm -rf "${dir}"' EXIT
# Runs from before this artifact existed, or older than its 90-day retention, have none.
gh run download "${run_id}" --repo "${GITHUB_REPOSITORY}" --name image-digest --dir "${dir}" >&2 \
  || { echo "Run ${run_id} has no image-digest artifact (too old or expired); pass a digest explicitly" >&2; exit 1; }
digest="$(tr -d '[:space:]' < "${dir}/digest.txt")"
[[ "${digest}" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "Run ${run_id}: digest.txt does not hold sha256:<64 hex>" >&2; exit 1; }
echo "Newest ${BRANCH} image is from Image run ${run_id}" >&2
echo "${digest}"
