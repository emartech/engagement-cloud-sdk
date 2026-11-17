# Detekt CI and publishing enforcement

## Shared authoritative gate

`.github/workflows/detekt.yml` is the reusable Ubuntu/JDK 17 quality job. It runs `make detekt-ci` (the complete baselined canonical matrix), validates the pinned rule catalog, retains per-task and merged HTML/XML/SARIF reports with `if: always()`, and never generates baselines or performs staged-only analysis.

One independent caller job named `Detekt` is added to normal PR/push CI, nightly CI, artifact publishing, and Maven Central promotion. It has no build/test dependencies, so analysis begins early rather than waiting for the matrix. The existing Android Lint jobs are byte-for-byte unchanged.

GitHub's reusable workflow semantics do not propagate caller-level `env` variables. No `secrets: inherit` or release secret input is used. The gate checks that release credentials and the verification caller's sentinel are absent. Official reference: https://docs.github.com/en/actions/reference/workflows-and-actions/reusing-workflow-configurations

## Protected publishing paths

- Snapshot Android/Maven, JS/NPM/CDN, and iOS publishing require `detekt` success.
- Artifact release pipelines, fallback tag creation, GitHub release creation, and Pages/CDN deployment require `detekt` success explicitly, including jobs that use `!cancelled()` and could otherwise bypass failed dependencies.
- Nightly E2E dispatch/reporting and normal reporting include the quality result.
- Manual Maven Central promotion analyzes `refs/tags/<requested version>` and requires success before the promotion job runs. This is fail-closed for historical tags without the integration; do not substitute an unrelated branch's passing result.
- Documentation-only deployment builds the separate docs repository and is not an SDK artifact publish path. Emergency release revocation/rollback is deliberately not blocked by static analysis.

No publishing, promotion, tag creation, E2E dispatch, or Slack notification workflow was executed during verification. The temporary branch still excludes the ordinary push matrix; only the no-publish verification workflow runs there. Remove the temporary branch-only exclusion when retiring that branch, not by bypassing the production gate.

## Reports and SARIF permissions

Artifact paths: `build/reports/detekt/` and `build/detekt-ci/`; seven-day retention matches the repository maximum. Reports are uploaded even when analysis fails.

Code Scanning dashboard upload is opt-in via repository variable `DETEKT_UPLOAD_SARIF=true`. Enable it only when licensing/settings and `security-events: write` permission support upload. Regardless of the variable, fork PRs, Dependabot, and missing SARIF files are skipped. SARIF remains in ordinary artifacts when dashboard upload is disabled or unsupported. Only `merged.sarif` is sent, avoiding duplicate parallel uploads.

## Required PR status — repository-owner action

The stable GitHub job/check name observed in verification is **`Detekt / Quality gate`**. A repository owner must add it to the protected-branch/ruleset required checks after the workflow is available on the target branch. Configure this for each protected merge branch (for example main/dev); do not use changed-files-only gating or a bypassable local hook as the merge gate.

The current account has push but not admin permissions. Reading main branch protection returned HTTP 404 and the ruleset listing was empty; this does not prove that all organization-level protections are absent. Required-check installation could not be verified or changed here. Workflow publishing dependencies are enforced in code, but mandatory merge protection remains an external acceptance item until an owner confirms it.

## Verification

The exact reusable job passed in CI run https://github.com/emartech/engagement-cloud-sdk/actions/runs/44115700328: `Detekt / Quality gate` succeeded in 4m8s, the caller environment sentinel did not propagate, reports uploaded, and a read-only dependent consumer ran only after success. Controlled syntax/type failure-restoration checks also passed without baseline changes.

`test_ci_wiring.py` checks every protected publishing job's dependency and top-level AND success guard, independent caller gates, release-tag source selection, reporter dependencies, SARIF opt-in/fork safety, and unchanged Android Lint job hashes. The tests failed before wiring. Mutations replacing an AND with OR, removing a publishing dependency, or bypassing SARIF opt-in each failed the suite. `actionlint` validates all edited workflow syntax/expressions.

Live fork token/dashboard upload behavior and actual release publication are intentionally not exercised with production credentials. Their safety guards and DAGs are validated without publishing. The accepted Detekt KMP/serialization semantic limitations remain documented in `ADOPTION-REPORT.md`.
