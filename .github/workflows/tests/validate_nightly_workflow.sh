#!/usr/bin/env bash
# Validation script for nightly_workflow.yml
# Tests the structural requirements for SDK-955
set -euo pipefail

WORKFLOW=".github/workflows/nightly_workflow.yml"
ERRORS=0

# Helper: fail with message
fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

pass() {
  echo "PASS: $1"
}

# 1. Workflow triggers: only schedule and workflow_dispatch (no push trigger)
if grep -q '^\s*push:' "$WORKFLOW"; then
  fail "Workflow should NOT trigger on push"
else
  pass "Workflow does not trigger on push"
fi

if grep -q 'workflow_dispatch' "$WORKFLOW" && grep -q 'schedule:' "$WORKFLOW"; then
  pass "Workflow triggers on schedule and workflow_dispatch"
else
  fail "Workflow must trigger on both schedule and workflow_dispatch"
fi

# 2. Permissions block exists with minimal scope
if grep -q '^permissions:' "$WORKFLOW"; then
  pass "Permissions block exists"
else
  fail "Missing explicit permissions block"
fi

# 3. Build job uses per-platform OS matrix (not hardcoded macos-latest)
BUILD_SECTION=$(sed -n '/^  build:/,/^  [a-z]/p' "$WORKFLOW")
if echo "$BUILD_SECTION" | grep -q 'runs-on: \${{ matrix.os }}'; then
  pass "Build job uses matrix.os for runner"
else
  fail "Build job should use matrix.os for runner selection"
fi

if echo "$BUILD_SECTION" | grep -q 'os: ubuntu-latest' && echo "$BUILD_SECTION" | grep -q 'os: macos-latest'; then
  pass "Build matrix includes both ubuntu-latest and macos-latest"
else
  fail "Build matrix must include ubuntu-latest (android/web) and macos-latest (ios)"
fi

# 4. Test job uses per-platform OS matrix
TEST_SECTION=$(sed -n '/^  test:/,/^  [a-z]/p' "$WORKFLOW")
if echo "$TEST_SECTION" | grep -q 'runs-on: \${{ matrix.os }}'; then
  pass "Test job uses matrix.os for runner"
else
  fail "Test job should use matrix.os for runner selection"
fi

# 5. Web Chrome install uses Linux apt-get (not brew)
if grep -q 'brew install.*google-chrome' "$WORKFLOW"; then
  fail "Web Chrome setup still uses brew (macOS-only) -- should use apt-get for Linux"
else
  pass "Web Chrome setup does not use brew"
fi

if grep -q 'apt-get install.*google-chrome' "$WORKFLOW"; then
  pass "Web Chrome setup uses apt-get for Linux"
else
  fail "Web Chrome setup must use apt-get for Linux runner"
fi

# 6. Unused secrets removed from env block
SECRET_ERRORS=0
for SECRET in OSSRH_USERNAME OSSRH_PASSWORD SONATYPE_STAGING_PROFILE_ID SONATYPE_SIGNING_KEY_ID SONATYPE_SIGNING_PASSWORD SONATYPE_SIGNING_SECRET_KEY_RING_FILE DETECT_LATEST_RELEASE_VERSION DETECT_PROJECT_USER_GROUPS DETECT_PROJECT_VERSION_DISTRIBUTION BLACKDUCK_ACCESS_TOKEN BLACKDUCK_URL ANDROID_RELEASE_KEY_PASSWORD ANDROID_RELEASE_KEY_ALIAS ANDROID_RELEASE_STORE_PASSWORD ANDROID_RELEASE_STORE_FILE_BASE64; do
  if grep -q "$SECRET" "$WORKFLOW"; then
    fail "Unused secret $SECRET should be removed from env block"
    SECRET_ERRORS=$((SECRET_ERRORS + 1))
  fi
done
if [ $SECRET_ERRORS -eq 0 ]; then
  pass "All unused secrets removed from env block"
fi

# 7. E2E dispatch job exists
if grep -q 'trigger-e2e:' "$WORKFLOW"; then
  pass "E2E dispatch job exists"
else
  fail "Missing trigger-e2e job"
fi

# 8. E2E dispatch uses E2E_DISPATCH_TOKEN (not GITHUB_TOKEN)
if grep -q 'E2E_DISPATCH_TOKEN' "$WORKFLOW"; then
  pass "E2E dispatch uses E2E_DISPATCH_TOKEN secret"
else
  fail "E2E dispatch must use E2E_DISPATCH_TOKEN secret"
fi

# 9. E2E dispatch targets engagement-cloud-sdk-test-app
if grep -q 'engagement-cloud-sdk-test-app' "$WORKFLOW"; then
  pass "E2E dispatch targets test-app repository"
else
  fail "E2E dispatch must target engagement-cloud-sdk-test-app"
fi

# 10. Reporting job includes trigger-e2e in needs
REPORT_SECTION=$(sed -n '/^  reporting:/,/^  [a-z]/p' "$WORKFLOW")
if echo "$REPORT_SECTION" | grep -q 'trigger-e2e'; then
  pass "Reporting job depends on trigger-e2e"
else
  fail "Reporting job must include trigger-e2e in needs"
fi

# 11. Slack messages reference E2E
if grep -i 'message:' "$WORKFLOW" | grep -qi 'e2e'; then
  pass "Slack messages reference E2E status"
else
  fail "Slack messages should mention E2E dispatch status"
fi

# 12. actionlint validation
if command -v actionlint &>/dev/null; then
  if actionlint "$WORKFLOW" 2>&1; then
    pass "actionlint passes"
  else
    fail "actionlint reports errors"
  fi
else
  echo "SKIP: actionlint not installed"
fi

# Summary
echo ""
echo "================================"
if [ $ERRORS -eq 0 ]; then
  echo "ALL CHECKS PASSED"
  exit 0
else
  echo "FAILURES: $ERRORS"
  exit 1
fi
