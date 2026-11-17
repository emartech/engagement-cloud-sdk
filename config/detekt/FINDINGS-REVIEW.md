# Pre-baseline findings review

The first complete GitHub inventory is run 44101159532, commit a4eb81b6:
https://github.com/emartech/engagement-cloud-sdk/actions/runs/44101159532

It contains 3,102 report occurrences and 2,958 module/task-specific baseline identities: 1,560 production, 1,533 test, and 9 build-script occurrences. The original reports and hashes remain preserved in the verification worktree before baseline generation.

This is a representative source review plus complete machine-identity reconciliation, not a claim that every finding is a confirmed defect or that every false positive has been identified. Initial adoption does not fix source or reformat the SDK.

## Representative findings and disposition

- **Published-library API (446 LibraryEntitiesShouldNotBePublic, 100 ForbiddenPublicDataClass occurrences):** `SAPEngagementCloudFirebaseMessagingService` and `FirebaseMessagingServiceRegistrationOptions` expose existing registration/service APIs. Their visibility/data-class shape is intentional existing API, not a reason to disable the rules. Baseline existing findings only; future API expansion remains subject to the approved policy.
- **Explicit return types (921 occurrences):** includes production APIs and existing test functions. The approved rule remains active in published modules. Test-API applicability is a follow-up policy consideration, not a new exclusion in this adoption.
- **FunctionNaming (80 occurrences):** `AppAndroidPreview` is annotated `@Preview`/`@Composable`, and generated Compose resource collector functions are also represented. These are concrete naming-policy/false-positive candidates. A future narrow Composable annotation exception needs a separate decision; none is silently added here.
- **UnsafeCallOnNullableType (14 occurrences):** `AndroidECConfigSerializer.deserialize` force-unwraps the `applicationCode` JSON value. This is a genuine robustness-review candidate, not automatically dismissed as a compiler-context false positive. Generated SQLDelight code also has findings and remains included. Do not fix or broadly exclude these files in the integration.
- **Material2 (13 occurrences):** existing sample UI imports Material2 components. Icons are already exempt in Compose rules 0.6.8. The Material2 rule remains enabled; the sample's existing component usage is inventoried, not hidden through a broader allowlist.

## Accepted engine limitations

Detekt reports 24 Compose, 91 SDK main, and 8 SDK host-test compiler diagnostics in the observed contexts. Upstream #9602 and #7531 explain KMP fragment relationships and generated serializer resolution. The operator accepted these limitations while requiring all sources and approved rules to remain present. Type-aware findings around unresolved symbols can be inaccurate; normal compilation still succeeds.

## Experimental evaluation and baselines

The next CI run creates a complete disposable experimental policy enabling the nine requested rules. It also inventories ordinary TODO comments diagnostically; FIXME/STOPSHIP remain enforced and ordinary TODO policy is not silently changed. The enforced YAML is never overwritten. Experimental counts and false-positive candidates are reported separately.

Baseline generation uses the enforced policy only and is rejected if an experimental config is requested. Each ID must match a preserved per-task SARIF identity (rule, file name, SHA-1 of entity signature). Duplicate report occurrences may collapse to one baseline identity; that is distinguished from merged-report deduplication.

The official XML merger deduplicates identical file/line/column/rule entries. The verifier checks that every distinct per-task finding survives XML and SARIF merging rather than incorrectly requiring duplicate occurrence totals to stay equal.

Baseline candidates are produced in CI only after the initial inventory is preserved. They are not automatically committed. Required verification: zero missing/orphan IDs, byte-identical regeneration, an unchanged-source pass, new syntax/type-aware failures, restoration after removing probes, and unchanged baseline hashes throughout.
