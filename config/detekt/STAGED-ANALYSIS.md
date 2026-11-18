# Staged-index analysis and opt-in hook

`make detekt-staged` reads `git diff --cached --name-only --diff-filter=ACMR -z`, resolves regular stage-0 blobs from the index, and writes only those Kotlin blobs into an ignored generated snapshot. Base64 path records preserve spaces/newlines. Unmerged or symlink Kotlin entries fail visibly rather than linting different content.

The runner serializes snapshot use, disables persistent VFS watching for snapshots replaced outside Gradle, and invokes only `detektStagedAll`. It never runs compilation, the full canonical matrix, devices, Node, or auto-correction. Missing/deleted/non-Kotlin paths do not start Gradle. Source and index are unchanged.

Each module uses the same pinned engine, canonical YAML, appropriate plugin scope, and the union of that module's committed task baselines. Unions are generated under `build/`, never written into committed baselines. Kotlin Gradle scripts use built-ins only, matching their CI analysis context. Staged analysis is syntax-oriented and can miss dependent-file/type-resolution issues; CI remains authoritative.

`make install-git-hooks` is explicit and repository-local. It refuses to shadow unrelated local/global hooks paths or default executable hooks unless `CONFIRM_REPLACE_HOOKS=1` is supplied. It does not delete old hook files, auto-install hooks, or add a pre-push hook. `SKIP_DETEKT=1` is an emergency local bypass, not a CI exemption.

## Verified scenarios

`test_staged.py` exercises actual disposable Git repositories/indexes and hook configuration. It checks snapshot fidelity, no-Kotlin fast exit, spaces/newlines, symlink rejection, module-isolated baseline unions, unchanged input baselines, installer idempotence, existing local/global/default hooks preservation, explicit confirmation, and static Make `.env` isolation without changing normal/mixed Android Lint goals.

`verify-staged.py` invokes the actual hook/Gradle engine with a private index. Local verification passed:

- no Kotlin: no Gradle, exit 0;
- clean staged content: pass;
- staged violation with clean working tree: expected ForbiddenComment failure and reproduction command;
- clean staged content with violating working tree: pass;
- rename with spaces: pass;
- existing baselined library file: pass;
- deleted-only Kotlin: no Gradle;
- explicit local bypass: pass without Gradle;
- source/index/committed baseline identities unchanged.

Observed warm staged runtime was **0.84s** locally, below the 15-second target; cold configuration can be slower. The verification workflow repeats the real scenarios on Ubuntu/JDK 17 and records timings/outcomes in `build/detekt-verification/staged/`. No hooks were installed in the original or verification worktree during tests; installation tests use disposable repositories.

Mutation checks replacing index blobs with working-tree reads or ignoring inherited global hook paths both failed the test suite. Ordinary developer/agent instructions are in `README.md` and `AGENTS.md`.
