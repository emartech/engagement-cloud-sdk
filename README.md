[![REUSE status](https://api.reuse.software/badge/github.com/emartech/engagement-cloud-sdk)](https://api.reuse.software/info/github.com/emartech/engagement-cloud-sdk)

# SAP Engagement Cloud SDK

> __`Important!`__
>
> The SAP Engagement Cloud SDK is currently available in a pilot release for a select group of clients. If you wish to participate, please reach out to your client success manager.
>
> Implementation should only begin after receiving confirmation of your pilot status from SAP.

## About this project

The **SAP Engagement Cloud SDK** is the next-generation SDK for **SAP Engagement Cloud**, providing a unified API for managing events, push notifications, in-app messaging, and more across Android, iOS, and Web. Built around integration consistency and ease of use, it enables straightforward onboarding and fast time-to-value across all platforms.

## Requirements and Setup

You can access our official documentation here: [SAP Engagement Cloud SDK - Wiki](https://emartech.github.io/engagement-cloud-sdk/docs/index.html)

## Additional Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Swift Package Manager Documentation](https://swift.org/package-manager/)
- [NPM Documentation](https://www.npmjs.com/)

## Static Analysis (detekt)

The project uses [detekt](https://detekt.dev/) 2.0.0-alpha.3 for Kotlin static analysis. Configuration lives in `config/detekt/detekt.yml` (every rule set explicitly) and existing findings are absorbed into `config/detekt/baseline.xml`. CI fails on any new violation outside the baseline.

**Run locally:**

```bash
./gradlew detekt                       # all per-source-set tasks across every module
./gradlew :engagement-cloud-sdk:detekt # single module
```

`./gradlew detekt` covers `commonMain`, `androidMain`, `jsMain`, `iosMain` (on macOS), and the Compose source set. v1 ships without the type-resolution-only `detekt-rules-libraries` rule set (`LibraryEntitiesShouldNotBePublic`, `LibraryCodeMustSpecifyReturnType`) — see `SPEC.md` §10 D-3 for the deferral rationale and §12 for the v2 follow-up.

iOS-specific detekt tasks (`detektIosMainSourceSet`, `detektIosArm64MainSourceSet`, …) only register on macOS hosts; on Linux they are stripped from the aggregate so `./gradlew detekt` runs cleanly. CI verifies the macOS-only iOS coverage via the reviewer pass (see `SPEC.md` AC-7b), not on the Linux CI runner.

**Reports:** every module writes `build/reports/detekt/<sourceSet>.{html,sarif,xml,md}`. Open the `.html` files for human-readable findings; CI uploads SARIF to GitHub code-scanning so PR annotations appear in the "Files changed" tab. The Linux CI run uploads the aggregated HTML reports as a `detekt-html-reports` artifact for offline triage.

**Baseline policy:** never edit `config/detekt/baseline.xml` by hand. To regenerate it after a sweeping fix or a detekt bump:

```bash
./gradlew detektProjectBaseline
```

This walks every Kotlin source file in the repo and rewrites the single baseline. See `CONTRIBUTING.md` for the full workflow when a new finding lands on your PR.

## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc. via [Zendesk](https://emarsys.zendesk.com). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](https://github.com/SAP/.github/blob/main/CONTRIBUTING.md).

## Security / Disclosure

If you find any bug that may be a security problem, please follow our instructions at in our [security policy](https://github.com/emartech/engagement-cloud-sdk/security/policy) on how to report it. Please do not create GitHub issues or Zendesk tickets for security-related doubts or problems.

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](https://github.com/emartech/engagement-cloud-sdk#coc-ov-file) at all times.

## Licensing

Copyright 2025-2026 SAP. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available via the [REUSE tool](https://api.reuse.software/info/github.com/emartech/engagement-cloud-sdk).
