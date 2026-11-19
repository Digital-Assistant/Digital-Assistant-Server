# Documentation index

`docs/` holds the permanent knowledge about this repository. Scratch notes and plans do not belong here.
Rules for writing pages are in [STYLE_GUIDE.md](STYLE_GUIDE.md).

| Page | What it holds |
|---|---|
| [STYLE_GUIDE.md](STYLE_GUIDE.md) | One-page rules for writing and checking docs |
| [architecture/overview.md](architecture/overview.md) | Map of the code: components, entry points, data stores, tenant flow, repository notes |
| [adr/README.md](adr/README.md) | Decision records (index, template, first records) |
| [workflow-profiles.md](workflow-profiles.md) | The `workflow:` label profiles and the six refinement prompts agents apply (moved from `AGENTS.md`) |
| [branch-protection.md](branch-protection.md) | What GitHub requires before a merge to `dev`, and how to re-check it |
| [release-process.md](release-process.md) | How a release is cut from `master` (Release Please), and what agents never do |
| [agentic-readiness/](agentic-readiness/README.md) | The finished first agentic-readiness run, and the live quality baseline file |
| [agentic-readiness-report/](agentic-readiness-report/README.md) | Frozen first-audit report, not current |

Decisions of record are in [adr/](adr/README.md). The map of the code is in [architecture/](architecture/overview.md).
Build and test commands are in [CONTRIBUTING.md, "Commands"](../CONTRIBUTING.md#commands). Setting up and running
the server is in [README.md, "Getting Started"](../README.md#-getting-started-local-development).
