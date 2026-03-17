#!/usr/bin/env bash
# validate_groupid.sh -- Validate Maven groupId migration from com.sap to com.sap.engagement-cloud
#
# Checks:
# 1. All 4 group/coordinates declarations use "com.sap.engagement-cloud"
# 2. The Android namespace is still "com.sap" (not changed)
# 3. No remaining group = "com.sap" (without .engagement-cloud) in any build.gradle.kts
# 4. CHANGELOG.md has the breaking change documented
# 5. No com.sap" string literals remain in coordinates() calls in the fcm/hms files

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
ERRORS=0

pass() {
  echo "  PASS: $1"
}

fail() {
  echo "  FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

echo "=== Validate Maven groupId Migration ==="
echo ""

# --------------------------------------------------------------------------
# Check 1: engagement-cloud-sdk/build.gradle.kts has group = "com.sap.engagement-cloud"
# --------------------------------------------------------------------------
echo "Check 1: engagement-cloud-sdk group declaration"
SDK_BUILD="$REPO_ROOT/engagement-cloud-sdk/build.gradle.kts"
if grep -q '^group = "com\.sap\.engagement-cloud"' "$SDK_BUILD"; then
  pass "engagement-cloud-sdk group = \"com.sap.engagement-cloud\""
else
  fail "engagement-cloud-sdk group should be \"com.sap.engagement-cloud\""
fi

# --------------------------------------------------------------------------
# Check 2: ios-notification-service/build.gradle.kts has group = "com.sap.engagement-cloud"
# --------------------------------------------------------------------------
echo "Check 2: ios-notification-service group declaration"
IOS_BUILD="$REPO_ROOT/ios-notification-service/build.gradle.kts"
if grep -q '^group = "com\.sap\.engagement-cloud"' "$IOS_BUILD"; then
  pass "ios-notification-service group = \"com.sap.engagement-cloud\""
else
  fail "ios-notification-service group should be \"com.sap.engagement-cloud\""
fi

# --------------------------------------------------------------------------
# Check 3: engagement-cloud-sdk-android-fcm coordinates use com.sap.engagement-cloud
# --------------------------------------------------------------------------
echo "Check 3: fcm coordinates declaration"
FCM_BUILD="$REPO_ROOT/engagement-cloud-sdk-android-fcm/build.gradle.kts"
if grep -q 'coordinates("com\.sap\.engagement-cloud"' "$FCM_BUILD"; then
  pass "fcm coordinates use \"com.sap.engagement-cloud\""
else
  fail "fcm coordinates should use \"com.sap.engagement-cloud\""
fi

# --------------------------------------------------------------------------
# Check 4: engagement-cloud-sdk-android-hms coordinates use com.sap.engagement-cloud
# --------------------------------------------------------------------------
echo "Check 4: hms coordinates declaration"
HMS_BUILD="$REPO_ROOT/engagement-cloud-sdk-android-hms/build.gradle.kts"
if grep -q 'coordinates("com\.sap\.engagement-cloud"' "$HMS_BUILD"; then
  pass "hms coordinates use \"com.sap.engagement-cloud\""
else
  fail "hms coordinates should use \"com.sap.engagement-cloud\""
fi

# --------------------------------------------------------------------------
# Check 5: Android namespace in engagement-cloud-sdk is still "com.sap"
# --------------------------------------------------------------------------
echo "Check 5: Android namespace preserved"
if grep -q 'namespace = "com\.sap"' "$SDK_BUILD"; then
  pass "Android namespace is still \"com.sap\""
else
  fail "Android namespace should remain \"com.sap\" (R class namespace)"
fi

# --------------------------------------------------------------------------
# Check 6: No remaining group = "com.sap" (without .engagement-cloud) in any build.gradle.kts
# --------------------------------------------------------------------------
echo "Check 6: No stale group = \"com.sap\" declarations"
# Search for group = "com.sap" but NOT group = "com.sap.engagement-cloud"
STALE_GROUP=$(grep -rn 'group = "com\.sap"' "$REPO_ROOT"/*/build.gradle.kts "$REPO_ROOT"/build.gradle.kts 2>/dev/null | grep -v 'com\.sap\.engagement-cloud' || true)
if [ -z "$STALE_GROUP" ]; then
  pass "No stale group = \"com.sap\" declarations found"
else
  fail "Found stale group = \"com.sap\" declarations:"
  echo "    $STALE_GROUP"
fi

# --------------------------------------------------------------------------
# Check 7: No com.sap" string literals in fcm/hms coordinates() calls
# --------------------------------------------------------------------------
echo "Check 7: No stale coordinates in fcm/hms"
FCM_STALE=$(grep -n 'coordinates("com\.sap"' "$FCM_BUILD" 2>/dev/null || true)
HMS_STALE=$(grep -n 'coordinates("com\.sap"' "$HMS_BUILD" 2>/dev/null || true)
if [ -z "$FCM_STALE" ] && [ -z "$HMS_STALE" ]; then
  pass "No stale coordinates(\"com.sap\", ...) in fcm/hms"
else
  if [ -n "$FCM_STALE" ]; then
    fail "fcm still has coordinates(\"com.sap\", ...): $FCM_STALE"
  fi
  if [ -n "$HMS_STALE" ]; then
    fail "hms still has coordinates(\"com.sap\", ...): $HMS_STALE"
  fi
fi

# --------------------------------------------------------------------------
# Check 8: CHANGELOG.md documents the breaking groupId change
# --------------------------------------------------------------------------
echo "Check 8: CHANGELOG.md breaking change documented"
CHANGELOG="$REPO_ROOT/CHANGELOG.md"
if [ -f "$CHANGELOG" ] && grep -q 'groupId.*com\.sap.*com\.sap\.engagement-cloud' "$CHANGELOG"; then
  pass "CHANGELOG.md documents groupId breaking change"
else
  fail "CHANGELOG.md should document the Maven groupId breaking change"
fi

# --------------------------------------------------------------------------
# Summary
# --------------------------------------------------------------------------
echo ""
if [ "$ERRORS" -eq 0 ]; then
  echo "All groupId validation checks passed."
  exit 0
else
  echo "$ERRORS check(s) failed."
  exit 1
fi
