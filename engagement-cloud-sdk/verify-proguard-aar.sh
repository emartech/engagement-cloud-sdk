#!/usr/bin/env bash
# Verification script for LOCAL-024-4: ProGuard consumer rules in AAR
# This script verifies that the SDK AAR contains the expected ProGuard rules.
#
# Usage: ./engagement-cloud-sdk/verify-proguard-aar.sh
# Exit code 0 = all checks pass, non-zero = failure

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== ProGuard AAR Verification ==="

# Find the AAR file
AAR_PATH=$(find "$SCRIPT_DIR/build/outputs" -name "*.aar" 2>/dev/null | head -1)
if [ -z "$AAR_PATH" ]; then
  echo "FAIL: No AAR file found. Run ./gradlew :engagement-cloud-sdk:assembleAndroidMain first."
  exit 1
fi
echo "AAR: $AAR_PATH"

# Check 1: AAR contains proguard.txt
echo ""
echo "Check 1: AAR contains proguard.txt"
if unzip -l "$AAR_PATH" | grep -q "proguard.txt"; then
  echo "  PASS: proguard.txt found in AAR"
else
  echo "  FAIL: proguard.txt NOT found in AAR"
  exit 1
fi

# Check 2: proguard.txt contains the -keep rule
echo ""
echo "Check 2: proguard.txt contains the -keep rule"
PROGUARD_CONTENT=$(unzip -p "$AAR_PATH" proguard.txt)
if echo "$PROGUARD_CONTENT" | grep -qF -- '-keep public class !com.sap.ec.EngagementCloud, com.sap.ec.**'; then
  echo "  PASS: -keep rule found"
else
  echo "  FAIL: -keep rule NOT found in proguard.txt"
  echo "  Content: $PROGUARD_CONTENT"
  exit 1
fi

# Check 3: proguard.txt does NOT contain the unnecessary -dontwarn rule
echo ""
echo "Check 3: proguard.txt does NOT contain -dontwarn java.lang.invoke.StringConcatFactory"
if echo "$PROGUARD_CONTENT" | grep -q "dontwarn java.lang.invoke.StringConcatFactory"; then
  echo "  FAIL: unnecessary -dontwarn rule still present"
  exit 1
else
  echo "  PASS: -dontwarn rule correctly removed"
fi

# Check 4: gradle.properties retains android.r8.strictFullModeForKeepRules=false
echo ""
echo "Check 4: gradle.properties retains android.r8.strictFullModeForKeepRules=false"
if grep -q "android.r8.strictFullModeForKeepRules=false" "$PROJECT_DIR/gradle.properties"; then
  echo "  PASS: R8 strict full mode setting retained"
else
  echo "  FAIL: android.r8.strictFullModeForKeepRules=false missing from gradle.properties"
  exit 1
fi

echo ""
echo "=== All ProGuard AAR checks passed ==="
exit 0
