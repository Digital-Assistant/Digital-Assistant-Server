# CI scripts

Small scripts that build, test, scan and package the Server. The GitHub workflows only call these scripts, so you can run the same steps on your laptop.

Run them from anywhere. They move to the repo root themselves. You need Java 21 (`JAVA_HOME` must point to it).

| Script | What it does |
|---|---|
| `build.sh` | Builds the jar. Skips tests. |
| `test.sh` | Runs the tests. Needs MariaDB and Elasticsearch on `127.0.0.1` (start the local stack first). |
| `scan.sh` | Does nothing for now and prints that the scan is skipped. The NVD-based scan is not used. |
| `image.sh` | Builds the Docker image `udan-server:local`. Does not push. Change the name with `IMAGE_NAME` and `IMAGE_TAG`. With `IMAGE_TAR` set it writes the image to a tar file instead (used by CI). |
| `image-scan.sh` | Scans an image tar with Trivy (run from a pinned container) and writes a CycloneDX SBOM. Fails on fixable HIGH/CRITICAL findings. Needs Docker. |
| `image-push.sh` | Loads an image tar and pushes it under the tags you give. Needs a prior `docker login`. Refuses `latest`. Used by `image.yml` only. |
| `deploy-test.sh` | Sends one image digest to the TEST box through AWS SSM (OIDC login, document `UDAN-DeployTest`) and waits for the result. Runs only in the `test` environment of GitHub Actions; checks its inputs before any AWS call. |
| `latest-image-digest.sh` | Prints the digest pushed by the newest successful `image.yml` run on `dev` (artifact `image-digest`). Used by `deploy-test.yml` when no digest is given. Needs `gh` and `GITHUB_REPOSITORY`; works on a laptop too. |

## Deploying to TEST
A push to `dev` builds and pushes the image but does not deploy it. TEST changes only when someone runs the `Deploy to TEST` workflow (`deploy-test.yml`): `gh workflow run deploy-test.yml` deploys the newest `dev` image, `-f digest=sha256:<64 hex>` deploys a given one (redeploy or rollback). Setting the repository variable `TEST_AUTO_DEPLOY` to `true` turns the deploy after every push back on.

No script contains a secret. Never put one in these files.
