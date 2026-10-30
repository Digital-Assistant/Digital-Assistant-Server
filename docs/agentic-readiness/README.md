# Agentic readiness

This folder tracks the work that makes this repo easier and safer for AI coding agents to change. It follows
the first audit in [`../agentic-readiness-report/`](../agentic-readiness-report/).

| File | Role |
| --- | --- |
| `digital-assistant-server-agentic-readiness-state.json` | Source of truth. Written only by the framework's `progress.py`; do not edit by hand |
| `digital-assistant-server-agentic-readiness-STATE.md` | Generated view of the JSON: preferences, themes, and every finding with its status |

## How to resume

1. Read `digital-assistant-server-agentic-readiness-STATE.md` first, before changing tooling, CI, docs or
   `AGENTS.md`.
2. Respect every preference and every `skipped` or `deferred` finding in it. A skipped finding is final: do not
   propose it again.
3. Work one theme at a time, on its own branch from `dev`, and record each status change with `progress.py`
   in the same branch.

The 2 findings the audit rejected are not queued and stay out of scope.
