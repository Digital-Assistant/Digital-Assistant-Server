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

## Plain language

Write for a new engineer who knows Git and Java but not this repository or its terms. The audit check `DOC-05`
measures rules 2 and 6 on every page under `docs/`. The repository has no prose-check script of its own, so check
your own pages by hand.

1. **Bottom line first.** Put the answer, the result or the decision in the first two lines.
2. **Short sentences.** At most 20 words in a numbered step. At most 25 words in other text.
3. **One instruction per sentence.** Write steps in the imperative: "Run the tests", not "You should run them".
4. **Active voice.** Say who does what: "The service reads the file", not "The file is read".
5. **One word, one meaning.** Use the same term every time. Do not change words for variety.
6. **No vague or padded words.** Do not write `simply`, `just`, `obviously`, `clearly`, `basically`, `easily`,
   `utilize`, `leverage`, `in order to`, `and/or` or `etc.`. Name the real items instead of `etc.`.

Example. The "before" text breaks rules 2, 4 and 6 on purpose, so the check skips it:

<!-- prose-check off -->

> Before: In order to ensure that the build is green, it is recommended that the tests are run by the
> contributor prior to the proposal of a change.
>
> After: Run the tests before you propose a change.

<!-- prose-check on -->

A text that must stay word for word, such as a quoted prompt, goes between the same two comments. Do not reword it.

## Diagrams

A diagram often explains a flow faster than text. Every explanation or tutorial page has at least one Mermaid
diagram. An index page does not need one. Pick the type by what you show:

| You show | Mermaid type |
|---|---|
| Steps and choices in a process | `flowchart` |
| Who calls whom, in order | `sequenceDiagram` |
| The states of one thing and how it moves between them | `stateDiagram-v2` |
| Data shapes and how they relate | `classDiagram` or `erDiagram` |

- Keep a diagram to about 12 nodes. Split a larger one.
- Write one sentence before the diagram that says what it shows.
- Validate every block before you commit it. The docs-lint job renders each one.

## Decisions

- Status is `proposed` until the owner accepts it. The owner changes the status, not the writer.
- State when a rationale is inferred from the code. If the repository gives no reason, write "Rationale not
  recorded; owner to fill in".
- Add the record to the [ADR index](adr/README.md) in the same change.

## Lint ratchet

[scripts/ci/docs-lint.sh](../scripts/ci/docs-lint.sh) fails on any rule violation in the files listed in
`scripts/ci/docs-lint-paths.txt` and only reports on all other Markdown. New pages go in the list. A file leaves the
report-only group by being fixed and then added to the list. Existing files are not mass-reformatted.
