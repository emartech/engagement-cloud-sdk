[![REUSE status](https://api.reuse.software/badge/github.com/emartech/engagement-cloud-sdk)](https://api.reuse.software/info/github.com/emartech/engagement-cloud-sdk)

# SAP Engagement Cloud SDK

> __`Important!`__
>
> The SAP Engagement Cloud SDK is currently available as part of an Early Adopter Care program for a select group of clients. See [Joining the Early Adopter Care Program](https://emartech.github.io/engagement-cloud-sdk/docs/integration/onboarding) for next steps.
>
> Implementation should only begin after receiving confirmation of your Early Adopter status from SAP.

## About this project

The **SAP Engagement Cloud SDK** is the next-generation SDK for **SAP Engagement Cloud**, providing a unified API for managing events, push notifications, in-app messaging, and more across Android, iOS, and Web. Built around integration consistency and ease of use, it enables straightforward onboarding and fast time-to-value across all platforms.

## Requirements and Setup

You can access our official documentation here: [SAP Engagement Cloud SDK - Wiki](https://emartech.github.io/engagement-cloud-sdk/docs/index.html)

## Additional Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Swift Package Manager Documentation](https://swift.org/package-manager/)
- [NPM Documentation](https://www.npmjs.com/)

## Support, Feedback, Contributing

This project is open to feature requests/suggestions, bug reports etc. via [Zendesk](https://emarsys.zendesk.com). Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](https://github.com/SAP/.github/blob/main/CONTRIBUTING.md).

## Kotlin static analysis

Detekt uses pinned versions and a shared explicit policy. In a Unix-like developer environment with Git, Make, Python 3, and the project's Gradle/JDK toolchain:

```sh
git add -- path/to/intended-change.kt
make detekt-staged
```

This checks the **staged/index content**, not different unstaged edits. It never auto-corrects source or changes the index. It uses module-specific plugin scope and baseline identities. Only staged Kotlin files are analyzed; cross-file/dependent-file and some type-aware findings can be missed. A staged pass is not a replacement for the complete required CI gate.

- `make detekt-staged`: fast developer/agent feedback; no `.env`, release credentials, Node, Chrome, emulator, or simulator setup is required.
- `make detekt-ci`: complete baselined analysis, **CI only**. Do not run it as a local edit/pre-push loop.
- `make lint`: the existing Android Lint command and `check-env` behavior are unchanged.
- `make help`: lists staged, CI-only, and hook-install commands separately.

Install the repository-owned pre-commit hook explicitly:

```sh
make install-git-hooks
```

Installation sets repository-local `core.hooksPath=.githooks` and is idempotent. It refuses to shadow unrelated local/global hook paths or existing executable hooks. Migrate those hooks first; `CONFIRM_REPLACE_HOOKS=1 make install-git-hooks` is an explicit confirmation, not an automatic override. No full-analysis pre-push hook is installed.

The hook exits without Gradle when no Kotlin file is staged and blocks a commit on a staged finding. `SKIP_DETEKT=1 git commit ...` is an emergency local bypass only; hooks are optional/bypassable and CI remains authoritative.

Do not change baselines, suppressions, or broad exclusions merely to obtain a pass. Baseline maintenance requires explicit review/authorization. The initial findings remain preserved in `config/detekt/initial-inventory/`; accepted Detekt KMP/serialization limitations are documented in `config/detekt/ADOPTION-REPORT.md`. See `config/detekt/CI-ENFORCEMENT.md` for publishing dependencies, SARIF permissions, and the repository-owner required-check configuration.

## Security / Disclosure

If you find any bug that may be a security problem, please follow our instructions at in our [security policy](https://github.com/emartech/engagement-cloud-sdk/security/policy) on how to report it. Please do not create GitHub issues or Zendesk tickets for security-related doubts or problems.

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](https://github.com/emartech/engagement-cloud-sdk#coc-ov-file) at all times.

## Licensing

Copyright 2025-2026 SAP. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available via the [REUSE tool](https://api.reuse.software/info/github.com/emartech/engagement-cloud-sdk).
