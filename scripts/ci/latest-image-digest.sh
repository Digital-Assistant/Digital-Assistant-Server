#!/usr/bin/env bash
# Print the image digest pushed by the newest Image run on dev.
#   scripts/ci/latest-image-digest.sh
# The Image workflow uploads the digest it pushed as artifact `image-digest` (digest.txt). This takes
# the newest push run on dev (not necessarily the dev head: a commit that changes no image file
# builds nothing) and waits for its `Build and Push` job only. It does not wait for the rest of the
# run (the scan), so a deploy started right after a merge gets the merged image, not the previous
# one. If that job failed, this fails: it never falls back to an older image.
# Used by the manual Deploy to TEST workflow when no digest is given. Prints only the digest on
# stdout; messages go to stderr.
# Needs: gh with GH_TOKEN (actions: read), GITHUB_REPOSITORY (<owner>/<repo>).
# Optional env: IMAGE_WORKFLOW (default image.yml), IMAGE_BRANCH (default dev),
#   IMAGE_JOB (job name prefix, default "Build and Push"), IMAGE_WAIT_SECONDS (default 600).
set -euo pipefail

WORKFLOW="${IMAGE_WORKFLOW:-image.yml}"
BRANCH="${IMAGE_BRANCH:-dev}"
export JOB_PREFIX="${IMAGE_JOB:-Build and Push}"
WAIT_SECONDS="${IMAGE_WAIT_SECONDS:-600}"
: "${GITHUB_REPOSITORY:?set GITHUB_REPOSITORY=<owner>/<repo>}"

run="$(gh run list --repo "${GITHUB_REPOSITORY}" --workflow "${WORKFLOW}" --branch "${BRANCH}" \
  --event push --limit 1 --json databaseId,headSha --jq '.[0] | select(. != null) | "\(.databaseId) \(.headSha)"')"
[ -n "${run}" ] || { echo "No ${WORKFLOW} push run on ${BRANCH}; pass a digest explicitly" >&2; exit 1; }
read -r run_id head_sha <<< "${run}"

deadline=$((SECONDS + WAIT_SECONDS))
while :; do
  # "<status> <conclusion>" of the build job; empty while the job is not listed yet.
  job="$(gh run view "${run_id}" --repo "${GITHUB_REPOSITORY}" --json jobs \
    --jq '.jobs[] | select(.name | startswith(env.JOB_PREFIX)) | "\(.status) \(.conclusion)"' | head -n1)"
  case "${job}" in
    "completed success") break ;;
    completed*) echo "Run ${run_id} (${head_sha:0:7}): job '${JOB_PREFIX}' ended as '${job#completed }'; pass a digest explicitly" >&2; exit 1 ;;
  esac
  [ "${SECONDS}" -lt "${deadline}" ] || { echo "Run ${run_id} (${head_sha:0:7}): job '${JOB_PREFIX}' not finished after ${WAIT_SECONDS}s; try again or pass a digest" >&2; exit 1; }
  echo "Waiting for job '${JOB_PREFIX}' of run ${run_id} (${head_sha:0:7}): ${job:-not started}" >&2
  sleep 10
done

dir="$(mktemp -d)"
trap 'rm -rf "${dir}"' EXIT
gh run download "${run_id}" --repo "${GITHUB_REPOSITORY}" --name image-digest --dir "${dir}" >&2 \
  || { echo "Run ${run_id} has no image-digest artifact (too old or expired); pass a digest explicitly" >&2; exit 1; }
digest="$(tr -d '[:space:]' < "${dir}/digest.txt")"
[[ "${digest}" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "Run ${run_id}: digest.txt does not hold sha256:<64 hex>" >&2; exit 1; }
echo "Newest ${BRANCH} image is from Image run ${run_id} (${head_sha:0:7})" >&2
echo "${digest}"
