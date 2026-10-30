# CI scripts

Small scripts that build, test, scan and package the Server. The GitHub workflows only call these scripts, so you can run the same steps on your laptop.

Run them from anywhere. They move to the repo root themselves. You need Java 21 (`JAVA_HOME` must point to it).

Which workflow calls which script:
```mermaid
flowchart LR
    subgraph WF["Workflows in .github/workflows"]
        CI["ci.yml<br/>PR and push to dev"]
        IMG["image.yml<br/>push to dev, release tags, image PRs"]
        DT["deploy-test.yml<br/>Run workflow button"]
        DPR["deploy-prod.yml<br/>Run workflow button,<br/>environment production"]
    end
    subgraph SC["scripts/ci"]
        B["build.sh"]
        T["test.sh"]
        SN["scan.sh"]
        SE["secrets.sh"]
        LW["lint-workflows.sh"]
        IM["image.sh"]
        IP["image-push.sh"]
        IS["image-scan.sh"]
        LI["latest-image-digest.sh"]
        DP["deploy-test.sh"]
        DPP["deploy-prod.sh"]
        SM["smoke-test.sh"]
    end
    BOX["TEST box<br/>deploy script lives in the private infra repo"]
    PBOX["PROD on the same box<br/>own deploy script, own SSM document"]
    CI --> B & T & SN & SE & LW
    IMG --> IM & IP & IS
    IMG -. "only if TEST_AUTO_DEPLOY=true" .-> DP & SM
    DT --> LI & DP & SM
    DP -- "AWS SSM" --> BOX
    DPR --> DPP & SM
    DPP -- "AWS SSM" --> PBOX
```

| Script | What it does |
|---|---|
| `build.sh` | Builds the jar. Skips tests. |
| `test.sh` | Runs the tests (Java 21). Needs MariaDB and Elasticsearch on `127.0.0.1` (start the local stack first). CI points the JDBC URL at its own MariaDB without SSL through `QUARKUS_DATASOURCE_JDBC_URL`, because the packaged URL asks for SSL. The step is blocking. |
| `scan.sh` | Does nothing for now and prints that the scan is skipped. The NVD-based scan is not used. |
| `image.sh` | Builds the Docker image `udan-server:local`. Does not push. Change the name with `IMAGE_NAME` and `IMAGE_TAG`. With `IMAGE_TAR` set it writes the image to a tar file instead (used by CI). |
| `image-scan.sh` | Scans an image tar with Trivy (run from a pinned container) and writes a CycloneDX SBOM. Fails on fixable HIGH/CRITICAL findings. Needs Docker. |
| `image-push.sh` | Loads an image tar and pushes it under the tags you give. Needs a prior `docker login`. Refuses `latest`. Used by `image.yml` only. |
| `secrets.sh` | Scans a commit range for secrets with a pinned gitleaks (`ci.yml`, job `Secret Scan`). Old history is not scanned. |
| `lint-workflows.sh` | Lints the workflows with pinned actionlint and zizmor (`ci.yml`, job `Workflow Lint`). |
| `deploy-test.sh` | Sends one image digest to the TEST box through AWS SSM (OIDC login, one SSM document that validates its inputs) and waits for the result. The role, the target instance and the document name come from the repository variables `AWS_ROLE_ARN`, `INSTANCE_ID` and `SSM_DOCUMENT` (`AWS_REGION` is optional, default `ap-south-1`). The script has no defaults for the first three: a missing variable stops before any AWS call. Runs only in the `test` environment of GitHub Actions. |
| `deploy-prod.sh` | Sends one component (`server` or `nestjs`) and an optional digest to PROD through AWS SSM, like `deploy-test.sh`, with the PROD role and document from the repository variables `PROD_AWS_ROLE_ARN` and `PROD_SSM_DOCUMENT` (plus `INSTANCE_ID`). An empty digest means "what TEST runs now"; the box refuses any digest that has not run on TEST. Writes digest, previous digest and health to the run summary. Runs only in the `production` environment (required reviewers). |
| `smoke-test.sh` | Post-deploy sanity check from outside: public search answers JSON (nginx, Server, Elasticsearch), protected endpoint refuses no token and a malformed token (401). No secrets, read-only. Runs as job `Sanity check TEST` after every TEST deploy, with the repo variable `TEST_BASE_URL`. Laptop: `scripts/ci/smoke-test.sh https://<test host>`. |
| `latest-image-digest.sh` | Prints the digest pushed by the newest `image.yml` push run on `dev` (artifact `image-digest`). Waits for that run's `Build and Push` job only, not the scan; fails if the job failed, never falls back to an older image. Used by `deploy-test.yml` when no digest is given. Needs `gh` and `GITHUB_REPOSITORY`; works on a laptop too. |

## Deploying to TEST
A push to `dev` builds and pushes the image but does not deploy it. TEST changes only when someone runs the `Deploy to TEST` workflow (`deploy-test.yml`): `gh workflow run deploy-test.yml` deploys the newest `dev` image, `-f digest=sha256:<64 hex>` deploys a given one (redeploy or rollback). Setting the repository variable `TEST_AUTO_DEPLOY` to `true` turns the deploy after every push back on.

## Deploying to PROD
One click: Actions > **Deploy to PROD** > Run workflow (branch `dev`). Defaults: `component=all` (Server, then NestJS) and empty digests, which deploy exactly what TEST runs now. The run waits until a required reviewer of the `production` environment approves. To roll back, run it again with the previous digest from the last run's summary (the box allows PROD's own previous digest). Command form: `gh workflow run deploy-prod.yml --ref dev [-f component=server] [-f digest_server=sha256:<64 hex>]`. Sanity check PROD runs after a Server deploy when the repository variable `PROD_BASE_URL` is set.

No script contains a secret. Never put one in these files.
