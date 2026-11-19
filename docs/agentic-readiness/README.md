# Agentic readiness

This folder records the first agentic-readiness run, which followed the first audit in
[`../agentic-readiness-report/`](../agentic-readiness-report/). All eight themes of that run are merged, so the run is
finished. Later runs keep their state outside the repository.

| File | Role |
| --- | --- |
| `digital-assistant-server-quality-baseline.json` | **Live.** The quality baseline that `./gradlew formatCheck` compares against (`scripts/ci/check-baseline.sh`). A count only goes down |
| `digital-assistant-server-agentic-readiness-state.json` | Record of the first run, written by the framework's `progress.py`. Do not edit it by hand |
| `digital-assistant-server-agentic-readiness-STATE.md` | Generated view of that record: preferences, themes and every finding with its status |

The state files are history, not instructions. The rules for a change are in [`CONTRIBUTING.md`](../../CONTRIBUTING.md)
and [`AGENTS.md`](../../AGENTS.md).
