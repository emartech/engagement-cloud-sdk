#!/usr/bin/env bash
# Validation script for revoke_release.yml
set -euo pipefail

WORKFLOW=".github/workflows/revoke_release.yml"
ERRORS=0

fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

pass() {
  echo "PASS: $1"
}

# 1. Workflow only triggers on workflow_dispatch (never on push or schedule)
if grep -q '^\s*push:' "$WORKFLOW"; then
  fail "Revoke workflow must NOT trigger on push"
else
  pass "Workflow does not trigger on push"
fi

if grep -q '^\s*schedule:' "$WORKFLOW"; then
  fail "Revoke workflow must NOT trigger on schedule"
else
  pass "Workflow does not trigger on schedule"
fi

if grep -q 'workflow_dispatch:' "$WORKFLOW"; then
  pass "Workflow triggers on workflow_dispatch"
else
  fail "Workflow must trigger on workflow_dispatch"
fi

# 2. Version input is required
if grep -q "required: true" "$WORKFLOW"; then
  pass "Version input is required"
else
  fail "Version input must be required"
fi

# 3. Dry run defaults to true (safe by default)
if grep -A4 'dry_run:' "$WORKFLOW" | grep -q 'default: true'; then
  pass "Dry run defaults to true (safe by default)"
else
  fail "Dry run must default to true for safety"
fi

# 4. Semver validation exists
if grep -q '\^\\[0-9\\]' "$WORKFLOW" || grep -q 'Invalid version format' "$WORKFLOW"; then
  pass "Semver format validation exists"
else
  fail "Must validate version format (semver)"
fi

# 5. Concurrency group prevents parallel revocations
if grep -q 'concurrency:' "$WORKFLOW"; then
  pass "Concurrency group defined"
else
  fail "Must have concurrency group to prevent parallel revocations"
fi

# 6. Permissions are explicitly set
if grep -q '^permissions:' "$WORKFLOW"; then
  pass "Permissions block exists"
else
  fail "Missing explicit permissions block"
fi

# 7. Contents write permission (for release/tag deletion and Package.swift push)
if grep -q 'contents: write' "$WORKFLOW"; then
  pass "Contents write permission for release/tag deletion"
else
  fail "Must have contents: write permission"
fi

# 8. Packages write permission (for GitHub Packages deletion)
if grep -q 'packages: write' "$WORKFLOW"; then
  pass "Packages write permission for GitHub Packages deletion"
else
  fail "Must have packages: write permission"
fi

# 9. GitHub Release deletion job exists
if grep -q 'gh release delete' "$WORKFLOW"; then
  pass "GitHub Release deletion command exists"
else
  fail "Must include gh release delete command"
fi

# 10. Git tag deletion exists
if grep -q 'git push origin.*refs/tags' "$WORKFLOW" || grep -q 'cleanup-tag' "$WORKFLOW"; then
  pass "Git tag deletion exists"
else
  fail "Must include git tag deletion"
fi

# 11. GitHub Pages cleanup exists (gh-pages branch)
if grep -q 'gh-pages' "$WORKFLOW"; then
  pass "GitHub Pages cleanup references gh-pages branch"
else
  fail "Must clean up gh-pages branch for CDN revocation"
fi

# 12. NPM package version deletion exists
if grep -q 'packages/npm' "$WORKFLOW"; then
  pass "NPM package deletion via GitHub API exists"
else
  fail "Must delete NPM package version from GitHub Packages"
fi

# 13. Maven package version deletion exists
if grep -q 'packages/maven' "$WORKFLOW"; then
  pass "Maven package deletion via GitHub API exists"
else
  fail "Must delete Maven package versions from GitHub Packages"
fi

# 14. SPM Package.swift revert exists
if grep -q 'Package.swift' "$WORKFLOW"; then
  pass "SPM Package.swift revert logic exists"
else
  fail "Must revert Package.swift for SPM revocation"
fi

# 15. MavenCentral immutability reminder exists
if grep -qi 'immutable\|cannot be deleted\|cannot delete' "$WORKFLOW"; then
  pass "MavenCentral immutability reminder exists"
else
  fail "Must include MavenCentral immutability reminder"
fi

# 16. Previous version discovery for SPM fallback
if grep -q 'sort -V' "$WORKFLOW" || grep -q 'previous.*version' "$WORKFLOW"; then
  pass "Previous version discovery for SPM fallback exists"
else
  fail "Must discover previous version to revert SPM Package.swift"
fi

# 17. DRY_RUN checks present in destructive operations
DRY_RUN_CHECKS=$(grep -c 'DRY_RUN' "$WORKFLOW" || true)
if [ "$DRY_RUN_CHECKS" -ge 5 ]; then
  pass "Dry run guards present ($DRY_RUN_CHECKS references)"
else
  fail "Insufficient dry run guards (found $DRY_RUN_CHECKS, expected ≥5)"
fi

# 18. Summary job exists
if grep -q 'summary:' "$WORKFLOW"; then
  pass "Summary job exists"
else
  fail "Must include a summary job"
fi

# 19. actionlint validation
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
