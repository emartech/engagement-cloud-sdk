# Temporary Detekt CI verification

This branch is an isolated integration spike, not a completed quality gate or release.

The `Detekt Integration Verification` workflow runs on `ci/detekt-verification` using Ubuntu and Temurin JDK 17. Adoption run 44106374035 preserved the complete unbaselined inventory, evaluated experimental rules separately, generated and reconciled candidates, and proved deterministic regeneration and new-finding enforcement. The verified 28 baselines are now committed. Normal runs never regenerate them: they verify the preserved report bundle and baseline hashes, run the complete baselined gate, and repeat syntax/type failure-restoration checks. Reports and logs are retained for 7 days; the original inventory is also archived in Git.

The ordinary push build/test/reporting workflow excludes only this temporary branch to avoid launching unrelated matrix jobs or notifications. That branch-only exclusion is not intended for the final integration. No publishing workflow is dispatched and no tags or pull requests are created.

## Versions and accepted limitations

- Detekt and libraries rules: `2.0.0-alpha.6`
- Compose rules: `0.6.8`
- Existing Kotlin / AGP / Gradle: `2.3.10` / `9.1.0` / `9.4.1`

The operator explicitly accepted the known incomplete semantic resolution around KMP expect/actual declarations and serialization-generated symbols. Relevant upstream issues: https://github.com/detekt/detekt/issues/9602 and https://github.com/detekt/detekt/issues/7531. No rule is disabled and no source is excluded to hide these diagnostics. `-Pdetekt.inventory=true` is adoption-only: existing findings do not fail the inventory, but configuration, compilation, and tool failures still fail CI.

Google Services' supported `IGNORE` missing-file strategy is used only for explicit Detekt invocations so static analysis does not require sample-app credentials. Existing supplied JSON is processed normally; ordinary build/lint invocations retain the plugin's default strategy.

The unfinished staged-hook implementation and old provisional baselines are deliberately not copied into this verification branch. Baseline candidates were generated only after the complete initial inventory was preserved, used the enforced policy only, and were inspected and deliberately adopted after CI verification—not automatically committed by CI. `make detekt-ci` is a thin CI-only wrapper without `check-env`; `DETEKT_ARGS` carries the verification runner's resource options. Final staged-index fidelity, release enforcement, and existing Android Lint regression checks remain separate acceptance work. See `FINDINGS-REVIEW.md` for the representative pre-baseline review and `ADOPTION-REPORT.md` for verified outcomes and remaining work.

Only the selected Detekt build/configuration, validator/report summarizer, and temporary workflow changes are committed here. Unrelated work and original planning files remain in the original worktree.
