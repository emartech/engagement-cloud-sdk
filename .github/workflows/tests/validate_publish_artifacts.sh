#!/usr/bin/env bash
# Validation script for publish_artifacts.yml (SDK-953)
# Encodes all acceptance criteria and security requirements for tag-based semantic versioning.
#
# Usage: bash .github/workflows/tests/validate_publish_artifacts.sh
# Exit code: 0 if all checks pass, 1 if any check fails.

set -euo pipefail

PUBLISH_FILE=".github/workflows/publish_artifacts.yml"
ON_PUSH_FILE=".github/workflows/on_push_workflow.yml"
PASS=0
FAIL=0

check() {
  local description="$1"
  local result="$2"  # "pass" or "fail"
  if [ "$result" = "pass" ]; then
    echo "  PASS: $description"
    PASS=$((PASS + 1))
  else
    echo "  FAIL: $description"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== SDK-953 Validation: publish_artifacts.yml ==="
echo ""

# --- Trigger checks ---
echo "[Trigger Configuration]"

# 1. Must trigger on push: tags with numeric semver pattern
if grep -q "tags:" "$PUBLISH_FILE" && grep -q '\[0-9\]' "$PUBLISH_FILE"; then
  check "Triggers on push: tags with numeric semver pattern" "pass"
else
  check "Triggers on push: tags with numeric semver pattern" "fail"
fi

# 2. Must NOT trigger on push: branches: [main]
if grep -A2 'push:' "$PUBLISH_FILE" | grep -q 'branches:'; then
  check "Does NOT trigger on push to main branch" "fail"
else
  check "Does NOT trigger on push to main branch" "pass"
fi

# 3. Must have workflow_dispatch with version input
if grep -q "version:" "$PUBLISH_FILE" && grep -q "type: string" "$PUBLISH_FILE"; then
  check "Has workflow_dispatch with version string input" "pass"
else
  check "Has workflow_dispatch with version string input" "fail"
fi

# 4. Must have workflow_dispatch with dry_run input
if grep -q "dry_run:" "$PUBLISH_FILE"; then
  check "Has workflow_dispatch with dry_run input" "pass"
else
  check "Has workflow_dispatch with dry_run input" "fail"
fi

echo ""
echo "[Version Derivation]"

# 5. Must have derive-version job
if grep -q "derive-version:" "$PUBLISH_FILE"; then
  check "Has derive-version job" "pass"
else
  check "Has derive-version job" "fail"
fi

# 6. Version validation regex must exist
if grep -q '\^\\[0-9\\]' "$PUBLISH_FILE" || grep -q '\^\[0-9\]' "$PUBLISH_FILE"; then
  check "Version validation regex exists" "pass"
else
  check "Version validation regex exists" "fail"
fi

# 7. Validated version passed via GITHUB_OUTPUT
if grep -q 'GITHUB_OUTPUT' "$PUBLISH_FILE"; then
  check "Version passed via GITHUB_OUTPUT" "pass"
else
  check "Version passed via GITHUB_OUTPUT" "fail"
fi

echo ""
echo "[Tag Creation (workflow_dispatch)]"

# 8. Must have create-tag job
if grep -q "create-tag:" "$PUBLISH_FILE"; then
  check "Has create-tag job" "pass"
else
  check "Has create-tag job" "fail"
fi

# 9. create-tag must use GITHUB_TOKEN (not a PAT)
if grep -A30 "create-tag:" "$PUBLISH_FILE" | grep -q 'GITHUB_TOKEN'; then
  check "create-tag uses GITHUB_TOKEN for push" "pass"
else
  check "create-tag uses GITHUB_TOKEN for push" "fail"
fi

# 10. create-tag must check tag existence before creating
if grep -q 'git tag -l' "$PUBLISH_FILE"; then
  check "create-tag checks tag does not already exist" "pass"
else
  check "create-tag checks tag does not already exist" "fail"
fi

echo ""
echo "[Security: Force-push Elimination]"

# 11. No git tag -f anywhere in file
if grep -q 'git tag -f' "$PUBLISH_FILE"; then
  check "No git tag -f (force tag) in workflow" "fail"
else
  check "No git tag -f (force tag) in workflow" "pass"
fi

# 12. No --force anywhere in git push commands
if grep -q '\-\-force' "$PUBLISH_FILE"; then
  check "No --force in git push commands" "fail"
else
  check "No --force in git push commands" "pass"
fi

echo ""
echo "[Version Reference Consistency]"

# 13. No remaining env.VERSION references (all should use derive-version output)
ENV_VERSION_COUNT=$(grep -c 'env\.VERSION' "$PUBLISH_FILE" || true)
if [ "$ENV_VERSION_COUNT" -eq 0 ]; then
  check "No remaining env.VERSION references" "pass"
else
  check "No remaining env.VERSION references ($ENV_VERSION_COUNT found)" "fail"
fi

# 14. No workflow-level VERSION env var
if grep -E '^\s+VERSION:' "$PUBLISH_FILE" | grep -q 'run_number'; then
  check "No workflow-level VERSION: 0.0.run_number env var" "fail"
else
  check "No workflow-level VERSION: 0.0.run_number env var" "pass"
fi

echo ""
echo "[Pipeline Job Dependencies]"

# 15. android-pipeline needs derive-version and create-tag
if grep -A3 'android-pipeline:' "$PUBLISH_FILE" | grep -q 'derive-version'; then
  check "android-pipeline depends on derive-version" "pass"
else
  check "android-pipeline depends on derive-version" "fail"
fi

# 16. js-pipeline needs derive-version and create-tag
if grep -A3 'js-pipeline:' "$PUBLISH_FILE" | grep -q 'derive-version'; then
  check "js-pipeline depends on derive-version" "pass"
else
  check "js-pipeline depends on derive-version" "fail"
fi

# 17. ios-pipeline needs derive-version and create-tag
if grep -A3 'ios-pipeline:' "$PUBLISH_FILE" | grep -q 'derive-version'; then
  check "ios-pipeline depends on derive-version" "pass"
else
  check "ios-pipeline depends on derive-version" "fail"
fi

echo ""
echo "[GitHub Release Consolidation]"

# 18. Must have create-release job
if grep -q "create-release:" "$PUBLISH_FILE"; then
  check "Has consolidated create-release job" "pass"
else
  check "Has consolidated create-release job" "fail"
fi

# 19. No softprops/action-gh-release in iOS pipeline
if grep -q 'softprops/action-gh-release' "$PUBLISH_FILE"; then
  check "No softprops/action-gh-release (iOS early release removed)" "fail"
else
  check "No softprops/action-gh-release (iOS early release removed)" "pass"
fi

# 20. JS pipeline should NOT create release (only upload)
if grep -B2 -A5 'Upload JS Web Bundle' "$PUBLISH_FILE" | grep -q 'gh release create'; then
  check "JS pipeline does not create release (only uploads)" "fail"
else
  check "JS pipeline does not create release (only uploads)" "pass"
fi

echo ""
echo "[Slack Reporting]"

# 21. report-slack needs create-release
if grep -A3 'report-slack:' "$PUBLISH_FILE" | grep -q 'create-release'; then
  check "report-slack depends on create-release" "pass"
else
  check "report-slack depends on create-release" "fail"
fi

echo ""
echo "[On-Push Workflow: Snapshot Version]"

# 22. on_push_workflow.yml has snapshot version derivation
if grep -q 'snapshot-version' "$ON_PUSH_FILE"; then
  check "on_push_workflow has snapshot version derivation step" "pass"
else
  check "on_push_workflow has snapshot version derivation step" "fail"
fi

# 23. on_push_workflow uses git describe for version
if grep -q 'git describe' "$ON_PUSH_FILE"; then
  check "on_push_workflow uses git describe for snapshot version" "pass"
else
  check "on_push_workflow uses git describe for snapshot version" "fail"
fi

# 24. on_push_workflow has fallback for no tags
if grep -q '0\.0\.0\.' "$ON_PUSH_FILE"; then
  check "on_push_workflow has fallback version when no tags exist" "pass"
else
  check "on_push_workflow has fallback version when no tags exist" "fail"
fi

echo ""
echo "[actionlint Validation]"

# 25. actionlint passes on publish_artifacts.yml
if actionlint "$PUBLISH_FILE" 2>&1; then
  check "actionlint passes on publish_artifacts.yml" "pass"
else
  check "actionlint passes on publish_artifacts.yml" "fail"
fi

# 26. actionlint passes on on_push_workflow.yml
if actionlint "$ON_PUSH_FILE" 2>&1; then
  check "actionlint passes on on_push_workflow.yml" "pass"
else
  check "actionlint passes on on_push_workflow.yml" "fail"
fi

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
else
  echo "All checks passed!"
  exit 0
fi
