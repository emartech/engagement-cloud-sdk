# Temporary Detekt CI verification

This branch is an isolated integration spike, not a completed quality gate or release.

The `Detekt Integration Verification` workflow runs on `ci/detekt-verification` using Ubuntu and Temurin JDK 17. It validates the policy and configuration cache, preserves the complete unbaselined inventory including merged XML/SARIF, evaluates experimental rules separately, then generates and reconciles baseline candidates. It verifies deterministic regeneration and before/during/after enforcement for new syntax and type-aware findings. Reports, candidates, and logs are retained for 7 days (the repository limit), including on failure.

The ordinary push build/test/reporting workflow excludes only this temporary branch to avoid launching unrelated matrix jobs or notifications. That branch-only exclusion is not intended for the final integration. No publishing workflow is dispatched and no tags or pull requests are created.

## Versions and accepted limitations

- Detekt and libraries rules: `2.0.0-alpha.6`
- Compose rules: `0.6.8`
- Existing Kotlin / AGP / Gradle: `2.3.10` / `9.1.0` / `9.4.1`

The operator explicitly accepted the known incomplete semantic resolution around KMP expect/actual declarations and serialization-generated symbols. Relevant upstream issues: https://github.com/detekt/detekt/issues/9602 and https://github.com/detekt/detekt/issues/7531. No rule is disabled and no source is excluded to hide these diagnostics. `-Pdetekt.inventory=true` is adoption-only: existing findings do not fail the inventory, but configuration, compilation, and tool failures still fail CI.

Google Services' supported `IGNORE` missing-file strategy is used only for explicit Detekt invocations so static analysis does not require sample-app credentials. Existing supplied JSON is processed normally; ordinary build/lint invocations retain the plugin's default strategy.

The unfinished staged-hook implementation and old provisional baselines are deliberately not copied into this verification branch. Baseline candidates are generated only after the complete initial inventory is preserved, use the enforced policy only, and are never automatically committed. `make detekt-ci` is a thin CI-only wrapper without `check-env`; `DETEKT_ARGS` carries the verification runner's resource options. Final staged-index fidelity, release enforcement, and existing Android Lint regression checks remain separate acceptance work. See `FINDINGS-REVIEW.md` for the representative pre-baseline review.

Only the selected Detekt build/configuration, validator/report summarizer, and temporary workflow changes are committed here. Unrelated work and original planning files remain in the original worktree.
