# Engagement Cloud SDK — Release Process

This document describes how to publish a new release of the Engagement Cloud SDK, verify that everything worked, handle errors, and revoke a release if needed.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Before You Start](#before-you-start)
- [Step 1: Prepare the Release](#step-1-prepare-the-release)
- [Step 2: Trigger the Publish Pipeline](#step-2-trigger-the-publish-pipeline)
- [Step 3: Monitor the Pipeline](#step-3-monitor-the-pipeline)
- [Step 4: Verify Staged Artifacts](#step-4-verify-staged-artifacts)
- [Step 5: Promote to Maven Central](#step-5-promote-to-maven-central)
- [Verification Checklist](#verification-checklist)
- [Error Handling](#error-handling)
- [Revoking a Release](#revoking-a-release)
- [Point of No Return](#point-of-no-return)
- [Workflows Reference](#workflows-reference)
- [Secrets and Tokens](#secrets-and-tokens)
- [FAQ](#faq)

---

## Overview

A release publishes the SDK to **six destinations**:

| Destination | Platform | What Gets Published | Revocable? |
|-------------|----------|---------------------|------------|
| **Maven Central** | Android | `com.sap.engagement-cloud:engagement-cloud-sdk`, `-android-fcm`, `-android-hms` + KMP metadata + iOS/JS publications | Only before promotion |
| **GitHub Packages (Maven)** | Android | Same artifacts as Maven Central | Yes |
| **GitHub Packages (NPM)** | Web | `@sap/engagement-cloud-sdk` | Yes |
| **GitHub Release** | All | XCFramework zips, web bundle zip, release notes | Yes |
| **GitHub Pages (CDN)** | Web | `engagement-cloud-sdk.js`, loader, service worker, TypeScript defs + Docusaurus docs | Yes |
| **SPM (Package.swift)** | iOS | `Package.swift` on `main` pointing to XCFramework download URLs with checksums | Yes (reverts to previous) |

The release is a **two-phase process**: the publish pipeline stages everything automatically, then you manually promote to Maven Central after verification.

## Architecture

```
                           ┌──────────────────────────────────────────────┐
                           │          publish_artifacts.yml               │
                           │          (automatic on tag push)             │
                           ├──────────────────────────────────────────────┤
                           │                                              │
  Tag push (1.2.3)  ──────►  derive-version ─► create-tag ─► create-release
  or workflow_dispatch     │       │                               │      │
                           │       ├── KMP Pipeline (macOS) ──────────────┤
                           │       │    ├─ Build + Test all KMP targets   │
                           │       │    ├─ Publish to GitHub Packages     │
                           │       │    └─ Stage to Maven Central ◄── PENDING, not promoted
                           │       │                                      │
                           │       ├── JS Pipeline (ubuntu) ─────────────┤
                           │       │    ├─ Build + Test JS/Web            │
                           │       │    ├─ Publish NPM to GitHub Packages │
                           │       │    └─ Upload web bundle to Release   │
                           │       │                                      │
                           │       ├── iOS Pipeline (macOS) ─────────────┤
                           │       │    ├─ Build + Test iOS               │
                           │       │    ├─ Upload XCFrameworks to Release │
                           │       │    └─ Commit Package.swift to main   │
                           │       │                                      │
                           │       ├── Deploy Pages ─────────────────────┤
                           │       │    ├─ CDN artifacts (versioned + latest)
                           │       │    └─ Docusaurus docs                │
                           │       │                                      │
                           │       └── Slack notification                 │
                           └──────────────────────────────────────────────┘

                                         │  (you verify everything)
                                         ▼

                           ┌──────────────────────────────────────────────┐
                           │      promote_maven_central.yml               │
                           │      (manual workflow_dispatch)              │
                           ├──────────────────────────────────────────────┤
                           │  Promotes PENDING ──► Maven Central          │
                           │  ⚠ IRREVERSIBLE after this point            │
                           └──────────────────────────────────────────────┘
```

## Before You Start

### Prerequisites

1. **CHANGELOG.md updated** — Rename `## [Unreleased]` to `## [x.y.z] - YYYY-MM-DD` and add a fresh empty `## [Unreleased]` section above it. The pipeline extracts release notes from the version-specific section.

2. **All CI checks passing** — The `on_push_workflow.yml` CI must be green on `main`. Don't release from a broken build.

3. **Sonatype namespace verified** — If this is the first release under `com.sap.engagement-cloud`, the namespace must be approved on Sonatype Central Portal. This is a one-time manual step. Check at https://central.sonatype.com/publishing/namespaces.

4. **Secrets configured** — All required GitHub secrets must be set (see [Secrets and Tokens](#secrets-and-tokens)).

5. **E2E_DISPATCH_TOKEN** — A fine-grained PAT for cross-repo dispatch must exist as a GitHub secret (for nightly E2E triggers — not blocking for releases, but should be set up).

### Version Format

Versions must follow strict **semver**: `x.y.z` (e.g., `1.0.0`, `2.3.1`). No prefixes, no pre-release suffixes. The pipeline rejects anything that doesn't match `^[0-9]+\.[0-9]+\.[0-9]+$`.

---

## Step 1: Prepare the Release

### 1a. Update CHANGELOG.md

Edit `CHANGELOG.md` on `main`:

```markdown
## [Unreleased]
<!-- keep this section empty for future changes -->

## [1.2.3] - 2026-03-17

### Added
- New push notification grouping feature
- Inbox message expiration support

### Fixed
- Fixed crash on iOS 15 when deep link URL is nil

### Changed
- Minimum iOS deployment target raised to iOS 14
```

### 1b. Commit and push to main

```bash
git add CHANGELOG.md
git commit -m "Prepare release 1.2.3"
git push origin main
```

### 1c. Wait for CI

Wait for the `Engagement Cloud SDK CI` workflow to pass on your commit. Check at:
https://github.com/emartech/engagement-cloud-sdk/actions/workflows/on_push_workflow.yml

---

## Step 2: Trigger the Publish Pipeline

You have two options:

### Option A: Tag push (recommended)

```bash
git tag 1.2.3
git push origin 1.2.3
```

The `publish_artifacts.yml` workflow triggers automatically on tag push matching `[0-9]*.[0-9]*.[0-9]*`.

### Option B: Manual dispatch

Go to **Actions > Publish Artifacts > Run workflow**:
- **version**: `1.2.3`
- **dry_run**: `false`

This creates the tag for you (if it doesn't exist) and runs the same pipeline.

### Option C: Dry run (test the pipeline without publishing)

Go to **Actions > Publish Artifacts > Run workflow**:
- **version**: `1.2.3`
- **dry_run**: `true`

This builds and tests everything but doesn't publish, stage, or create a release. Useful for validating a release candidate.

---

## Step 3: Monitor the Pipeline

Watch the workflow at:
https://github.com/emartech/engagement-cloud-sdk/actions/workflows/publish_artifacts.yml

The pipeline runs these jobs (in dependency order):

| Job | Runner | Duration (approx.) | What It Does |
|-----|--------|-------|--------------|
| derive-version | ubuntu | ~10s | Validates version format |
| create-tag | ubuntu | ~10s | Creates git tag (workflow_dispatch only) |
| create-release | ubuntu | ~15s | Creates GitHub Release with CHANGELOG notes |
| **KMP Pipeline** | macOS-xlarge | ~15-25 min | Builds/tests all KMP targets, publishes to GitHub Packages, **stages to Maven Central** |
| **JS Pipeline** | ubuntu | ~8-12 min | Builds/tests JS, publishes NPM, uploads web bundle |
| **iOS Pipeline** | macOS-xlarge | ~15-20 min | Builds/tests iOS, uploads XCFrameworks, commits Package.swift |
| Deploy Pages | ubuntu | ~3-5 min | Deploys CDN + Docusaurus docs to GitHub Pages |
| Report / Slack | ubuntu | ~5s | Sends success/failure notification to `#mobile-team-ci` |

KMP Pipeline, JS Pipeline, and iOS Pipeline **run in parallel**. Deploy Pages waits for all three to finish.

### What to watch for

- **KMP Pipeline** is the most critical — it builds ALL Kotlin Multiplatform targets (Android, iOS, JS, metadata) and stages to Maven Central. If this fails, nothing reaches Maven Central.
- **iOS Pipeline** commits `Package.swift` to `main` — you'll see a bot commit appear on the main branch.
- **Deploy Pages** force-pushes to the `gh-pages` branch. This is normal.
- **Slack** will notify `#mobile-team-ci` with success or failure.

---

## Step 4: Verify Staged Artifacts

After the pipeline completes successfully, **do not promote to Maven Central yet**. First verify:

### GitHub Release

Check https://github.com/emartech/engagement-cloud-sdk/releases/tag/1.2.3

Verify these assets are attached:
- [ ] `EngagementCloudSDK.xcframework.zip`
- [ ] `EngagementCloudSDKNotificationService.xcframework.zip`
- [ ] `engagement-cloud-sdk-web-1.2.3.zip`
- [ ] Release notes from CHANGELOG are shown

### GitHub Packages (Maven)

Check https://github.com/orgs/emartech/packages?repo_name=engagement-cloud-sdk

Verify these packages have version `1.2.3`:
- [ ] `com.sap.engagement-cloud.engagement-cloud-sdk`
- [ ] `com.sap.engagement-cloud.engagement-cloud-sdk-android-fcm`
- [ ] `com.sap.engagement-cloud.engagement-cloud-sdk-android-hms`

### GitHub Packages (NPM)

Check https://github.com/emartech/engagement-cloud-sdk/packages

- [ ] `@sap/engagement-cloud-sdk` version `1.2.3` exists

### GitHub Pages (CDN)

Check these URLs return content (not 404):
- [ ] `https://emartech.github.io/engagement-cloud-sdk/1.2.3/engagement-cloud-sdk.js`
- [ ] `https://emartech.github.io/engagement-cloud-sdk/1.2.3/engagement-cloud-sdk-loader.js`
- [ ] `https://emartech.github.io/engagement-cloud-sdk/1.2.3/ec-service-worker.js`
- [ ] `https://emartech.github.io/engagement-cloud-sdk/latest/engagement-cloud-sdk.js` (should match 1.2.3)
- [ ] `https://emartech.github.io/engagement-cloud-sdk/docs/` (Docusaurus docs)

### SPM (Package.swift)

Check `Package.swift` on the `main` branch:
- [ ] URLs point to `releases/download/1.2.3/EngagementCloudSDK.xcframework.zip`
- [ ] Checksums are non-empty 64-character hex strings
- [ ] Bot commit message: `"Update Package.swift for SPM release 1.2.3"`

To test SPM resolution locally:
```bash
# In a test Xcode project
# Add package dependency: https://github.com/emartech/engagement-cloud-sdk.git
# Verify it resolves to 1.2.3
```

### Maven Central Staging

Check https://central.sonatype.com/publishing/deployments

- [ ] A deployment exists containing `1.2.3` in the name
- [ ] Status is **PENDING** or **VALIDATED** (not PUBLISHED, not FAILED)

If status is VALIDATED, the artifacts passed Sonatype's automated checks (POM completeness, signature validity, Javadoc presence) and are ready for promotion.

---

## Step 5: Promote to Maven Central

**Only do this after completing the verification checklist above.**

Go to **Actions > Promote to Maven Central > Run workflow**:
- **version**: `1.2.3`
- **deployment_id**: *(leave empty — auto-detected)*
- **dry_run**: `false`

Or first do a dry run to confirm the deployment is found:
- **dry_run**: `true`

The workflow calls the Sonatype Central Portal API to promote the staged deployment. Artifacts typically appear on Maven Central within **10-30 minutes** after promotion.

### Verify on Maven Central

After ~30 minutes, check:
- [ ] https://central.sonatype.com/artifact/com.sap.engagement-cloud/engagement-cloud-sdk/1.2.3
- [ ] https://central.sonatype.com/artifact/com.sap.engagement-cloud/engagement-cloud-sdk-android-fcm/1.2.3
- [ ] https://central.sonatype.com/artifact/com.sap.engagement-cloud/engagement-cloud-sdk-android-hms/1.2.3

To test Gradle resolution:
```kotlin
// In a test project's build.gradle.kts
dependencies {
    implementation("com.sap.engagement-cloud:engagement-cloud-sdk:1.2.3")
    implementation("com.sap.engagement-cloud:engagement-cloud-sdk-android-fcm:1.2.3")
}
```

---

## Verification Checklist

Copy this into the release PR or Slack thread:

```
Release 1.2.3 Verification:

Pipeline:
- [ ] publish_artifacts.yml completed successfully
- [ ] Slack notification received in #mobile-team-ci

GitHub Release:
- [ ] Release exists with correct notes
- [ ] EngagementCloudSDK.xcframework.zip attached
- [ ] EngagementCloudSDKNotificationService.xcframework.zip attached
- [ ] engagement-cloud-sdk-web-1.2.3.zip attached

GitHub Packages:
- [ ] Maven: engagement-cloud-sdk 1.2.3
- [ ] Maven: engagement-cloud-sdk-android-fcm 1.2.3
- [ ] Maven: engagement-cloud-sdk-android-hms 1.2.3
- [ ] NPM: @sap/engagement-cloud-sdk 1.2.3

CDN (GitHub Pages):
- [ ] /1.2.3/engagement-cloud-sdk.js returns 200
- [ ] /latest/engagement-cloud-sdk.js matches 1.2.3
- [ ] /docs/ loads Docusaurus site

iOS SPM:
- [ ] Package.swift on main points to 1.2.3
- [ ] Xcode resolves the package (optional manual test)

Maven Central:
- [ ] Staging deployment in PENDING/VALIDATED state
- [ ] Promoted via promote_maven_central.yml
- [ ] Artifacts visible on central.sonatype.com (~30 min)
```

---

## Error Handling

### KMP Pipeline failed

**Symptoms**: `KMP Pipeline (Build, Test, Publish, Stage to Maven Central)` job is red.

**Impact**: No artifacts on GitHub Packages, nothing staged to Maven Central. iOS and JS pipelines may have succeeded independently.

**What to do**:
1. Check the workflow logs for the failing step
2. Common causes:
   - **Gradle build failure**: Code doesn't compile. Fix on main and re-tag.
   - **Test failure**: A test broke. Fix on main and re-tag.
   - **Signing key error**: `SONATYPE_SIGNING_SECRET_KEY_RING_FILE` secret may be expired or malformed. Re-upload the base64-encoded GPG key.
   - **Sonatype credentials expired**: `OSSRH_USERNAME`/`OSSRH_PASSWORD` may need rotation. Generate a new token at https://central.sonatype.org/publish/generate-portal-token/
   - **GitHub Packages auth**: `GITHUB_TOKEN` permissions issue — check the workflow `permissions` block.
3. After fixing: delete the tag, delete the GitHub Release, then re-tag:
   ```bash
   git push origin :refs/tags/1.2.3   # delete remote tag
   git tag -d 1.2.3                    # delete local tag
   gh release delete 1.2.3 --yes       # delete the release
   git tag 1.2.3
   git push origin 1.2.3
   ```

### iOS Pipeline failed

**Symptoms**: `iOS Pipeline (Build, Test, Publish)` job is red.

**Impact**: No XCFrameworks on the GitHub Release, no Package.swift update. Android and JS are unaffected.

**What to do**:
1. Common causes:
   - **Konan cache miss + timeout**: The Konan (Kotlin/Native) cache can miss on new runner images. Usually passes on retry.
   - **XCFramework build failure**: iOS-specific compilation error. Check the `make pipeline-ios` logs.
   - **Package.swift push conflict**: Another commit hit main between checkout and push. Re-run the job.
2. You can re-run the failed job from the Actions UI without re-triggering the entire pipeline.
3. If re-run doesn't work, fix the issue and follow the re-tag process above.

### JS Pipeline failed

**Symptoms**: `JS Pipeline (Build, Test, Publish)` job is red.

**Impact**: No NPM package, no CDN artifacts, no web bundle on the Release. Deploy Pages will also fail (missing CDN artifacts).

**What to do**:
1. Common causes:
   - **Chrome install failure**: The Chrome install step uses `apt-key` which is deprecated. If the GPG key URL changes, this step needs updating.
   - **Karma test timeout**: JS browser tests can be flaky. Re-run.
   - **NPM publish conflict**: Version already exists on GitHub Packages. Check if a previous partial run published it.
2. Re-run the failed job, or fix and re-tag.

### Deploy Pages failed

**Symptoms**: `Deploy to GitHub Pages` job is red.

**Impact**: CDN URLs for the new version don't work. Previous versions are unaffected (the deployment uses `keep_files: true`).

**What to do**:
1. Common causes:
   - **Empty CDN artifacts**: The JS Pipeline didn't upload the `js-cdn-artifacts` artifact. Check JS Pipeline logs.
   - **Docusaurus build failure**: The docs submodule may have broken dependencies. Check the `npm ci` / `npx docusaurus build` step.
   - **Submodule checkout failure**: The `engagement-cloud-sdk-docs` repo must be public (the checkout uses `GITHUB_TOKEN` which is scoped to the SDK repo).
2. Re-run the Deploy Pages job. It downloads artifacts from the same workflow run.

### Maven Central staging failed

**Symptoms**: KMP Pipeline succeeded but the `Stage all KMP artifacts to Maven Central` step is red.

**Impact**: Artifacts are on GitHub Packages but NOT on Maven Central staging. Everything else (Release, CDN, SPM) is fine.

**What to do**:
1. Common causes:
   - **Sonatype credentials**: Token expired or incorrect
   - **Signing failure**: GPG key issue
   - **Duplicate deployment**: A staging deployment for this version already exists (from a previous attempt)
2. Check https://central.sonatype.com/publishing/deployments — if a PENDING deployment exists from a previous attempt, drop it first, then re-run
3. You can re-run the KMP Pipeline job. The `pipeline-android` step will skip publishing to GitHub Packages (version already exists) and proceed to staging.

### Promote to Maven Central failed

**Symptoms**: `Promote to Maven Central` workflow is red.

**What to do**:
1. Check the deployment status at https://central.sonatype.com/publishing/deployments
2. If status is **FAILED**: The deployment didn't pass Sonatype validation. Check the failure reason in the portal. Common issues: missing POM fields, invalid signatures, namespace not verified.
3. If status is **PENDING**: The promote API call may have timed out. Check if promotion is actually in progress in the portal.
4. If the deployment is gone: It was dropped (by the revoke workflow or manually). You'll need to re-stage by re-running the KMP Pipeline.

---

## Revoking a Release

### Before Maven Central promotion (fully reversible)

If you need to revoke a release **before** promoting to Maven Central, everything can be cleanly undone:

Go to **Actions > Revoke Release > Run workflow**:
- **version**: `1.2.3`
- **dry_run**: `true` (start with dry run to see what will happen)

Review the plan, then re-run with `dry_run: false`.

The revoke workflow will:
1. **Drop the Maven Central staging deployment** (artifacts never reach Central)
2. **Delete the GitHub Release** (removes XCFramework zips, web bundle)
3. **Delete the git tag** (`1.2.3`)
4. **Remove the version directory from GitHub Pages** (`/1.2.3/`), rebuild `/latest/` from the previous version
5. **Delete GitHub Packages versions** (Maven + NPM)
6. **Revert Package.swift** on `main` to point at the previous release

After revocation, it's as if the release never happened.

### After Maven Central promotion (partially irreversible)

If you already promoted to Maven Central, run the revoke workflow — it will still clean up everything **except** Maven Central:

- GitHub Release, tag, Pages, Packages, SPM: all revoked normally
- Maven Central staging: the workflow detects the deployment was already published and skips the drop
- A **reminder is logged** with your options for dealing with the immutable Maven Central artifacts

Your options for the Maven Central artifacts:

1. **Security vulnerability**: Contact Sonatype support at https://central.sonatype.org/faq/can-i-delete/ to request artifact deprecation
2. **Broken release (non-security)**: Publish a patch version (`1.2.4`) with the fix. Consumers will naturally pick up the newer version.
3. **Need to redirect**: Publish a new version with `<relocation>` in the POM to redirect consumers to the correct coordinates

---

## Point of No Return

The release process has one irreversible step:

```
   Publish pipeline         ◄── fully reversible (drop staging, delete everything)
         │
         ▼
   Verify artifacts         ◄── fully reversible
         │
         ▼
   ┌─────────────────┐
   │  PROMOTE TO      │
   │  MAVEN CENTRAL   │     ◄── ⚠  POINT OF NO RETURN for Maven Central
   └─────────────────┘
         │
         ▼
   Artifacts on Central     ◄── IMMUTABLE. Cannot be deleted or replaced.
                                 Other destinations (GitHub, CDN, SPM) can
                                 still be revoked.
```

**Before you promote**, make sure:
- All verification checks pass
- You've tested the staged artifacts if this is a significant release
- The CHANGELOG entry is correct (it's already published in the GitHub Release)
- The version number is correct (you can't re-use it on Maven Central)

**After you promote**, the `com.sap.engagement-cloud:engagement-cloud-sdk:1.2.3` coordinates are permanently claimed. Even if you revoke everything else, anyone who already resolved `1.2.3` from Maven Central will keep getting those artifacts.

---

## Workflows Reference

| Workflow | Trigger | File | Purpose |
|----------|---------|------|---------|
| **Publish Artifacts** | Tag push or manual | `publish_artifacts.yml` | Build, test, publish to all destinations (Maven Central = staging only) |
| **Promote to Maven Central** | Manual only | `promote_maven_central.yml` | Promote staged deployment to Maven Central (irreversible) |
| **Revoke Release** | Manual only | `revoke_release.yml` | Remove artifacts from all destinations (dry_run defaults to true) |
| **CI** | Push to main, PRs | `on_push_workflow.yml` | Build and test (no publishing) |
| **Nightly** | Schedule (02:00 UTC) | `nightly_workflow.yml` | Full build/test + E2E dispatch |
| **CodeQL** | Schedule + PRs | `codeql.yml` | Security analysis |

### Makefile Targets (local development)

| Target | What It Does |
|--------|--------------|
| `make build-android` | Build Android artifacts |
| `make build-ios` | Build iOS artifacts |
| `make build-web` | Build JS/Web artifacts |
| `make test` | Run all tests (android + web + ios) |
| `make lint` | Run lint checks |
| `make pipeline-android` | Full Android pipeline (build + lint + test + optional publish) |
| `make pipeline-ios` | Full iOS pipeline (test + optional XCFramework build) |
| `make pipeline-js` | Full JS pipeline (build + test) |
| `make stage-maven-central` | Stage to Maven Central locally (for testing) |
| `make release` | Full MavenCentral release (stage + auto-promote) — **use CI instead** |
| `make prepare-spm` | Build local SPM Package.swift for testing |

---

## Secrets and Tokens

These GitHub secrets must be configured on the repository:

### Sonatype / Maven Central

| Secret | Purpose | Rotation |
|--------|---------|----------|
| `OSSRH_USERNAME` | Sonatype Central Portal user token (username part) | Generate at https://central.sonatype.org/publish/generate-portal-token/ |
| `OSSRH_PASSWORD` | Sonatype Central Portal user token (password part) | Same as above |
| `SONATYPE_STAGING_PROFILE_ID` | Staging profile ID for `com.sap.engagement-cloud` | Found in Central Portal > Publishing > Namespaces |
| `SONATYPE_SIGNING_KEY_ID` | GPG key ID for artifact signing | 8-char hex key ID |
| `SONATYPE_SIGNING_PASSWORD` | GPG key passphrase | — |
| `SONATYPE_SIGNING_SECRET_KEY_RING_FILE` | Base64-encoded GPG secret keyring (`secring.asc.gpg`) | `base64 < secring.asc.gpg` |

### Android

| Secret | Purpose |
|--------|---------|
| `ANDROID_RELEASE_KEY_PASSWORD` | Android signing key password |
| `ANDROID_RELEASE_KEY_ALIAS` | Android signing key alias |
| `ANDROID_RELEASE_STORE_PASSWORD` | Android keystore password |
| `ANDROID_RELEASE_STORE_FILE_BASE64` | Base64-encoded Android keystore |

### General

| Secret | Purpose |
|--------|---------|
| `SLACK_WEBHOOK` | Slack incoming webhook for `#mobile-team-ci` |
| `E2E_DISPATCH_TOKEN` | Fine-grained PAT for cross-repo dispatch to test-app |
| `GOOGLE_SERVICES_API_KEY` | Firebase API key |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64-encoded `google-services.json` |
| `FIREBASE_PROJECT_ID` | Firebase project ID |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase service account JSON |
| `GOOGLE_OAUTH_SERVER_CLIENT_ID` | Google OAuth client ID |

---

## FAQ

### Can I release from a branch other than main?

No. The tag must point to a commit on `main`. The iOS pipeline pushes `Package.swift` back to `main`, and the CI workflow only builds on `main` + PRs.

### What if I pushed a tag with the wrong version?

Delete the tag and the release, then re-tag:
```bash
git push origin :refs/tags/1.2.3
git tag -d 1.2.3
gh release delete 1.2.3 --yes
# fix whatever needs fixing
git tag 1.2.4
git push origin 1.2.4
```

Or use the Revoke Release workflow to clean everything up.

### Can I re-use a version number?

On GitHub (Releases, Packages, Pages): yes, after revoking. On Maven Central: **no, never**. Once `1.2.3` is promoted to Maven Central, that version is permanently taken. You must use a new version number.

### How long do staged artifacts stay on the Central Portal?

Sonatype automatically drops unclaimed deployments after **~90 days**. If you forget to promote, you'll need to re-stage.

### Why does the KMP Pipeline run on macOS?

Kotlin Multiplatform conditionally applies the KMMBridge plugin only on macOS (`System.getProperty("os.name").contains("Mac")`). The pipeline builds iOS XCFrameworks and publishes iOS Maven publications — all of which require macOS. The KMP Pipeline publishes **all** platform artifacts (Android, iOS, JS, metadata) from a single macOS job to ensure consistency.

### What if Slack notification says failure but all jobs are green?

The `report-slack` job checks all upstream job results. If any job was **cancelled** (e.g., you manually cancelled it), Slack reports failure. Check the individual job results in the Actions UI.

### How do I test a release without publishing?

Use the **dry run** option: Actions > Publish Artifacts > Run workflow > set `dry_run: true`. This runs the entire build and test pipeline without creating a release, publishing packages, staging to Maven Central, or deploying to Pages.

### What happens to CDN URLs when I revoke?

The version-specific URL (`/1.2.3/engagement-cloud-sdk.js`) will return 404. The `/latest/` URL is rebuilt to point at the previous version. Any customer scripts hardcoded to the version URL will break — coordinate with customers before revoking a published release.
