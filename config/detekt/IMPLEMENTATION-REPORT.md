# Detekt integration implementation report

## Status and scope

The integration code and applicable runtime checks are implemented on `ci/detekt-verification` in `/Users/I564133/work/engagement-cloud-sdk-detekt-ci`. **Overall acceptance is not complete:** a repository owner must install/confirm `Detekt / Quality gate` as a required protected-branch check. This account is not an admin; protection could not be verified through the API. Nothing was merged, released, tagged, or published. The original worktree and its unrelated staged/untracked work remain untouched.

The operator explicitly approved continuing with the demonstrated Detekt semantic limitations, then authorized worktree commits/pushes and CI verification. That does not remove the limitations or authorize hiding findings.

## Versions and compatibility

| Component | Version / result |
| --- | --- |
| Detekt plugin and engine | `2.0.0-alpha.6` |
| Official libraries extension | `2.0.0-alpha.6` |
| Compose rules | `0.6.8`, compiled against alpha.6 |
| Existing Kotlin / AGP / Gradle | `2.3.10` / `9.1.0` / `9.4.1`, unchanged |
| Authoritative verification | GitHub Ubuntu runner, Temurin JDK 17 |
| Koin rules `1.1.0` | Rejected: old Detekt 1 SPI; loading against Detekt 2 fails `NoClassDefFoundError: io/gitlab/arturbosch/detekt/api/RuleSetProvider` |
| Detekt ktlint wrapper | Not added |
| Alpha.3 / Compose 0.6.0 fallback | Disposable evaluation reproduces the same four minimal KMP/serializer diagnostics; not selected |

Detekt configures and executes both syntax-only and Android/JVM type-resolved tasks. Its standalone engine does not correctly reproduce KMP fragment relationships or serialization-generated declarations: observed production/test diagnostics remain 24/91/8, matching upstream [#9602](https://github.com/detekt/detekt/issues/9602) and [#7531](https://github.com/detekt/detekt/issues/7531). Typed checks around those constructs can miss findings or produce false positives. Alpha.3 and compiler argument changes did not restore accuracy. No compiler-diagnostic suppression flag, broad source exclusion, rule disablement, or SDK source fix was used as a workaround.

## Changed files

- Central declarations/configuration: `gradle/libs.versions.toml`, `build.gradle.kts`, `config/detekt/detekt.yml`.
- Stable commands/instructions: `Makefile`, `README.md`, `AGENTS.md`, `.githooks/pre-commit`.
- Workflows: `.github/workflows/detekt.yml`, `detekt_verification.yml`, `on_push_workflow.yml`, `nightly_workflow.yml`, `publish_artifacts.yml`, `promote_maven_central.yml`.
- Helpers/tests: `scripts/detekt/VerifyDetektPolicy.java`, `report-inventory.py`, `experimental-policy.py`, `verify-merged-reports.py`, `verify-new-findings.py`, `staged-index.py`, `merge-staged-baselines.py`, `install-hooks.sh`, `verify-staged.py`, `test_verification.py`, `test_ci_wiring.py`, `test_staged.py`.
- Evidence/baselines: all 28 XML files under `config/detekt/baseline/<module>/<task>.xml`, `config/detekt/initial-inventory/`.
- Review/behavior documentation: `ADOPTION-REPORT.md`, `CI-ENFORCEMENT.md`, `CI-VERIFICATION.md`, `FINDINGS-REVIEW.md`, `STAGED-ANALYSIS.md`, and this report under `config/detekt/`.

No module build file or maintained SDK Kotlin source was modified. Existing Android Lint jobs are byte-for-byte unchanged. Documentation-only deployment and emergency revocation were not turned into SDK static-analysis publishing paths.

## Policy and coverage

The YAML explicitly configures every selected rule: **273 rules from 11 pinned runtime providers** (built-ins, libraries, Compose). `buildUponDefaultConfig=false`, `allRules=false`, validation/exhaustiveness enabled, warnings treated as errors. `VerifyDetektPolicy.java` discovers the actual engine/extension providers, not a remote or self-copied catalog; it rejects omitted/obsolete rules, non-Boolean activation, and unknown configured options.

Libraries rules load only in published modules; Compose rules load in the three Compose-bearing modules. Gradle script analysis uses built-ins. The canonical aggregate analyzes all seven modules' maintained common/Android/iOS/JS production/tests, with Android/JVM compilation tasks where available. Native/JS tasks remain syntax-only. Notification-service JVM tasks are selected on non-macOS hosts. Redundant SDK common passes are omitted where compilation tasks already cover them; remaining variant/compilation overlap is preserved as distinct context.

Existing rule-specific test exemptions recognize AGP 9 androidHostTest/androidDeviceTest names. Material2 icons are already exempt in pinned Compose rules; no broad component exception was added. Ordinary TODOs remain allowed after a diagnostic inventory found one roadmap comment; FIXME/STOPSHIP remain forbidden.

## Initial inventory, reports, and baselines

The complete enforced-policy CI inventory contains **3,102 occurrences**: **1,560 production, 1,533 tests, 9 scripts**, all reported at error severity. Full module/task/rule/source-set/category counts are in `initial-inventory/summary.json`. Largest rules include LibraryCodeMustSpecifyReturnType 921, NewLineAtEndOfFile 713, LibraryEntitiesShouldNotBePublic 446, and MaxLineLength 306.

HTML/XML/SARIF were preserved before baseline generation. Complete original reports remain archived in Git at `initial-inventory/reports.tar.gz`; summaries, every report hash, provenance, and baseline SHA-256 values are in `adoption-proof.json` and `baseline-reconciliation.json`. Original older local spike inventory (3,691 occurrences / 3,476 identities) is retained in the untouched original worktree; it is not silently substituted for the corrected canonical CI inventory.

**28 active baselines / 2,958 IDs** exactly reconcile with per-task SARIF rule/file/entity-signature identities; zero orphan or missing IDs. ManuallySuppressedIssues is empty. Forced regeneration was byte-identical. Baseline IDs can collapse repeated occurrences (144 in the per-task inventory); this is distinguished from report merging. All per-file counts and paths are machine-readable in `baseline-reconciliation.json`.

The official XML merger retains 3,080 distinct entries; SARIF retains 2,937 distinct rule/path/signature identities across contexts. Verifiers prove every distinct per-task finding survives. HTML reports include a repository-owned index. Report locations are repository-relative; SARIF source-root URI metadata follows the tool's normal format.

## Experimental rules

The complete disposable policy enables only the nine requested experimental rules; enforced YAML is never overwritten and baseline generation rejects experimental config. Counts: CognitiveComplexMethod 12, CoroutineLaunchedInTestWithoutRunTest 0, GlobalCoroutineUsage 1, SuspendFunSwallowedCancellation 20, Deprecation 4, ElseCaseInsteadOfExhaustiveWhen 11, MissingUseCall 23, NullableToStringCall 27, PropertyUsedBeforeDeclaration 0. Experimental counts and representative correctness/false-positive considerations are in `ADOPTION-REPORT.md` and `experimental-summary.json`. None was promoted or included in enforced baselines.

## Commands and outcomes

| Command / scenario | Verified outcome |
| --- | --- |
| `./gradlew validateDetektPolicy` | 273 explicit rules verified against 11 providers; pass |
| Omit rule / bad active / unknown extension option | Fail; restoring policy restores pass |
| `./gradlew detektAll --dry-run` | Canonical graph, including Linux JVM tasks; no runtime/device/Node setup dependency |
| `./gradlew check --dry-run` | No hidden full Detekt dependency |
| Full `detektAll -Pdetekt.inventory=true` in CI | Initial reports retained; compiler/config/tool failures still blocking |
| `detektBaselineAll` and forced regeneration in CI | 28 files / 2,958 identities, exact reconciliation and identical hashes |
| `make detekt-ci` in normal CI | Pass, zero new findings, no baseline-generation task |
| New FIXME / nullable-assertion probes | Expected ForbiddenComment / UnsafeCallOnNullableType failures (`make` exit 2) |
| Remove probes | Pass; baselines unchanged; probes removed |
| `make detekt-staged` on intended staged Kotlin work | Pass; only index-snapshot staged tasks run |
| Index violation / working-tree clean | Fail with reproduction command |
| Index clean / working-tree violation | Pass; working-tree edits preserved |
| No Kotlin / deleted-only Kotlin | Immediate pass without Gradle |
| Rename with spaces, baselined file | Pass; newline path safety also unit-tested |
| Installer twice / unrelated hooks | Idempotent local setup in disposable repos; unrelated default/local/global setup refused without confirmation |
| Configuration cache | Full gate and staged commands pass; reuse verified |
| `python3 -m unittest ...` | 24 tests pass; representative mutations fail |
| `actionlint` on changed workflows | Pass |
| Existing FCM/HMS `assembleRelease` and `lintRelease` in CI | Pass, unchanged source/jobs/Make Lint semantics |

The complete gate is CI-only; normal developer/agent/hook analysis is `make detekt-staged`. Staging snapshots and unions live under ignored build output; no auto-correction, real-index change, or committed baseline mutation occurs. Staged analysis is intentionally incomplete; full CI remains pending for subsequent changes.

## CI and hook behavior

The reusable early Ubuntu job is shared by normal PR/push, nightly, publishing, and promotion. All SDK artifact/tag/CDN paths explicitly require Detekt success, including `!cancelled()` jobs. Promotion analyzes the requested release tag, not an unrelated current branch. Reporting includes quality results. Release workflows were not dispatched during verification.

Reports upload with `if: always()`, seven-day retention (repository maximum). SARIF dashboard upload is opt-in, requires supported repository settings, and is skipped for forks, Dependabot, or absent reports; normal SARIF artifacts are retained regardless. Live privileged/fork upload was not exercised with production tokens.

`make install-git-hooks` is opt-in/local and never silently overwrites unrelated hook setup. It installs only pre-commit; no full-analysis pre-push hook. `SKIP_DETEKT=1` is a transparent emergency local bypass, never a CI exemption. No hooks were installed in the real worktrees during tests.

## Runtime proof

- Initial complete inventory: [44101159532](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44101159532).
- Baseline adoption/experimental/determinism/failure-restoration: [44106374035](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44106374035), 7m9s.
- Committed baselines with no regeneration: [44109500692](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44109500692), 5m30s.
- Exact reusable production job and read-only consumer: [44115700328](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44115700328).
- Production workflow wiring/mutation-tested guards: [44119399221](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44119399221).
- Final staged/Lint/build regression proof, commit `ebf3c503`: [44278334859](https://github.com/emartech/engagement-cloud-sdk/actions/runs/44278334859). Production job 5m22s; verification job 8m26s; every step succeeds.

Final CI staged scenarios: cold 18.50s, warm **1.14s**, staged violation/clean working tree fails in 1.53s, clean index/violating working tree passes, rename/baselined-file passes, deletion/no-Kotlin skips. Source/index/baselines unchanged and probes removed. Detailed run metadata, logs, reports and SHA-256 manifests are locally preserved under `verification-results/<run-id>/` in the verification worktree; those generated copies are not automatically committed.

## Deviations, risks, and remaining acceptance

- Accepted upstream semantic accuracy limitations remain; no claim of complete analysis around affected declarations.
- Early local drafts advanced despite warnings and used macOS/JDK 25 rather than the required CI environment. Their reports are preserved as provisional/history; authoritative adoption and final verification were repeated on real Ubuntu/JDK 17 CI before final baseline adoption.
- Local Docker verification hit shared disk exhaustion; only this run's temporary cache was relocated. Network status-watch timeouts were not treated as CI failures. GitHub results confirm success.
- Cold staged startup exceeds the warm 15-second target; warm runtime meets it. Partial staged analysis can miss cross-file/type-dependent findings, by design.
- The branch's ordinary push matrix remains temporarily excluded to avoid unrelated expensive jobs/notifications while a dedicated no-publish verification workflow runs. No code path skips the actual Detekt gate.
- Baseline identity collisions/legacy suppression semantics remain intrinsic tool limitations; a new finding with an already-baselined identity may not be distinguishable. Baselines are never regenerated by normal CI.
- Alpha upgrades require catalog, compatibility, and preserved-minimal-reproducer revalidation. Narrow Compose/test API policy follow-ups need explicit approval, not automatic exceptions.
- A repository owner must require **`Detekt / Quality gate`** on protected merge branches. This external setup is not verified or applied; **overall integration acceptance remains open** until it is confirmed. See `CI-ENFORCEMENT.md` for exact setup and fail-closed historical-tag promotion behavior.

The implementation is ready for review on its branch, but not merged or declared fully accepted. No unrelated original work was removed, staged, or committed. The original worktree Git status and selected file hashes remained unchanged throughout worktree verification.
