# SPEC: Detekt Static Analysis Integration

**Status:** Draft — pending user approval
**Owner:** TBD
**Last updated:** 2026-06-12

---

## 1. Objective

Integrate **detekt** as the static code analysis tool for the `engagement-cloud-sdk` Kotlin Multiplatform project. Detekt should:

- Analyze every Kotlin source set (common, android, js, iosX64/Arm64/SimulatorArm64) across all 7 Gradle modules.
- Produce structured reports (HTML for humans, XML for tooling, SARIF for GitHub code scanning).
- Run locally via a single `./gradlew detekt` command and in CI on every push/PR.
- Establish a **baseline** of existing findings so the rollout is non-blocking for already-shipped code, while **CI fails on any NEW violation** outside the baseline from day one.

### Target users

- **SDK developers** running detekt locally before pushing.
- **CI / reviewers** consuming SARIF in GitHub's code-scanning UI on PRs.
- **Future maintainers** auditing/tightening rules without re-architecting the integration.

### Non-goals

- Replacing existing tooling (e.g. AGP lint, kotlinc warnings) — detekt augments, doesn't replace.
- Auto-formatting / write-mode (`detekt --auto-correct`) is out of scope for v1.
- A "soft launch" warn-only period. Per reviewer direction (plannotator), **CI fails immediately on any new violation**. Existing code is absorbed by the baseline.
- Custom in-house rule set authoring.

---

## 2. Assumptions

> Surfaced explicitly per agent-skills:using-agent-skills §1.

1. The 4 user-confirmed decisions stand:
   - **detekt 2.0.0-alpha.3** (plugin id `dev.detekt`) — explicitly opting into pre-release. **Do not fall back to 1.23.x** (per plannotator review).
   - **Type resolution enabled** via per-source-set tasks (`detektMetadataCommonMain`, `detektJvmMain`, `detektMetadataIosMain`, etc.).
   - **Rule sets:** all available first-party rule sets + `formatting` (ktlint-wrapper) + `compose-rules` (mrmans0n) + `libraries`. **Every rule is explicitly configured in `detekt.yml`** — no implicit defaults (per plannotator review).
   - **Day-one gate:** CI **fails on any new violation** outside the baseline (per plannotator review). Existing code is absorbed by the baseline so the first run is green.
2. Kotlin 2.3.10, AGP 9.1.0, JDK toolchain 17 — these are compatible with detekt 2.0.0-alpha.3 (compiler-plugin variant). _Verification step in §10._
3. Linux CI runners are sufficient for non-iOS analysis; iOS source-set detekt tasks run only on macOS runners (matches existing macOS-only Kotlin/Native build path guarded by `isMac`).
4. Compose Multiplatform usage in `composeApp` and `commonComposeMain` makes `compose-rules` worthwhile.
5. All modules use Gradle Kotlin DSL with the version-catalog convention; the integration **must** flow through `gradle/libs.versions.toml`.
6. Generated code under `build/generated` (sqldelight, buildconfig, KSP, kotlinx-serialization) must not be analyzed.

### Risk: 2.0.0-alpha.3 on a published SDK

Pre-release pins carry concrete downside:
- Plugin id and rule defaults can shift before GA — re-baselining on each bump is likely.
- Third-party plugins (`compose-rules`, `libraries`, ktlint-wrapper) must ship 2.0-compatible artifacts; today only `dev.detekt:detekt-rules-ktlint-wrapper:2.0.0-alpha.3` and `dev.detekt:detekt-rules-libraries:2.0.0-alpha.3` are confirmed. **`io.nlopez.compose.rules:detekt` 2.0 compatibility is not yet verified** — see §10 verification gate.

**No fallback to 1.23.x** (per plannotator review). If V-4 (compose-rules 2.0 compatibility) fails, the contingency is to **defer compose-rules to a follow-up** and ship v1 without it on detekt 2.0.0-alpha.3 — _not_ to downgrade the detekt line. Verification §10 must pass for V-1, V-2, V-3, V-5, V-6 before implementation begins.

---

## 3. User stories

### US-1 — Local pre-push check
> _As an SDK developer, I run `./gradlew detekt` before pushing and see findings grouped by module with HTML report links, so I can fix issues without waiting for CI._

**Acceptance:**
- `./gradlew detekt` (root) executes detekt across every module's analyzable source sets.
- HTML report opens in a browser and shows a per-module breakdown.
- Wall-clock on a warm build < 60 s on an Apple Silicon dev box.

### US-2 — CI gate (fail-fast on new violations, day one)
> _As a reviewer, I see detekt findings as inline annotations on PRs via GitHub code-scanning, **and the build fails if a PR introduces any violation outside the baseline**._

**Acceptance:**
- A new CI job runs detekt on every push to `main` and every PR.
- SARIF is uploaded via `github/codeql-action/upload-sarif@v4` (pattern already used in `codeql.yml`).
- Findings appear in the PR "Files changed" tab as code-scanning annotations.
- **Job fails on any new violation** (i.e., any finding NOT recorded in `config/detekt/baseline.xml`). No `continue-on-error`.
- The first CI run after merge is green because the committed baseline absorbs all existing findings.

### US-3 — Baseline-driven adoption
> _As a maintainer, existing violations are recorded in a baseline so day-one noise is zero, and I can shrink the baseline file as code is cleaned up._

**Acceptance:**
- A single `config/detekt/baseline.xml` exists at repo root and is checked in.
- Re-running `./gradlew detektGenerateBaseline` (or the 2.0 equivalent) updates the file.
- New violations introduced after baseline are reported (and, post-warn-only period, fail the build).

### US-4 — KMP-aware analysis
> _As a developer touching `commonMain`, I get findings with type-resolution context so detekt catches API-surface, nullability, and library-misuse rules that require the type system._

**Acceptance:**
- Per-source-set tasks (`detektMetadataCommonMain`, `detektAndroidMain`, `detektJsMain`, `detektMetadataIosMain` on macOS, etc.) are present and wired into the aggregate `detekt` task.
- Type-resolution rules (e.g. `LibraryEntitiesShouldNotBePublic`, `NullCheckOnMutableProperty`) actually fire when violated.

---

## 4. Acceptance criteria (rolled up)

| # | Criterion | How to verify |
|---|-----------|---------------|
| AC-1 | `./gradlew detekt` succeeds on a clean checkout | Manual run + CI |
| AC-2 | Reports generated: HTML, XML, SARIF, MD | Inspect `<module>/build/reports/detekt/` |
| AC-3 | Generated code excluded (sqldelight, buildconfig, KSP, build/) | Grep SARIF for paths containing `build/generated` → must be empty |
| AC-4 | Baseline committed; `./gradlew detekt` exits 0 | CI green on first run |
| AC-5 | Configuration cache compatible | `./gradlew detekt --configuration-cache` succeeds twice (second run uses cache) |
| AC-6 | Build cache compatible | Re-run with no source changes hits FROM-CACHE |
| AC-7 | iOS source-set tasks skipped (not failed) on Linux CI | `./gradlew tasks` on Linux shows no `detektMetadataIosMain`, OR task no-ops |
| AC-7b | iOS detekt tasks verified locally on macOS by the reviewer | Reviewer runs `./gradlew detektMetadataIosMain` (and arm64/sim variants) on a Mac and confirms green |
| AC-8 | Compose-rules + ktlint-wrapper + libraries rules active | Findings file references rules from each set |
| AC-9 | SARIF visible in GitHub code-scanning on PRs | Open a test PR with a deliberate violation |
| AC-10 | Plugin + version sourced from `gradle/libs.versions.toml` | `grep -r detekt build.gradle.kts` shows `alias(libs.plugins.detekt)` only |
| AC-11 | All rules explicitly configured (no implicit defaults) | `detekt.yml` sets `active:` on every rule from every enabled rule set; `buildUponDefaultConfig = false` |
| AC-12 | README updated with detekt usage section | `README.md` includes how to run detekt locally, where reports land, baseline policy |

---

## 5. Commands

| Command | Purpose |
|---------|---------|
| `./gradlew detekt` | Run detekt across all per-source-set tasks (commonMain, androidMain, iosMain, jsMain, webMain, …) on every module. **No type resolution** in this aggregate (see D-3) — local-friendly, no secrets needed. |
| `./gradlew detektCommonMainSourceSet` (and per-set variants: `detektAndroidMainSourceSet`, `detektIosMainSourceSet`, `detektJsMainSourceSet`, `detektWebMainSourceSet`) | Run detekt on a single Kotlin source set. Used to drill into a specific platform's findings. |
| `./gradlew detektMain detektMainAndroid detektHostTestAndroid detektDeviceTestAndroid` | Run detekt with **type resolution** on Android compilations. Requires `google-services.json` materialized first (CI step or `make build-android` locally). Enables SPEC US-4 marquee rules like `LibraryEntitiesShouldNotBePublic`. |
| `./gradlew detektProjectBaseline` | Regenerate `config/detekt/baseline.xml` (custom project-wide aggregator task — works around detekt 2.0's per-task baselines). |
| `./gradlew detekt --configuration-cache` | Verify configuration-cache compatibility. |
| `./gradlew :engagement-cloud-sdk:detekt` | Run detekt for a single module (its per-source-set tasks). |

> Task names confirmed via `./gradlew tasks --all | grep detekt` on detekt 2.0.0-alpha.3 (see D-1 in §10).

---

## 6. Project structure

New / modified files:

```
engagement-cloud-sdk/
├── config/
│   └── detekt/
│       ├── detekt.yml          # Single shared config (NEW)
│       └── baseline.xml        # Shared baseline (NEW, generated)
├── gradle/
│   └── libs.versions.toml      # +detekt version, +detekt plugin alias, +detektPlugins libs
├── build.gradle.kts            # +detekt plugin apply false, +subprojects { } block OR convention plugin call
├── buildSrc/                   # OPTIONAL — convention plugin (see §7)
│   └── src/main/kotlin/
│       └── detekt-conventions.gradle.kts
├── .github/
│   └── workflows/
│       └── on_push_workflow.yml  # +detekt job with SARIF upload
└── SPEC.md                     # this file
```

### Configuration approach: subprojects {} block vs. convention plugin

**Decision: root `subprojects { }` block in `build.gradle.kts`.**

Tradeoff:
- **Root subprojects {} block** — simpler, no new build module, single source of truth. Downside: tightly couples root build to every subproject, harder to opt-out per module.
- **buildSrc convention plugin** — cleaner separation, idiomatic for larger codebases, easier to share across projects. Downside: adds a new module, slows configuration phase, overkill for 7 modules.

This project has only 7 modules and no existing `buildSrc/` (verified during survey). The subprojects block is the lower-friction choice; we can promote to a convention plugin in a future iteration if module count grows or other shared logic accumulates.

---

## 7. Code style / configuration

### `config/detekt/detekt.yml`

**Policy: every rule from every enabled rule set is set explicitly. No implicit defaults.** (per plannotator review)

Authoring procedure:

1. Generate the canonical config: `./gradlew detektGenerateConfig` — this emits a `detekt.yml` with **every** rule listed and `active:` set explicitly.
2. Append the third-party rule sections by hand: `formatting:` (from `detekt-rules-ktlint-wrapper`'s default config), `Compose:` (from `io.nlopez.compose.rules:detekt`'s sample config), `libraries:` (from `detekt-rules-libraries`).
3. Set **`buildUponDefaultConfig = false`** in the `detekt {}` block so detekt never silently merges in default values for rules we omitted.
4. CI runs `./gradlew detekt --build-upon-default-config=false` to enforce this in the gate.

Project-specific tunings on top of the generated baseline config:
- `comments.UndocumentedPublicClass.active: true` — relevant for an SDK; baseline absorbs current violations.
- `complexity.LongParameterList.functionThreshold: 8` — Compose `@Composable` functions routinely exceed default 6.
- `style.MagicNumber.ignoreAnnotation: true`, plus standard ignores for tests.
- `formatting.*` — fully enabled (provided by `detekt-rules-ktlint-wrapper`).
- `Compose.*` — fully enabled (provided by `io.nlopez.compose.rules:detekt`); subject to V-4 outcome.
- `libraries.*` — fully enabled and tuned; `LibraryEntitiesShouldNotBePublic` is the marquee rule for an SDK.
- `output-reports.active`: html, xml, sarif, md.
- `processors.active: true` (file/loc/class metrics).
- `console-reports.active: true`.

Any rule we want to disable is set to `active: false` **explicitly with a comment explaining why**. Any new rule that lands in a future detekt bump is treated as a CI failure (because it won't be in our pinned config) — this is intentional; we triage on bump.

### Excludes (in detekt {} block)

```kotlin
detekt {
    source.from(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/jsMain/kotlin",
            "src/iosMain/kotlin",
            // tests included intentionally — code quality matters there too
        )
    )
}
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/build/**")
    exclude("**/generated/**")              // sqldelight, KSP, kotlinx-serialization
    exclude("**/build/generated/source/buildConfig/**")
    exclude("**/*.gradle.kts")              // run on .kt only by default
}
```

### Rule sets to wire in

**All available first-party rule sets are enabled** (per reviewer direction "use all rulesets and set each rule explicitly"):

- `detekt-rules-style` (default)
- `detekt-rules-complexity` (default)
- `detekt-rules-performance` (default)
- `detekt-rules-potential-bugs` (default)
- `detekt-rules-exceptions` (default)
- `detekt-rules-naming` (default)
- `detekt-rules-empty-blocks` (default)
- `detekt-rules-coroutines` (default — Kotlin coroutines used heavily here)
- `detekt-rules-libraries` (opt-in, **enabled** — SDK-relevant)
- `detekt-rules-ktlint-wrapper` (opt-in, **enabled** — formatting)
- `detekt-rules-ruleauthors` (opt-in, **enabled** — even though we don't author custom rules today, reviewer asked for "all rulesets"; rules that don't apply are explicitly `active: false` with a comment)

Plus third-party:
- `io.nlopez.compose.rules:detekt` (Compose rules, **enabled** — pending 2.0 artifact verification in §10; if V-4 fails, deferred to a follow-up rather than blocking v1)

---

## 8. Testing strategy

Detekt itself isn't unit-tested in this repo. Verification of the **integration** is functional:

1. **Smoke test**: `./gradlew detekt` exits 0 after baseline is generated.
2. **Negative test**: introduce a deliberate violation (e.g. `MagicNumber`) on a temporary branch; confirm:
   - Local: appears in HTML report.
   - CI: appears as PR annotation via SARIF upload.
   - With warn-only: build still succeeds.
3. **Cache test**: run `./gradlew detekt --configuration-cache` twice; second run reuses configuration cache.
4. **Linux/macOS parity test**: trigger CI on both runners; verify iOS source-set tasks skip cleanly on Linux.
5. **Generated-code test**: grep the SARIF for any path under `build/generated` — must be empty.
6. **Module coverage test**: confirm each of the 7 modules contributes findings (or an explicit "no findings" entry) to the aggregated report.

---

## 9. Boundaries

### Always do
- Source detekt version + plugin id from `gradle/libs.versions.toml`. Never hard-code in module build files.
- Apply detekt via the root `subprojects { }` block; never duplicate config in module build files.
- Keep `config/detekt/detekt.yml` and `config/detekt/baseline.xml` in version control.
- Set every rule's `active:` explicitly in `detekt.yml`. Use `buildUponDefaultConfig = false`.
- Exclude `build/`, `**/generated/**`, and `*.gradle.kts` from analysis.
- Run with `--configuration-cache` in CI to catch regressions early.
- Verify iOS detekt tasks (`detektMetadataIosMain`, etc.) locally on macOS during AC verification — reviewer has a Mac.

### Ask first
- Tightening rules beyond defaults (e.g. lowering `LongMethod` threshold).
- Adding/removing rule sets after the initial set.
- Bumping detekt version (especially across alpha → GA).
- Disabling any rule (must include a comment explaining why).

### Never do
- Suppress findings via `@Suppress("detekt:RuleName")` in production code without a code comment explaining why. Use the baseline for systemic existing issues.
- Edit `baseline.xml` by hand. Always regenerate.
- Configure detekt with `continue-on-error: true` or `ignoreFailures = true` in CI. The gate is fail-fast from day one.
- Run detekt against generated code (sqldelight, KSP outputs, buildconfig).
- Fall back to detekt 1.23.x if 2.0.0-alpha.3 verification fails. Drop the affected rule set instead and file a follow-up.

---

## 10. Pre-implementation verification gate

**Block implementation until these are confirmed.** Each is a 1-command check:

| # | Check | Command / source | Pass criterion |
|---|-------|------------------|----------------|
| V-1 | detekt 2.0.0-alpha.3 plugin published | `https://plugins.gradle.org/plugin/dev.detekt` | Page lists 2.0.0-alpha.3 |
| V-2 | `dev.detekt:detekt-rules-libraries:2.0.0-alpha.3` on Maven Central | search.maven.org | Artifact resolves |
| V-3 | `dev.detekt:detekt-rules-ktlint-wrapper:2.0.0-alpha.3` on Maven Central | search.maven.org | Artifact resolves |
| V-4 | `io.nlopez.compose.rules:detekt` has a release compatible with detekt 2.0.0-alpha.3 | github.com/mrmans0n/compose-rules releases | Release notes mention detekt 2.0 OR API compat |
| V-5 | KMP type-resolution task names in 2.0 | detekt 2.0 docs | Confirm `detektMetadataCommonMain` etc. still exist |
| V-6 | Compatibility with Kotlin 2.3.10 | detekt release notes | Explicit support |

**If V-4 fails** → **drop compose-rules from v1 scope** and proceed on detekt 2.0.0-alpha.3 without it. File a follow-up to add compose-rules once the upstream ships a 2.0-compatible artifact. **Do not** fall back to 1.23.x (per plannotator review).

### Verification log (T1, 2026-06-12 on `feat/detekt-integration`)

| # | Result | Source / evidence |
|---|--------|-------------------|
| V-1 | ✅ | https://plugins.gradle.org/plugin/dev.detekt — "Version 2.0.0-alpha.3 (latest)". No 2.0 stable; only alpha.0..alpha.3. |
| V-2 | ✅ | `HEAD https://repo.maven.apache.org/maven2/dev/detekt/detekt-rules-libraries/2.0.0-alpha.3/...pom` → 200. (search.maven.org solr returned 0 — index lag, not absence.) |
| V-3 | ✅ | Same direct-POM HEAD check returns 200 for `detekt-rules-ktlint-wrapper:2.0.0-alpha.3`. |
| V-4 | ✅ | `io.nlopez.compose.rules:detekt:0.6.0` (Jun 2025) explicitly supports detekt 2.0.0-alpha.3. POM HEAD on Maven Central returns 200. **compose-rules stays in v1 scope; pin to `0.6.0` in T2.** |
| V-5 | ✅ | `https://detekt.dev/docs/gettingstarted/type-resolution` — confirms `detektMetadataCommonMain`, `detektJvmMain`, `detektMetadataIosMain`, `detektAndroid<Variant>`. |
| V-6 | ⚠️ pass-with-caveat | detekt 2.0.0-alpha.3 was built against **Kotlin 2.3.21**, not the project's 2.3.10. Detekt's classpath must run on its own bundled Kotlin; the project's Kotlin can differ. **Constraint:** never introduce `configurations.all { resolutionStrategy.eachDependency { useVersion(...) } }` for `org.jetbrains.kotlin` without excluding the `detekt` configuration. Repo grep confirmed zero existing alignment. T3 must encode a comment to that effect. |

**Outcome:** all six checks pass. Implementation may proceed (T2..T11).

### Post-T1 discoveries (2026-06-15 on `feat/detekt-integration`)

The verification log above was a documentation/availability gate; running `./gradlew detekt` in practice surfaced four issues the gate didn't catch. None invalidate the v1 plan, but each requires the build wiring to deviate from what §5/§10 implied.

| # | Discovery | Resolution |
|---|-----------|------------|
| D-1 | Detekt 2.0.0-alpha.3 task names differ from §10 V-5's source. **Actual names:** `detektCommonMainSourceSet`, `detektAndroidMainSourceSet`, `detektIosMainSourceSet`, `detektJsMainSourceSet`, `detektWebMainSourceSet`, plus type-resolution variants `detektMain` / `detektDebug` / `detektRelease` / `detektTest` (pure-Android modules), `detektMainAndroid` / `detektHostTestAndroid` / `detektDeviceTestAndroid` (KMP+Android modules). NOT `detektMetadataCommonMain`/`detektMetadataIosMain`/`detektAndroid<Variant>`. | §5 + §10 V-5 amended; commands updated. |
| D-2 | The aggregate `:detekt` task in detekt 2.0 is the JVM-only task per subproject. On KMP modules it has NO-SOURCE — `./gradlew detekt` silently skipped commonMain/iosMain/jsMain. SPEC US-4 was technically passing while analyzing 0% of `engagement-cloud-sdk`'s actual code. | `build.gradle.kts` `subprojects {}` block now wires `tasks.named("detekt").dependsOn(tasks.withType<Detekt>().matching { it.name.endsWith("SourceSet") })`. Per-source-set tasks fold into the aggregate. |
| D-3 | Detekt 2.0's Android type-resolution tasks (`detektMain`/`detektMainAndroid`/`detektHostTestAndroid`/`detektDeviceTestAndroid`) transitively depend on the full Android compilation graph, including `processDebugGoogleServices`, which requires `google-services.json`. Locally unavailable; CI has it via `GOOGLE_SERVICES_JSON_BASE64` secret. | Aggregate `detekt` task includes only `*SourceSet` tasks, so local devs run without secrets. **CI workflow** materializes `google-services.json` (mirroring nightly_workflow.yml) and invokes the type-resolution tasks explicitly: `./gradlew detekt detektMain detektMainAndroid detektHostTestAndroid` — preserving SPEC US-4's marquee rules (`LibraryEntitiesShouldNotBePublic`) in CI. |
| D-4 | The `tasks.withType<Detekt>().configureEach { setSource(source.filter { ... }) }` filter does NOT prevent generated-code analysis on per-source-set tasks. KGP appears to populate `source` after the configureEach hook runs, re-introducing `build/generated/sqldelight/...` paths. AC-3 currently fails. | **In progress** — needs different lifecycle hook (likely per-task `doFirst { setSource(...) }`, or per-task `exclude` on absolute paths, or KGP-aware source-set filtering). Tracked as T1.5. |
| D-5 | `Configuration was resolved during configuration time` warnings (Gradle 10 incompatibility) come from detekt 2.0.0-alpha.3 itself — not just our `detektProjectBaseline` task. Switching `getByName(...)` → `named(...)` in our task fixed our share; the rest is upstream. | Documented as a known-warning; won't fix in v1. Files a tracking note in §12 follow-ups. |

**Implications for §10/§11:**
- §10 V-6 caveat about Kotlin 2.3.21 vs 2.3.10 still applies.
- §11 Phase 2 (CI fail-fast) gains a new step: materialize `google-services.json` from `GOOGLE_SERVICES_JSON_BASE64` secret before `./gradlew detekt detekt*Android*` runs.
- Baseline regenerated post-D-4 fix will be substantially larger than the current 1.18 MB — expected, since we're now actually analyzing the KMP code that was previously skipped.

---

## 11. Rollout plan

1. **Phase 0 — Verify** (§10).
2. **Phase 1 — Wire** (~½ day):
   - Add to version catalog.
   - Add subprojects {} block to root `build.gradle.kts`.
   - Add `config/detekt/detekt.yml` with **every rule explicitly set** (`buildUponDefaultConfig = false`).
   - Generate baseline locally; commit.
3. **Phase 2 — CI fail-fast** (~½ day):
   - New job in `on_push_workflow.yml`.
   - SARIF upload via `github/codeql-action/upload-sarif@v4`.
   - **No `continue-on-error`.** Build fails on any new violation outside the baseline.
4. **Phase 3 — Cache + parity** (~½ day):
   - Verify configuration cache + build cache.
   - Verify Linux runner skips iOS tasks cleanly.
   - **Reviewer runs iOS detekt tasks locally on macOS** to confirm green (AC-7b).
5. **Phase 4 — Document** (~¼ day):
   - **README section** on running detekt locally, where reports land, and the baseline policy (AC-12).
   - CONTRIBUTING note on how to handle new findings (fix vs. justify; never edit baseline by hand).
6. **Phase 5 — Steady state**:
   - Track baseline shrink as code is touched. No future "flip the gate" step needed — gate is fail-fast from day one.

---

## 12. Open follow-ups (post-merge, not blocking v1)

- Add `io.nlopez.compose.rules:detekt` if V-4 deferred it.
- Consider promoting to a buildSrc convention plugin if module count grows.
- Revisit detekt version once 2.0 GA ships; re-baseline on the bump and audit any newly-shipped rules (currently treated as CI failures by the explicit-config policy).
- Evaluate `detekt --auto-correct` as a pre-commit hook (formatting only).
- **Track upstream resolution of detekt 2.0.0-alpha.3 "Configuration resolved during configuration time" warnings (D-5).** They originate from detekt's own task wiring, not ours. Re-check on each detekt bump; consider filing an upstream issue if not resolved by GA.
- **Re-evaluate D-4 fix** (generated-code exclusion in per-source-set tasks) once detekt 2.0 GA ships — upstream may add KGP-aware source-set filtering that subsumes our manual filter.

---

## Sign-off

Reviewer (plannotator, 2026-06-12):
- [x] §1–4 captures the goal and acceptance criteria correctly. → **yes**
- [x] §7 rule-set choices match expectations. → **"yes, use all rulesets and set each rule explicitly"** — incorporated.
- [x] §10 verification gate. → **"do not fall back"** — 1.23.x exit ramp removed; V-4 contingency is to defer compose-rules instead.
- [x] §11 phasing. → **yes** — phase 2 changed from warn-only to fail-fast per §1 directive.

Plus annotations addressed:
- US-2: pipeline now fails on new violations outside the baseline.
- AC-7b added: reviewer to verify iOS tasks locally on macOS.
- AC-12 added: README updated with detekt section.
- §7: every rule explicitly set; `buildUponDefaultConfig = false`; ruleauthors enabled (with non-applicable rules `active: false` + comment).

Next step: `agent-skills:planning-and-task-breakdown` against this spec.
