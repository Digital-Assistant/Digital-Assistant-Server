#!/usr/bin/env bash
# Scan an image tar with Trivy and write an SBOM next to it.
# Usage: image-scan.sh <image.tar> [sbom-output-path]
# Fails (exit 1) on any HIGH or CRITICAL vulnerability that has a fix. Unfixable ones are
# printed in the report but do not fail the build.
# Trivy runs from a container pinned by digest, so no third-party GitHub Action is needed.
set -euo pipefail

tar_file="${1:?usage: image-scan.sh <image.tar> [sbom-output-path]}"
sbom_out="${2:-sbom.cdx.json}"

# trivy v0.76.1
trivy_image="docker.io/aquasec/trivy@sha256:790fd66d1697395d8693d1acaa1ea702406a20f109b6fc7bac8db623eca6c06c"

tar_abs="$(cd "$(dirname "${tar_file}")" && pwd)/$(basename "${tar_file}")"
out_dir="$(cd "$(dirname "${sbom_out}")" && pwd)"
cache_dir="${TRIVY_CACHE_DIR:-${HOME}/.cache/trivy}"
mkdir -p "${cache_dir}"

run_trivy() {
  docker run --rm \
    -v "${tar_abs}:/image.tar:ro" \
    -v "${out_dir}:/out" \
    -v "${cache_dir}:/cache" \
    -u "$(id -u):$(id -g)" \
    "${trivy_image}" --cache-dir /cache "$@"
}

echo "==> SBOM (CycloneDX)"
run_trivy image --input /image.tar --format cyclonedx --output "/out/$(basename "${sbom_out}")" --quiet

echo "==> Vulnerability scan (fail on fixable HIGH/CRITICAL)"
run_trivy image --input /image.tar --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 --quiet
echo "==> Scan passed"
