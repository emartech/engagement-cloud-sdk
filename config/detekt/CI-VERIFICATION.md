# Temporary Detekt CI verification

This branch is an isolated integration spike, not a completed quality gate or release.

The `Detekt Integration Verification` workflow runs on `ci/detekt-verification` using Ubuntu and Temurin JDK 17. It validates the policy against the pinned runtime providers, checks configuration-cache reuse, inventories the task graph, and runs the complete canonical analysis without baselines. HTML/XML/SARIF findings and logs are retained for 30 days, including on failure.

The ordinary push build/test/reporting workflow excludes only this temporary branch to avoid launching unrelated matrix jobs or notifications. That branch-only exclusion is not intended for the final integration. No publishing workflow is dispatched and no tags or pull requests are created.

## Versions and accepted limitations

- Detekt and libraries rules: `2.0.0-alpha.6`
- Compose rules: `0.6.8`
- Existing Kotlin / AGP / Gradle: `2.3.10` / `9.1.0` / `9.4.1`

The operator explicitly accepted the known incomplete semantic resolution around KMP expect/actual declarations and serialization-generated symbols. Relevant upstream issues: https://github.com/detekt/detekt/issues/9602 and https://github.com/detekt/detekt/issues/7531. No rule is disabled and no source is excluded to hide these diagnostics. `-Pdetekt.inventory=true` is adoption-only: existing findings do not fail the inventory, but configuration, compilation, and tool failures still fail CI.

Google Services' supported `IGNORE` missing-file strategy is used only for explicit Detekt invocations so static analysis does not require sample-app credentials. Existing supplied JSON is processed normally; ordinary build/lint invocations retain the plugin's default strategy.

The unfinished staged-hook implementation and provisional baselines are deliberately not copied into this verification branch. Baselines may be generated only after the successful complete inventory is preserved and reconciled. Final staged-index fidelity, CI/release enforcement, merged reports, and regression checks remain separate acceptance work.

Only the selected Detekt build/configuration, validator/report summarizer, and temporary workflow changes are committed here. Unrelated work and original planning files remain in the original worktree.
