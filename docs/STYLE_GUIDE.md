# Documentation style guide

Scope: every Markdown page in this repository. Code rules live in [CONVENTIONS.md](../CONVENTIONS.md); this page
does not restate them.

## Where things live

- `docs/` is the one home for permanent knowledge. [README.md](README.md) indexes it; add every new page there.
- Decisions go in [adr/](adr/README.md), one file per decision, started from the template.
- The map of the code goes in [architecture/overview.md](architecture/overview.md).
- Scratch work, plans and drafts stay out of version control.
- Do not restructure `agentic-readiness-report/` (frozen first-audit report).

## Writing rules

- Pick the page type first: tutorial (learn by doing), how-to (one task, steps, a check), reference (exact facts),
  or explanation (why). Do not mix types on one page.
- Describe only what exists. Mark anything planned as planned.
- Cite code by repository path, for example `src/main/java/com/nistapp/uda/index/RestApplication.java`.
- Use relative links between pages.
- Never put values from env files, credentials, realm exports or keystores in a page; name the variable instead.
- Do not copy text from private repositories or workspaces. Describe such a dependency in one sentence.

## Format

- ATX headings (`#`), one H1 per page, at most four levels; blank line around headings, lists, tables and code.
- Fenced code blocks with a language tag.
- Diagrams are Mermaid in fenced blocks, validated before saving (the docs-lint job renders them).
- Line length 120. The rules are in [../.markdownlint-cli2.yaml](../.markdownlint-cli2.yaml); run
  `scripts/ci/docs-lint.sh` before pushing.

## Decisions

- Status is `proposed` until the owner accepts it. The owner changes the status, not the writer.
- State when a rationale is inferred from the code. If the repository gives no reason, write "Rationale not
  recorded; owner to fill in".
- Add the record to the [ADR index](adr/README.md) in the same change.

## Lint ratchet

[scripts/ci/docs-lint.sh](../scripts/ci/docs-lint.sh) fails on any rule violation in the files listed in
`scripts/ci/docs-lint-paths.txt` and only reports on all other Markdown. New pages go in the list. A file leaves the
report-only group by being fixed and then added to the list. Existing files are not mass-reformatted.
