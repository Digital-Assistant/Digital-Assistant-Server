@AGENTS.md

## Claude Code notes
- Prefer the codebase-memory MCP (`search_graph`, `trace_path`, `get_code_snippet`) over grep for code questions; the index project name is derived from this directory.
- Do not start the server or run `local-up.sh` unless the task needs it; infrastructure lives in the sibling repo `Digital_Assistant_microServices`.
