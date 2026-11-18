# Agent verification contract

## Kotlin changes

- Stage only the intended `*.kt`/`*.kts` changes, then run `make detekt-staged` before reporting Kotlin work complete. It analyzes Git index content, not unstaged source edits, using the pinned engine, module plugins, shared policy, and committed baseline identities.
- Do not run `detektAll` or `make detekt-ci` locally during normal agent/developer work. Complete analysis belongs to CI; explicit integration verification must use CI or a CI-equivalent environment.
- Report the exact staged command and outcome. A staged pass is incomplete analysis: clearly say the complete gate remains pending the required CI result.
- Never regenerate/edit a baseline to absorb a new finding unless the operator explicitly requests baseline maintenance. Never add broad exclusions or suppressions merely to obtain a pass.
- Do not auto-correct or mutate source while verifying. Preserve unrelated working-tree, staged, and untracked work. Do not commit/push unless requested.

## Repository commands

- `make detekt-staged`: developer/agent staged-index analysis.
- `make detekt-ci`: complete baselined gate, CI only.
- `make lint`: existing Android Lint behavior; its environment requirements are unchanged.
- `make install-git-hooks`: explicit, repository-local opt-in hook installation. It refuses to silently replace unrelated hook setup.

The canonical KMP matrix is in Gradle; do not duplicate its task list here. See `config/detekt/CI-ENFORCEMENT.md` and `config/detekt/ADOPTION-REPORT.md` for CI enforcement, accepted upstream semantic limitations, and preserved initial findings.
