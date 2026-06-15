# Implementation Plan: Detekt Static Analysis Integration

**Spec:** [SPEC.md](./SPEC.md)
**Branch:** `feat/detekt-integration`
**Status:** Draft — pending user approval
**Last updated:** 2026-06-15

---

## Overview

Wire detekt 2.0.0-alpha.3 into the 7-module Kotlin Multiplatform repo with type-resolution-aware analysis, a single committed baseline, fail-fast CI gating via SARIF upload, and developer-facing docs. Verification gate (SPEC §10) has already passed; foundation work in `build.gradle.kts`, `gradle/libs.versions.toml`, `config/detekt/detekt.yml`, and `config/detekt/baseline.xml` is in place. **What remains is verification, CI wiring, parity testing, and documentation** — the runnable proof that the wiring works end-to-end.

## What's already done (do NOT redo)

Surveyed before writing this plan — these match SPEC §6 / §7 and need only verification, not re-implementation:

- ✅ `gradle/libs.versions.toml`: `detekt = "2.0.0-alpha.3"`, `composeRules = "0.6.0"`, four `detekt-rules-*` libraries, `detekt` plugin alias.
- ✅ `build.gradle.kts`: `alias(libs.plugins.detekt)` applied at root, `subprojects { apply("dev.detekt") }` block, per-task report config (HTML/SARIF/checkstyle XML/MD), absolute-path-based exclude for `build/generated/` and `build/sources/`, `buildUponDefaultConfig = false`, `parallel = true`, `basePath` set to root.
- ✅ Custom `detektProjectBaseline` task that walks every source set across every subproject and writes ONE `config/detekt/baseline.xml` (works around detekt 2.0's per-task baseline default).
- ✅ `config/detekt/detekt.yml` (387 `active:` keys; rules from all enabled rule sets explicitly set per SPEC AC-11).
- ✅ `config/detekt/baseline.xml` generated and committed.
- ✅ Kotlin-version-alignment guard comment in `build.gradle.kts` documenting V-6 caveat.
- ✅ SPEC §10 verification log (V-1..V-6 all ✅, with documented V-6 caveat).

## What's NOT done (this plan)

- ❌ **D-2 fix:** Aggregate `detekt` task wired to per-source-set tasks (`detekt*SourceSet`). _Partially applied to `build.gradle.kts` in this session._
- ❌ **D-4 fix:** Generated-code exclusion working on per-source-set tasks (currently `build/generated/sqldelight/**` analyzed; AC-3 fails).
- ❌ **D-3 handling:** CI workflow materializes `google-services.json` from secret + invokes Android type-resolution detekt tasks separately.
- ❌ Baseline regenerated against the corrected task graph (will balloon — current 1.18 MB → expected 2-3× after D-2 + D-4 fixes).
- ❌ Smoke run: `./gradlew detekt` exits 0 against the regenerated baseline (AC-1, AC-4).
- ❌ Configuration-cache + build-cache compatibility verified (AC-5, AC-6) on the corrected wiring.
- ❌ Generated-code exclusion proven empirically by grepping SARIF (AC-3) post-D-4 fix.
- ❌ Linux runner skipping iOS source-set tasks cleanly (AC-7).
- ❌ Reviewer-on-macOS verification of iOS detekt tasks (AC-7b).
- ❌ CI job in `on_push_workflow.yml` + SARIF upload via `github/codeql-action/upload-sarif@v4` (AC-9, US-2).
- ❌ Negative test: PR with deliberate violation surfaces SARIF annotation AND fails the build.
- ❌ README detekt section + CONTRIBUTING note on baseline policy (AC-12).
- ❌ Final commit history cleanup on `feat/detekt-integration`.

## Architecture decisions (already locked by SPEC + this session)

- **Single subprojects {} block** in root `build.gradle.kts`, not buildSrc convention plugin (SPEC §6).
- **All rules explicit, `buildUponDefaultConfig = false`** (SPEC §7, AC-11).
- **Single shared baseline** at `config/detekt/baseline.xml` — implemented via custom `detektProjectBaseline` task because detekt 2.0's default is per-task baselines that overwrite each other.
- **Day-one fail-fast** — no `continue-on-error`, no `ignoreFailures = true` (SPEC §1, US-2, §9).
- **Pin detekt 2.0.0-alpha.3, do NOT fall back to 1.23.x** (SPEC §10).
- **Linux CI runs the non-iOS source-set detekt tasks; iOS tasks verified locally on macOS by reviewer** (SPEC §11 phase 3, AC-7b).
- **(NEW, this session) Aggregate `detekt` covers `*SourceSet` only.** Android type-resolution tasks (`detektMain`, `detektMainAndroid`, `detektHostTestAndroid`, `detektDeviceTestAndroid`) are excluded from the aggregate because they pull in the full Android compilation graph (needs `google-services.json`). CI invokes them separately after materializing the secret. Local devs run the aggregate without setup; for type-resolution rules locally, run `make build-android` first, then `./gradlew detektMainAndroid`. (SPEC D-3.)

## Risks & mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| D-4 fix turns out to need a structural change to the source filter | Medium | T1.5 timeboxed: try 3 approaches (`doFirst { setSource }`, per-task `exclude` with absolute paths, KGP source-set filtering). If none work in <90 min, document the leak in baseline and file follow-up. |
| Regenerated baseline balloons past GitHub's 100 MB hard file limit | Low | Current 1.18 MB; even 10× growth stays well under limit. Worst case: split per-module baselines (would require reworking `detektProjectBaseline`). |
| CI google-services.json materialization breaks if secret format changes | Low | Reuse the exact `davidSchuppa/base64Secret-toFile-action@v3` step from `nightly_workflow.yml:135-141` so we inherit any maintenance there. |
| Configuration-time-resolution warnings escalate to errors on Gradle bump | Medium | D-5: documented as known upstream; tracked in §12 follow-ups. If Gradle 10 lands before detekt 2.0 GA, may need to pin Gradle. |
| Per-source-set tasks have hidden iOS dependencies that fail on Linux CI even after isMac guard | Low–Medium | T7 verifies. The repo's existing `isMac` pattern in `engagement-cloud-sdk/build.gradle.kts:31` skips registering iOS targets entirely on Linux, so iOS source-set detekt tasks won't exist there. |
| SARIF too large for `upload-sarif` (10 MB / 5000 results limit) | Low–Medium | Baseline absorbs current findings → SARIF should be near-empty on green runs. Monitor first CI run. |
| Compose-rules 0.6.0 fires on commonComposeMain but produces noise | Low | Already absorbed by baseline; treat new findings on PR as actionable |

## Open questions

- **Q1.** Should the CI detekt job run on `ubuntu-latest` (cheap, but skips iOS source-set tasks) or `macos-latest-xlarge` (covers iOS too, but expensive)? — **Default proposal:** ubuntu-latest, mirroring the SPEC §11 phase-3 split (CI Linux + reviewer macOS for iOS coverage). T7 must make this explicit so reviewers know what's covered where.
- **Q2.** Does the existing `lint` job stay (AGP lint), or do we replace/merge it with detekt? — **Default proposal:** keep both; SPEC §1 non-goal #1 says detekt augments, doesn't replace AGP lint.
- **Q3.** Slack notification on detekt failure — reuse existing `reporting` job's Slack hook (it already aggregates `needs.*.result`) or skip? — **Default proposal:** add `detekt` to the `reporting` job's `needs:` list so existing Slack reporting catches it without new code.

If any answer differs from the default proposal, raise before T7 starts.

---

## Task list

### Phase 1 — Prove the local wiring (foundation verification)

#### Task 1: Smoke-run `./gradlew detekt` against the committed baseline ✅ COMPLETED

**Result:** `./gradlew detekt` exits 0 in 31s. **But:** revealed D-2 (aggregate task NO-SOURCE on KMP modules) — see SPEC §10 post-T1 discoveries. T1.5 below addresses the actual coverage gap.

---

#### Task 1.5: Wire per-source-set detekt tasks + fix generated-code leakage (D-2 + D-4)

**Description:** Two related fixes:
1. **D-2** (already partially applied): Aggregate `detekt` task in each subproject must depend on its `detekt*SourceSet` tasks so KMP commonMain/androidMain/iosMain/jsMain/webMain are actually analyzed. Already in `build.gradle.kts:102-106` via `tasks.named("detekt").dependsOn(tasks.withType<Detekt>().matching { it.name.endsWith("SourceSet") })`. Verify by running `./gradlew :engagement-cloud-sdk:detekt --dry-run` and confirming `detektCommonMainSourceSet`, `detektAndroidMainSourceSet`, `detektJsMainSourceSet`, `detektIosMainSourceSet` (on macOS) appear in the graph.
2. **D-4 (BLOCKING)**: The current `tasks.withType<Detekt>().configureEach { setSource(source.filter { ... }) }` filter does NOT prevent generated-code analysis on per-source-set tasks. KGP populates `source` after configureEach runs, re-introducing `build/generated/sqldelight/...`. Confirmed by running `:engagement-cloud-sdk:detektCommonMainSourceSet` and seeing 159 violations against generated SQLDelight code.

**Approach for D-4 (try in this order, stop at first that works):**
- (a) Move the filter into `tasks.withType<Detekt>().configureEach { doFirst { setSource(source.filter { ... }) } }` — runs at execution time, after KGP source population.
- (b) Per-task `exclude` with absolute path patterns matched against rooted paths — but `SourceTask.exclude` evaluates relative to source roots, which is what causes the original problem.
- (c) Override `source` via a Provider that derives from `kotlinSourceSets.main.kotlin` minus generated dirs.
- (d) Drop the FileCollection filter, instead pre-process: `tasks.withType<Detekt>().configureEach { source.from(...); setSource(source.minus(fileTree("build/generated"))) }`.

**Acceptance criteria:**
- [ ] `./gradlew :engagement-cloud-sdk:detekt --dry-run` lists at least: `detektCommonMainSourceSet`, `detektAndroidMainSourceSet`, `detektJsMainSourceSet`, `detektCommonComposeMainSourceSet`.
- [ ] `./gradlew :engagement-cloud-sdk:detektCommonMainSourceSet` runs and **does NOT report violations against any path containing `build/generated/`**.
- [ ] No Android type-resolution task (`detektMain`/`detektMainAndroid`/etc.) is in the aggregate graph (kept out by `endsWith("SourceSet")` filter).
- [ ] Configuration-time-resolution warnings remaining are exclusively from upstream detekt (D-5), not from our `detektProjectBaseline` or `subprojects {}` block.

**Verification:**
- [ ] `./gradlew :engagement-cloud-sdk:detekt --dry-run --console=plain | grep detekt` — expected source-set tasks present.
- [ ] `./gradlew :engagement-cloud-sdk:detektCommonMainSourceSet 2>&1 | grep 'build/generated' || echo "GENERATED-CODE-EXCLUSION OK"`.

**Dependencies:** T1 (proves baseline wiring); blocks T1.6, T2, T3, T4, T6.

**Files likely touched:**
- `build.gradle.kts` (the `subprojects {}` block, lines ~74-92).

**Estimated scope:** S — 30-90 min depending on which D-4 approach lands.

---

#### Task 1.6: Regenerate baseline against the corrected task graph

**Description:** With D-2 + D-4 fixed, the source coverage just expanded substantially (KMP source sets now actually scanned, generated code now actually excluded). The committed `baseline.xml` (1.18 MB, generated against the JVM-only task graph) no longer matches reality. Regenerate it via `./gradlew detektProjectBaseline` and commit the result. Inspect the new file size and finding count to make sure the explosion is sane (not e.g. 100 MB).

**Acceptance criteria:**
- [ ] `./gradlew detektProjectBaseline` runs to completion.
- [ ] New `config/detekt/baseline.xml` written; old version replaced.
- [ ] `wc -l config/detekt/baseline.xml` produces a number that is at most ~5× the previous 7,598 lines (i.e. ≤40k lines). If larger, investigate before committing.
- [ ] After commit, `./gradlew detekt` (on the local Linux/macOS dev box) exits 0.
- [ ] Spot-check the new baseline: `grep -c '<ID>' config/detekt/baseline.xml` to count findings; sanity-check by category.

**Verification:**
- [ ] Capture before/after line counts and finding counts in the PR body.

**Dependencies:** T1.5

**Files likely touched:**
- `config/detekt/baseline.xml` (regenerated; staged for commit).

**Estimated scope:** XS — but waits ~30 s for the full project scan.

---

#### Task 2: Empirically verify generated-code exclusion (AC-3)

**Description:** Confirm post-D-4 fix that no SARIF file references `build/generated/**` or `build/sources/**`.

**Acceptance criteria:**
- [ ] After T1.6, `find . -name '*.sarif' -path '*/build/reports/detekt/*'` produces SARIF files.
- [ ] `grep -r 'build/generated' <each-sarif>` returns zero matches.
- [ ] `grep -r 'build/sources' <each-sarif>` returns zero matches.

**Verification:**
- [ ] Document the exact grep commands and results in the task notes (or PR body).

**Dependencies:** T1.5, T1.6

**Files likely touched:** None unless the filter is leaking.

**Estimated scope:** XS

---

#### Task 3: Configuration cache + build cache compatibility (AC-5, AC-6)

**Description:** Verify detekt tasks don't break configuration cache or build cache. SPEC §9 says "Run with `--configuration-cache` in CI to catch regressions early" — we need to know if it's safe to enable that flag.

**Acceptance criteria:**
- [ ] `./gradlew detekt --configuration-cache` succeeds twice in a row; second run reports "Reusing configuration cache".
- [ ] `./gradlew detekt` (no source changes) on second run shows tasks `FROM-CACHE` or `UP-TO-DATE`.
- [ ] If configuration cache fails, capture exact error and decide: fix vs. document as known limitation in README.

**Verification:**
- [ ] Capture command output snippets showing the cache hit lines.

**Dependencies:** T1.5, T1.6

**Files likely touched:** Possibly `build.gradle.kts` if a `Provider` API needs adjusting.

**Estimated scope:** XS — S if a cache violation needs fixing.

---

### Checkpoint A — Local wiring proven

Before moving to CI:
- [x] T1 done (revealed D-2)
- [ ] T1.5 done (D-2 wiring + D-4 generated-code fix)
- [ ] T1.6 done (regenerated baseline committed)
- [ ] T2 + T3 green
- [ ] Any deviations from SPEC §7 documented in the plan
- [ ] User reviews and approves before Phase 2

---

### Phase 2 — CI integration

#### Task 4: Add `detekt` job to `on_push_workflow.yml`

**Description:** Add a new job (parallel to `lint`/`build`/`test`) that runs detekt on Ubuntu, including Android type-resolution tasks (D-3 — needs `google-services.json` materialized first), uploads the aggregated SARIF to GitHub code-scanning via `github/codeql-action/upload-sarif@v4`, and **fails the build on any new violation outside the baseline** (no `continue-on-error`).

**Acceptance criteria:**
- [ ] New `detekt` job added; runs on every push (excluding `dependabot/**` matching existing convention) and every PR.
- [ ] Uses `ubuntu-latest` (Q1 default).
- [ ] Reuses the existing JDK 17 + `gradle/actions/setup-gradle@v6.1.0` setup pattern from the `lint` job.
- [ ] **Materializes `google-services.json`** via `davidSchuppa/base64Secret-toFile-action@v3` from `GOOGLE_SERVICES_JSON_BASE64` secret before invoking detekt. (Mirrors `nightly_workflow.yml:135-141`.)
- [ ] Invokes detekt as: `./gradlew detekt :engagement-cloud-sdk:detektMainAndroid :engagement-cloud-sdk:detektHostTestAndroid :androidApp:detektMain :engagement-cloud-sdk-android-fcm:detektMain :engagement-cloud-sdk-android-hms:detektMain --configuration-cache` (T3 determines if `--configuration-cache` is safe; if not, drop with comment).
- [ ] Uploads SARIF via `github/codeql-action/upload-sarif@v4` matching the codeql.yml pattern; aggregates SARIF from every module's `build/reports/detekt/detekt.sarif`.
- [ ] No `continue-on-error: true`; no `ignoreFailures = true`.
- [ ] Permissions block includes `security-events: write` (required for SARIF upload).
- [ ] HTML reports uploaded as a workflow artifact (`actions/upload-artifact@v4`) for developer inspection on failure.
- [ ] `reporting` job's `needs:` list updated to include `detekt` so Slack catches failures (Q3 default).

**Verification:**
- [ ] Push branch and observe the job runs.
- [ ] Confirm green run on baseline-clean state.
- [ ] Inspect the SARIF upload step's logs for the upload confirmation.
- [ ] Confirm the `lint`/`build`/`test`/`publish-*` jobs do NOT change behavior.

**Dependencies:** Phase 1 checkpoint A (T1.5, T1.6, T2, T3)

**Files likely touched:**
- `.github/workflows/on_push_workflow.yml` (one new job + one `needs:` line update)

**Estimated scope:** S

---

#### Task 5: Negative test — deliberate violation triggers PR annotation + build failure

**Description:** Open a throwaway PR that introduces a single deliberate violation (e.g., an unsuppressed `MagicNumber` in a non-test path) and confirm the end-to-end gate works: code-scanning annotation appears in "Files changed", and the `detekt` job fails the build.

**Acceptance criteria:**
- [ ] Test commit added on a temporary branch (NOT merged).
- [ ] CI run shows `detekt` job failed on that branch.
- [ ] PR "Files changed" tab shows the inline annotation via SARIF upload.
- [ ] Test branch deleted after verification — no merge.

**Verification:**
- [ ] Screenshot or link to the failed CI run captured in PR body or plan notes.

**Dependencies:** T4

**Files likely touched:** None permanently. Temporary one-line edit in any `*.kt` file under `engagement-cloud-sdk/src/`.

**Estimated scope:** XS

---

### Checkpoint B — CI gate works

- [ ] T4 + T5 green
- [ ] PR annotation visually confirmed
- [ ] Build genuinely fails on new violation

---

### Phase 3 — Parity verification

#### Task 6: AC-7b — Reviewer runs iOS detekt tasks on macOS

**Description:** SPEC AC-7b explicitly defers iOS source-set detekt to local macOS verification by the reviewer (CI runs Linux for the regular detekt job). Confirm `detektMetadataIosMain`, `detektNativeIosArm64Main`, `detektNativeIosX64Main`, `detektNativeIosSimulatorArm64Main` (or whatever names detekt 2.0.0-alpha.3 actually generates — discover via `tasks --all`) run green on a Mac.

**Acceptance criteria:**
- [ ] On macOS: `./gradlew tasks --all | grep -i 'detekt.*ios'` lists at least one iOS source-set task.
- [ ] Each listed iOS detekt task runs successfully (or no-ops cleanly with "no source") with the committed baseline.
- [ ] Task name list captured in the PR body or CONTRIBUTING.md so future maintainers can repeat the check.

**Verification:**
- [ ] Reviewer confirmation in PR description (manual sign-off).

**Dependencies:** T1

**Files likely touched:** None expected. If iOS source-sets are missing from the rule-set's task generation, may need to extend the subprojects {} block — but SPEC V-5 says these names exist in detekt 2.0.

**Estimated scope:** XS — S if a wiring gap exists.

---

#### Task 7: AC-7 — Linux runner skips iOS tasks cleanly

**Description:** The CI Linux runner from T4 must skip iOS tasks without failing. SPEC AC-7 allows either "tasks not present on Linux" OR "tasks no-op". Verify which one happens in practice.

**Acceptance criteria:**
- [ ] On the CI Linux run from T4, no iOS detekt task fails.
- [ ] Either: (a) iOS tasks are absent from the Linux task graph, OR (b) they run and report "no source / skipped".
- [ ] Behavior captured (a vs b) in the PR body for future reference.

**Verification:**
- [ ] Read the T4 CI run log for `detekt*Ios*` task lines.

**Dependencies:** T4

**Files likely touched:** Probably none. If iOS tasks DO fail on Linux, this becomes an S task gating on either `onlyIf { isMac }` or removing them from the Linux task graph — match the existing `isMac` guard pattern used elsewhere in `build.gradle.kts` for Kotlin/Native.

**Estimated scope:** XS — M if a gating fix is needed.

---

### Checkpoint C — Parity verified

- [ ] T6 (macOS reviewer pass) and T7 (Linux clean skip) both confirmed
- [ ] No iOS coverage gap is silently introduced — the split is documented

---

### Phase 4 — Documentation

#### Task 8: README detekt section (AC-12)

**Description:** Add a "Static Analysis (detekt)" section to `README.md` covering: how to run locally, where reports land, the baseline policy, and the macOS-vs-CI iOS split.

**Acceptance criteria:**
- [ ] `README.md` has a new section (≤ 30 lines) documenting:
  - `./gradlew detekt` to run all detekt tasks.
  - `./gradlew detektProjectBaseline` to regenerate the baseline (note: this is the project's custom task, not detekt 2.0's per-task `detektBaselineMain` etc.).
  - Where reports land: `<module>/build/reports/detekt/detekt.{html,sarif,xml,md}`.
  - Baseline policy: never edit by hand; regenerate the file.
  - iOS source-sets: verified locally on macOS, not in CI Linux.
- [ ] Tone matches the rest of the existing README (concise, link-heavy, no marketing).

**Verification:**
- [ ] Render the README locally (or in GitHub preview); confirm headings + links work.

**Dependencies:** T1, T6, T7 (so the documented commands actually work)

**Files likely touched:**
- `README.md`

**Estimated scope:** XS

---

#### Task 9: CONTRIBUTING.md note on handling new detekt findings

**Description:** Add (or create) `CONTRIBUTING.md` guidance: when a new finding fires, options are (1) fix the code, (2) suppress with `@Suppress("RuleName")` plus a code comment explaining why, NEVER (3) edit the baseline by hand. Mention the baseline regen command as the reset hatch when a sweeping fix is applied.

**Acceptance criteria:**
- [ ] `CONTRIBUTING.md` has a "Static analysis findings" section (or equivalent).
- [ ] Mentions `detektProjectBaseline` regen procedure.
- [ ] References SPEC §9 boundaries explicitly so the rule provenance is traceable.

**Verification:**
- [ ] Review the diff; ensure the rule is unambiguous.

**Dependencies:** T8 (consistent style with README)

**Files likely touched:**
- `CONTRIBUTING.md` (likely new file — check existence first)

**Estimated scope:** XS

---

### Phase 5 — Wrap-up

#### Task 10: Commit cleanup + PR-ready state

**Description:** The branch already has uncommitted SPEC.md, config/, and gradle modifications. Land them in a logical commit sequence so the PR reads well.

**Acceptance criteria:**
- [ ] Commits are atomic and logically grouped:
  - "spec(detekt): add SPEC.md for detekt integration"
  - "build(detekt): wire detekt 2.0.0-alpha.3 with single-baseline task"
  - "config(detekt): add explicit-rule detekt.yml + generated baseline.xml"
  - "ci(detekt): add fail-fast detekt job with SARIF upload"
  - "docs(detekt): document detekt usage in README + CONTRIBUTING"
- [ ] Commit messages explain the WHY, not just the WHAT.
- [ ] Branch rebased on latest `main` if needed.
- [ ] `gradle/gradle-daemon-jvm.properties` (currently untracked) decision: commit, gitignore, or remove? — Default: ignore for now, surface to user as a side-question if it appears related.

**Verification:**
- [ ] `git log feat/detekt-integration --oneline` reads as a coherent story.
- [ ] No unintended file changes (`git status` clean).

**Dependencies:** All prior tasks

**Files likely touched:** None (git operations only).

**Estimated scope:** S

---

#### Task 11: Open the PR

**Description:** Open a PR against `main` with a body that:
- Links to SPEC.md.
- Summarizes verification results (V-1..V-6 from SPEC + AC-1..AC-12 from this plan).
- Calls out the reviewer's macOS responsibility for AC-7b.
- Notes the SPEC §10 V-6 caveat about Kotlin version alignment (already in code comments, but reviewers shouldn't have to discover it).
- Includes a link to the negative-test branch (T5) showing the gate worked.

**Acceptance criteria:**
- [ ] PR opened, marked "ready for review".
- [ ] Body includes the AC checklist with [x] marks for each verified item.
- [ ] Reviewer assigned (the same reviewer who acted as plannotator on the SPEC, if applicable).

**Verification:**
- [ ] PR URL captured in the plan.

**Dependencies:** T10

**Files likely touched:** None.

**Estimated scope:** XS

---

### Checkpoint D — Ready to ship

- [ ] All 11 tasks complete
- [ ] All 12 SPEC ACs verifiable from PR body
- [ ] Reviewer signs off

---

## Sizing summary

| Phase | Tasks | Scope | Wall-clock estimate |
|-------|-------|-------|---------------------|
| 1 — Local proof | T1 ✅, T1.5, T1.6, T2, T3 | XS + S + 3×XS | ~2 h (D-4 debug is the variable) |
| 2 — CI gate | T4, T5 | S + XS | ~2-3 h (one CI iteration cycle, +secret materialization step) |
| 3 — Parity | T6, T7 | 2 × XS | ~30 min (macOS hands-on + log read) |
| 4 — Docs | T8, T9 | 2 × XS | ~30 min |
| 5 — Wrap-up | T10, T11 | S + XS | ~30 min |
| **Total** | **13 tasks** | mostly XS, two S | **~½–¾ day end-to-end** assuming D-4 has a tractable fix |

This revised estimate exceeds SPEC §11's roll-up by ~½ day, attributable to the D-2/D-3/D-4 discoveries that weren't in §10's verification gate.

## Verification before starting implementation

- [x] Every task has acceptance criteria
- [x] Every task has a verification step
- [x] Task dependencies are identified and ordered (T1 gates everything; T4 gates T5/T7; T8 gates T9; T10 gates T11)
- [x] No task touches more than ~3 files (T4 touches one workflow file only)
- [x] Checkpoints exist between phases (A/B/C/D)
- [ ] Human has reviewed and approved the plan
