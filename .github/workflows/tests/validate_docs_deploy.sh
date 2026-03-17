#!/usr/bin/env bash
# Validation script for SDK-956: docs submodule and GitHub Pages deployment
# Encodes all acceptance criteria for documentation deployment alongside CDN artifacts.
#
# Usage: bash .github/workflows/tests/validate_docs_deploy.sh
# Exit code: 0 if all checks pass, 1 if any check fails.

set -euo pipefail

PUBLISH_FILE=".github/workflows/publish_artifacts.yml"
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

echo "=== SDK-956 Validation: Docs Submodule and GitHub Pages Deployment ==="
echo ""

# --- Submodule checks ---
echo "[Git Submodule Configuration]"

# 1. .gitmodules must exist
if [ -f ".gitmodules" ]; then
  check ".gitmodules file exists" "pass"
else
  check ".gitmodules file exists" "fail"
fi

# 2. .gitmodules must reference the correct docs repo URL
if grep -q 'https://github.com/emartech/engagement-cloud-sdk-docs' ".gitmodules" 2>/dev/null; then
  check ".gitmodules references correct docs repo URL" "pass"
else
  check ".gitmodules references correct docs repo URL" "fail"
fi

# 3. .gitmodules must reference the correct submodule path
if grep -q 'docs/engagement-cloud-sdk-docs' ".gitmodules" 2>/dev/null; then
  check ".gitmodules references correct submodule path" "pass"
else
  check ".gitmodules references correct submodule path" "fail"
fi

echo ""
echo "[JS Pipeline: No Direct GitHub Pages Deploy]"

# 4. peaceiris/actions-gh-pages must NOT appear in js-pipeline section
# Extract the js-pipeline job block (from "js-pipeline:" to the next top-level job)
JS_PIPELINE_BLOCK=$(sed -n '/^  js-pipeline:/,/^  [a-z][a-z_-]*:$/p' "$PUBLISH_FILE")
if echo "$JS_PIPELINE_BLOCK" | grep -q 'peaceiris/actions-gh-pages'; then
  check "peaceiris/actions-gh-pages NOT in js-pipeline job" "fail"
else
  check "peaceiris/actions-gh-pages NOT in js-pipeline job" "pass"
fi

# 5. JS pipeline must upload CDN artifacts for deploy-pages consumption
if echo "$JS_PIPELINE_BLOCK" | grep -q 'js-cdn-artifacts'; then
  check "JS pipeline uploads CDN artifacts as js-cdn-artifacts" "pass"
else
  check "JS pipeline uploads CDN artifacts as js-cdn-artifacts" "fail"
fi

echo ""
echo "[Deploy-Pages Job Structure]"

# 6. deploy-pages job must exist
if grep -q 'deploy-pages:' "$PUBLISH_FILE"; then
  check "deploy-pages job exists" "pass"
else
  check "deploy-pages job exists" "fail"
fi

# Extract the deploy-pages job block
DEPLOY_PAGES_BLOCK=$(sed -n '/^  deploy-pages:/,/^  [a-z][a-z_-]*:$/p' "$PUBLISH_FILE")

# 7. deploy-pages must use submodules: recursive in checkout
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'submodules: recursive'; then
  check "deploy-pages uses submodules: recursive in checkout" "pass"
else
  check "deploy-pages uses submodules: recursive in checkout" "fail"
fi

# 8. deploy-pages must depend on js-pipeline
if echo "$DEPLOY_PAGES_BLOCK" | grep 'needs:' | grep -q 'js-pipeline'; then
  check "deploy-pages depends on js-pipeline" "pass"
else
  check "deploy-pages depends on js-pipeline" "fail"
fi

# 9. deploy-pages must depend on derive-version
if echo "$DEPLOY_PAGES_BLOCK" | grep 'needs:' | grep -q 'derive-version'; then
  check "deploy-pages depends on derive-version" "pass"
else
  check "deploy-pages depends on derive-version" "fail"
fi

# 10. deploy-pages must have peaceiris/actions-gh-pages (the single deployment point)
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'peaceiris/actions-gh-pages'; then
  check "deploy-pages uses peaceiris/actions-gh-pages for deployment" "pass"
else
  check "deploy-pages uses peaceiris/actions-gh-pages for deployment" "fail"
fi

# 11. deploy-pages must download js-cdn-artifacts
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'js-cdn-artifacts'; then
  check "deploy-pages downloads js-cdn-artifacts" "pass"
else
  check "deploy-pages downloads js-cdn-artifacts" "fail"
fi

echo ""
echo "[Docusaurus Build Configuration]"

# 12. deploy-pages must have sed command for baseUrl override
if echo "$DEPLOY_PAGES_BLOCK" | grep -q "baseUrl: '/engagement-cloud-sdk/docs/'"; then
  check "deploy-pages overrides Docusaurus baseUrl via sed" "pass"
else
  check "deploy-pages overrides Docusaurus baseUrl via sed" "fail"
fi

# 13. deploy-pages must run npm ci for Docusaurus dependencies
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'npm ci'; then
  check "deploy-pages runs npm ci for Docusaurus" "pass"
else
  check "deploy-pages runs npm ci for Docusaurus" "fail"
fi

# 14. deploy-pages must build Docusaurus with --out-dir
if echo "$DEPLOY_PAGES_BLOCK" | grep -q '\-\-out-dir'; then
  check "deploy-pages builds Docusaurus with --out-dir" "pass"
else
  check "deploy-pages builds Docusaurus with --out-dir" "fail"
fi

echo ""
echo "[Empty-Directory Guards]"

# 15. CDN artifacts empty-directory guard
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'CDN artifacts directory is empty'; then
  check "CDN artifacts empty-directory guard exists" "pass"
else
  check "CDN artifacts empty-directory guard exists" "fail"
fi

# 16. Docs build output empty-directory guard
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'Docusaurus build output is empty'; then
  check "Docs build output empty-directory guard exists" "pass"
else
  check "Docs build output empty-directory guard exists" "fail"
fi

echo ""
echo "[DRY_RUN Configuration]"

# 17. deploy-pages Deploy step uses workflow-level DRY_RUN (no step-level re-declaration)
if echo "$DEPLOY_PAGES_BLOCK" | grep -q "if: env.DRY_RUN != 'true'"; then
  check "deploy-pages Deploy step uses DRY_RUN conditional" "pass"
else
  check "deploy-pages Deploy step uses DRY_RUN conditional" "fail"
fi

# 18. deploy-pages Deploy step must NOT have a step-level DRY_RUN env re-declaration
# Check for env: block right after/within the peaceiris step that redeclares DRY_RUN
PEACEIRIS_BLOCK=$(sed -n '/Deploy to GitHub Pages/,/^      - name:\|^  [a-z]/p' "$PUBLISH_FILE" | tail -n +1)
# Look specifically in the deploy-pages section for env: DRY_RUN after peaceiris
DEPLOY_PAGES_FULL=$(sed -n '/^  deploy-pages:/,/^  [a-z][a-z_-]*:$/p' "$PUBLISH_FILE")
PEACEIRIS_IN_DEPLOY=$(echo "$DEPLOY_PAGES_FULL" | sed -n '/Deploy to GitHub Pages/,/^      - name:\|^  [a-z]/p')
if echo "$PEACEIRIS_IN_DEPLOY" | grep -q 'DRY_RUN:'; then
  check "deploy-pages Deploy step does NOT re-declare DRY_RUN env (dead config)" "fail"
else
  check "deploy-pages Deploy step does NOT re-declare DRY_RUN env (dead config)" "pass"
fi

echo ""
echo "[Report-Slack Dependencies]"

# 19. report-slack must depend on deploy-pages
if grep -A3 'report-slack:' "$PUBLISH_FILE" | grep -q 'deploy-pages'; then
  check "report-slack depends on deploy-pages" "pass"
else
  check "report-slack depends on deploy-pages" "fail"
fi

echo ""
echo "[Single Deployment Point]"

# 20. peaceiris/actions-gh-pages must appear exactly once in the entire workflow
PEACEIRIS_COUNT=$(grep -c 'peaceiris/actions-gh-pages' "$PUBLISH_FILE" || true)
if [ "$PEACEIRIS_COUNT" -eq 1 ]; then
  check "peaceiris/actions-gh-pages appears exactly once (single deployment point)" "pass"
else
  check "peaceiris/actions-gh-pages appears exactly once (got $PEACEIRIS_COUNT)" "fail"
fi

echo ""
echo "[Submodule Update]"

# 21. deploy-pages must update submodule to latest
if echo "$DEPLOY_PAGES_BLOCK" | grep -q 'submodule update --remote'; then
  check "deploy-pages updates submodule to latest commit" "pass"
else
  check "deploy-pages updates submodule to latest commit" "fail"
fi

echo ""
echo "[actionlint Validation]"

# 22. actionlint passes on publish_artifacts.yml
if command -v actionlint &>/dev/null; then
  if actionlint "$PUBLISH_FILE" 2>&1; then
    check "actionlint passes on publish_artifacts.yml" "pass"
  else
    check "actionlint passes on publish_artifacts.yml" "fail"
  fi
else
  echo "  SKIP: actionlint not installed"
fi

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
else
  echo "All checks passed!"
  exit 0
fi
