# Detekt baseline-adoption verification

## Evidence

- Initial CI inventory: https://github.com/emartech/engagement-cloud-sdk/actions/runs/44101159532 (`a4eb81b6`).
- Complete adoption/enforcement proof: https://github.com/emartech/engagement-cloud-sdk/actions/runs/44106374035 (`8e342d01`), successful in **7m9s** on Ubuntu/JDK 17 without `.env` or sample-app Google credentials.
- Pinned versions: Detekt/libraries `2.0.0-alpha.6`, Compose rules `0.6.8`; Kotlin `2.3.10`, AGP `9.1.0`, Gradle `9.4.1` unchanged.

## Inventory and baselines

The preserved enforced-policy run contains **3,102 report occurrences**: 1,560 production, 1,533 tests, and 9 Gradle-script findings. It yields **2,958 module/task-specific identities**, with 144 duplicate identity occurrences.

The official report mergers preserve all distinct findings. XML merges 3,102 occurrences to 3,080 distinct entries; SARIF preserves 2,937 distinct rule/path/signature identities across contexts. These differ from per-task baseline counts by design, not because findings were dropped.

**28 module/task baseline files, 2,958 IDs** were generated only after preserving initial reports. Every ID reconciles to a reported rule/file/entity-signature fingerprint; no missing or orphan IDs. ManuallySuppressedIssues is empty in every file. Forced regeneration was byte-identical.

The initial HTML/XML/SARIF reports are durably archived in `initial-inventory/reports.tar.gz`, not replaced by clean baselined reports. `initial-inventory/summary.json`, `baseline-reconciliation.json`, and `adoption-proof.json` provide counts, provenance, report hashes, and every baseline SHA-256. The archive is a deliberate preservation deliverable, not a release/build distribution.

## New-finding enforcement

The complete `make detekt-ci` gate was exercised in CI:

| Scenario | Exit code |
| --- | ---: |
| Unchanged sources before probes | 0 |
| New FIXME comment (`ForbiddenComment`) | 2 |
| Remove comment probe | 0 |
| New nullable assertion (`UnsafeCallOnNullableType`) | 2 |
| Remove type-aware probe | 0 |

`make` reports the underlying Gradle failure as exit code 2. Both failures contain the expected rule ID. Baseline hashes were unchanged throughout and both temporary source probes were removed. The final normal report contains zero unbaselined findings.

After inspection, the verified candidates were adopted on the verification branch. Normal CI no longer generates or edits baselines: it reconciles the committed files against the preserved initial inventory, runs the baselined gate, and repeats the controlled failure/restoration checks.

## Experimental findings — not enforced or baselined

| Rule | Report occurrences |
| --- | ---: |
| CognitiveComplexMethod | 12 |
| CoroutineLaunchedInTestWithoutRunTest | 0 |
| GlobalCoroutineUsage | 1 |
| SuspendFunSwallowedCancellation | 20 |
| Deprecation | 4 |
| ElseCaseInsteadOfExhaustiveWhen | 11 |
| MissingUseCall | 23 |
| NullableToStringCall | 27 |
| PropertyUsedBeforeDeclaration | 0 |

Representative observations:

- Cognitive complexity flags `updateActionsInUserInfo` in the iOS notification service (20 versus threshold 15): useful follow-up review candidate; not promoted.
- GlobalScope and cancellation handling both flag `BroadcastReceiver.goAsync`: its lifetime is tied to Android's pending result, but the catch-all logs rather than rethrowing cancellation. Review coroutine lifetime/cancellation together before promotion or source fixes.
- MissingUseCall flags test-created Ktor clients: assess resource ownership/close behavior rather than treating every occurrence as a confirmed leak.
- NullableToStringCall flags nullable payload logging in the Android sample: literal "null" output may be intentional; a concrete false-positive/policy candidate.
- Deprecation flags legacy notification-priority usage in tests: test compatibility coverage needs consideration before enforcing the rule.
- Enum `else` findings deserve exhaustiveness/compatibility review; zero-count rules are not proven to cover unsupported target semantics.

The diagnostic TODO evaluation found one ordinary roadmap comment in JS HTML `ListView.kt` concerning append-loading presentation. Ordinary TODOs remain allowed; FIXME/STOPSHIP stay forbidden. No experimental rule was promoted into the enforced policy or baselines.

## Limitations and remaining work

The accepted upstream KMP/serialization limitations remain: observed compiler diagnostics 24/91/8 can affect semantic findings. This is not a claim of complete type-resolution accuracy. Rules, source sets, generated-source coverage, and toolchain pins remain intact.

This completes the baseline-adoption verification slice, not the whole integration. Staged-index fidelity and hooks, protected-branch/release enforcement, contributor/agent documentation, Android Lint non-regression, and final acceptance/reporting remain to be completed. No merge, publishing, or tag creation was performed; the original worktree remains untouched.
