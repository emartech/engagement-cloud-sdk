#!/usr/bin/env bash
# Validation script for SDK-954: CHANGELOG.md and changelog-based GitHub Release notes
# Encodes all acceptance criteria for changelog creation and workflow integration.
#
# Usage: bash .github/workflows/tests/validate_changelog.sh
# Exit code: 0 if all checks pass, 1 if any check fails.

set -euo pipefail

CHANGELOG_FILE="CHANGELOG.md"
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

echo "=== SDK-954 Validation: CHANGELOG.md and Release Notes ==="
echo ""

# --- CHANGELOG.md existence and format ---
echo "[CHANGELOG.md Existence]"

# 1. CHANGELOG.md must exist
if [ -f "$CHANGELOG_FILE" ]; then
  check "CHANGELOG.md exists" "pass"
else
  check "CHANGELOG.md exists" "fail"
fi

echo ""
echo "[Keep a Changelog Format]"

# 2. Must have top-level Changelog heading
if grep -q '^# Changelog' "$CHANGELOG_FILE" 2>/dev/null; then
  check "Has top-level '# Changelog' heading" "pass"
else
  check "Has top-level '# Changelog' heading" "fail"
fi

# 3. Must reference Keep a Changelog
if grep -q 'keepachangelog.com' "$CHANGELOG_FILE" 2>/dev/null; then
  check "References Keep a Changelog format" "pass"
else
  check "References Keep a Changelog format" "fail"
fi

# 4. Must reference Semantic Versioning
if grep -q 'semver.org' "$CHANGELOG_FILE" 2>/dev/null; then
  check "References Semantic Versioning" "pass"
else
  check "References Semantic Versioning" "fail"
fi

# 5. Must have [Unreleased] section
if grep -q '## \[Unreleased\]' "$CHANGELOG_FILE" 2>/dev/null; then
  check "Has [Unreleased] section" "pass"
else
  check "Has [Unreleased] section" "fail"
fi

# 6. Must have at least one change category (Changed, Added, Breaking, etc.)
if grep -qE '^### (Added|Changed|Deprecated|Removed|Fixed|Security|Breaking)' "$CHANGELOG_FILE" 2>/dev/null; then
  check "Has at least one change category" "pass"
else
  check "Has at least one change category" "fail"
fi

echo ""
echo "[Changelog Extraction Logic]"

# 7. Extraction script returns empty for non-existent version
if [ -f "$CHANGELOG_FILE" ]; then
  NOTES=$(sed -n "/^## \[99\.99\.99\]/,/^## \[/{/^## \[/!p;}" "$CHANGELOG_FILE" | sed '/^$/N;/^\n$/d')
  if [ -z "$NOTES" ]; then
    check "Extraction returns empty for non-existent version (99.99.99)" "pass"
  else
    check "Extraction returns empty for non-existent version (99.99.99)" "fail"
  fi
else
  check "Extraction returns empty for non-existent version (99.99.99)" "fail"
fi

# 8. [Unreleased] section is NOT extracted by version extraction
if [ -f "$CHANGELOG_FILE" ]; then
  # When we search for a specific version like "1.0.0", [Unreleased] must NOT match
  SPECIFIC_NOTES=$(sed -n "/^## \[1\.0\.0\]/,/^## \[/{/^## \[/!p;}" "$CHANGELOG_FILE" | sed '/^$/N;/^\n$/d')
  if [ -z "$SPECIFIC_NOTES" ]; then
    check "[Unreleased] section is not extracted for specific version search" "pass"
  else
    check "[Unreleased] section is not extracted for specific version search" "fail"
  fi
else
  check "[Unreleased] section is not extracted for specific version search" "fail"
fi

echo ""
echo "[Workflow Integration: Changelog Extraction Step]"

# 9. publish_artifacts.yml has a changelog extraction step
if grep -q 'Extract release notes from CHANGELOG' "$PUBLISH_FILE" 2>/dev/null; then
  check "Publish workflow has changelog extraction step" "pass"
else
  check "Publish workflow has changelog extraction step" "fail"
fi

# 10. Extraction step writes found=true/false to GITHUB_OUTPUT
if grep -q 'found=true' "$PUBLISH_FILE" 2>/dev/null && grep -q 'found=false' "$PUBLISH_FILE" 2>/dev/null; then
  check "Extraction step outputs found=true/false" "pass"
else
  check "Extraction step outputs found=true/false" "fail"
fi

# 11. Extraction step uses sed to extract version section
if grep -q "sed -n" "$PUBLISH_FILE" 2>/dev/null && grep -q 'CHANGELOG.md' "$PUBLISH_FILE" 2>/dev/null; then
  check "Extraction step uses sed to parse CHANGELOG.md" "pass"
else
  check "Extraction step uses sed to parse CHANGELOG.md" "fail"
fi

# 12. Extraction step writes notes to /tmp/release-notes.md
if grep -q '/tmp/release-notes.md' "$PUBLISH_FILE" 2>/dev/null; then
  check "Extraction writes notes to /tmp/release-notes.md" "pass"
else
  check "Extraction writes notes to /tmp/release-notes.md" "fail"
fi

echo ""
echo "[Workflow Integration: Conditional Release Creation]"

# 13. Release creation uses --notes-file when changelog entry found
if grep -q '\-\-notes-file' "$PUBLISH_FILE" 2>/dev/null; then
  check "Release creation uses --notes-file for extracted notes" "pass"
else
  check "Release creation uses --notes-file for extracted notes" "fail"
fi

# 14. Release creation falls back to --generate-notes
if grep -q '\-\-generate-notes' "$PUBLISH_FILE" 2>/dev/null; then
  check "Release creation falls back to --generate-notes" "pass"
else
  check "Release creation falls back to --generate-notes" "fail"
fi

# 15. Release creation conditionally checks changelog.outputs.found
if grep -q 'steps.changelog.outputs.found' "$PUBLISH_FILE" 2>/dev/null; then
  check "Release creation checks changelog.outputs.found" "pass"
else
  check "Release creation checks changelog.outputs.found" "fail"
fi

# 16. No || true anywhere in the create-release job (failures should not be masked)
# The || true may be on a continuation line, so check the entire create-release job block
CREATE_RELEASE_BLOCK=$(sed -n '/^  create-release:/,/^  [a-z].*:$/p' "$PUBLISH_FILE" 2>/dev/null)
if echo "$CREATE_RELEASE_BLOCK" | grep -q '|| true'; then
  check "No || true masking release creation failures" "fail"
else
  check "No || true masking release creation failures" "pass"
fi

echo ""
echo "[CHANGELOG.md Missing Fallback]"

# 17. Extraction step handles missing CHANGELOG.md
if grep -q 'No CHANGELOG.md found' "$PUBLISH_FILE" 2>/dev/null; then
  check "Extraction handles missing CHANGELOG.md gracefully" "pass"
else
  check "Extraction handles missing CHANGELOG.md gracefully" "fail"
fi

echo ""
echo "[actionlint Validation]"

# 18. actionlint passes on publish_artifacts.yml
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
