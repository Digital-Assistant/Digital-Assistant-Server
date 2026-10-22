# CI scripts

Small scripts that build, test, scan and package the Server. The GitHub workflows only call these scripts, so you can run the same steps on your laptop.

Run them from anywhere. They move to the repo root themselves. You need Java 21 (`JAVA_HOME` must point to it).

| Script | What it does |
|---|---|
| `build.sh` | Builds the jar. Skips tests. |
| `test.sh` | Runs the tests. Needs MariaDB and Elasticsearch on `127.0.0.1` (start the local stack first). |
| `scan.sh` | Looks for known security problems in the dependencies. Set `NVD_API_KEY` in your shell to make it faster. |
| `image.sh` | Builds the Docker image `udan-server:local`. Does not push. Change the name with `IMAGE_NAME` and `IMAGE_TAG`. |

No script contains a secret. Never put one in these files.
