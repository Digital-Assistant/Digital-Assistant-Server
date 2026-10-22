#!/usr/bin/env bash
# Load a scanned image tar and push it to a registry under the given tags.
# Usage: image-push.sh <image.tar> <registry/name> <tag> [<tag>...]
# Needs: a prior `docker login`. Prints the pushed digest as `digest=sha256:...` on the last line.
# Never pushes `latest`.
set -euo pipefail

tar_file="${1:?usage: image-push.sh <image.tar> <registry/name> <tag>...}"
repo="${2:?missing registry/name}"
shift 2
[[ $# -ge 1 ]] || { echo "ERROR: give at least one tag" >&2; exit 1; }

for t in "$@"; do
  [[ "$t" != "latest" ]] || { echo "ERROR: refusing to push 'latest'" >&2; exit 1; }
done

loaded="$(docker load -i "${tar_file}" | sed -n 's/^Loaded image: //p' | head -n1)"
[[ -n "${loaded}" ]] || { echo "ERROR: no image in ${tar_file}" >&2; exit 1; }

for t in "$@"; do
  docker tag "${loaded}" "${repo}:${t}"
  echo "==> Pushing ${repo}:${t}"
  docker push "${repo}:${t}"
done

digest="$(docker inspect --format '{{index .RepoDigests 0}}' "${repo}:$1" | sed 's/.*@//')"
echo "digest=${digest}"
