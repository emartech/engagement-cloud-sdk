# Contributing

Thanks for considering a contribution. The bulk of contribution policy lives in [SAP/.github CONTRIBUTING](https://github.com/SAP/.github/blob/main/CONTRIBUTING.md). This document covers project-specific workflows.

## Static analysis findings (detekt)

CI runs [detekt](https://detekt.dev/) on every PR and **fails on any new violation outside the committed baseline**. See `README.md` for how to run detekt locally and `SPEC.md` §9 for the full policy.

When a new finding lands on your PR, your options are, in order of preference:

1. **Fix the code.** Most findings are real and worth addressing. The HTML report (`<module>/build/reports/detekt/<sourceSet>.html` locally, or the `detekt-html-reports` workflow artifact in CI) tells you the rule, location, and suggested remediation.

2. **Suppress at the call site with a rationale.** If the rule is wrong for this specific case but right in general, add `@Suppress("RuleName")` with a code comment explaining *why* — not just *what*. Example:

   ```kotlin
   @Suppress("MagicNumber") // RFC 7231: HTTP 200 OK
   private const val HTTP_OK = 200
   ```

3. **Disable the rule project-wide** (rare). Open a PR that edits `config/detekt/detekt.yml` to set `RuleName.active: false` with a comment explaining the rationale. Per SPEC §9, every disabled rule needs a comment. This requires reviewer sign-off — it changes policy for everyone.

### Never do

- **Never edit `config/detekt/baseline.xml` by hand.** It is generated. Hand edits are silently lost on the next regeneration.
- **Never add `continue-on-error: true` or `ignoreFailures = true`** to bypass the gate. The day-one fail-fast posture is a deliberate SPEC §1 / US-2 decision.
- **Never use `@Suppress("detekt:RuleName")` without a code comment** explaining why. Use the baseline only for systemic existing issues, not for a single case you don't want to fix.

### Regenerating the baseline

If you've applied a sweeping fix that resolves many baseline entries, or after a detekt version bump that adds new rules, regenerate the baseline:

```bash
./gradlew detektProjectBaseline
git add config/detekt/baseline.xml
```

Inspect the diff before committing — a baseline that *grows* on a sweeping-fix commit indicates new violations were introduced; investigate before merging.

> **Note (v1 only):** the type-resolution-only `detekt-rules-libraries` rule set is deferred to v2 (`LibraryEntitiesShouldNotBePublic`, `LibraryCodeMustSpecifyReturnType`, etc.). They are disabled in `detekt.yml` because the noJdk baseline can't absorb their findings under the current single-baseline architecture. See `SPEC.md` §10 D-3 + §12 follow-up.

## Reporting issues

Bug reports and feature requests go through Zendesk per the main project policy — see `README.md`.
