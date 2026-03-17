# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Release pipeline now triggers on semantic version tags instead of every push to main
- On-commit CI restricted to main branch and pull requests only
- Nightly workflow fixed: per-platform runner matrix, E2E dispatch to test-app
- Legacy iOS and JS release workflows removed; unified into publish_artifacts
- Environment variables scoped per-workflow (removed unused secrets from CI workflows)

### Added
- CHANGELOG.md following Keep a Changelog format
- Git submodule for engagement-cloud-sdk-docs with GitHub Pages deployment
- Snapshot versioning for on-commit builds (last-tag.commits-since-tag)
- Concurrency controls on CI and publish workflows

### Breaking
- **Maven groupId changed from `com.sap` to `com.sap.engagement-cloud`**
  - Update dependency declarations: `com.sap:engagement-cloud-sdk:x.y.z` becomes `com.sap.engagement-cloud:engagement-cloud-sdk:x.y.z`
  - Applies to: engagement-cloud-sdk, engagement-cloud-sdk-android-fcm, engagement-cloud-sdk-android-hms
