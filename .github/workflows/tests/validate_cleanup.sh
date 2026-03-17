#!/usr/bin/env bash
# Validation script for SDK-958: Remove superseded workflows and unify CI configuration
# Tests that legacy workflows are deleted, env blocks are trimmed, concurrency is added,
# secrets are scoped to jobs, and untouched workflows remain unchanged.
# Exit 0 = all checks pass, Exit 1 = one or more checks failed.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
WORKFLOW_DIR="$REPO_ROOT/.github/workflows"

PASSED=0
FAILED=0
SKIPPED=0

check() {
  local description="$1"
  local result="$2"
  if [ "$result" = "pass" ]; then
    echo "  PASS: $description"
    PASSED=$((PASSED + 1))
  else
    echo "  FAIL: $description"
    FAILED=$((FAILED + 1))
  fi
}

echo "=== SDK-958 Cleanup Validation ==="
echo ""

# ---------------------------------------------------------------------------
# Test 1: ios_release.yml does not exist
# ---------------------------------------------------------------------------
if [ -f "$WORKFLOW_DIR/ios_release.yml" ]; then
  check "ios_release.yml deleted" "fail"
else
  check "ios_release.yml deleted" "pass"
fi

# ---------------------------------------------------------------------------
# Test 2: js_release.yml does not exist
# ---------------------------------------------------------------------------
if [ -f "$WORKFLOW_DIR/js_release.yml" ]; then
  check "js_release.yml deleted" "fail"
else
  check "js_release.yml deleted" "pass"
fi

# ---------------------------------------------------------------------------
# Test 3: No BLACKDUCK_* vars in any remaining workflow
# ---------------------------------------------------------------------------
BLACKDUCK_HITS=$(grep -rl 'BLACKDUCK_' "$WORKFLOW_DIR"/*.yml 2>/dev/null || true)
if [ -z "$BLACKDUCK_HITS" ]; then
  check "No BLACKDUCK_* vars in remaining workflows" "pass"
else
  echo "    Found BLACKDUCK_* in: $BLACKDUCK_HITS"
  check "No BLACKDUCK_* vars in remaining workflows" "fail"
fi

# ---------------------------------------------------------------------------
# Test 4: No DETECT_* vars in any remaining workflow
# ---------------------------------------------------------------------------
DETECT_HITS=$(grep -rl 'DETECT_' "$WORKFLOW_DIR"/*.yml 2>/dev/null || true)
if [ -z "$DETECT_HITS" ]; then
  check "No DETECT_* vars in remaining workflows" "pass"
else
  echo "    Found DETECT_* in: $DETECT_HITS"
  check "No DETECT_* vars in remaining workflows" "fail"
fi

# ---------------------------------------------------------------------------
# Test 5: publish_artifacts.yml has concurrency block
# ---------------------------------------------------------------------------
PUBLISH_FILE="$WORKFLOW_DIR/publish_artifacts.yml"
if [ -f "$PUBLISH_FILE" ]; then
  if grep -q '^concurrency:' "$PUBLISH_FILE"; then
    check "publish_artifacts.yml has concurrency block" "pass"
  else
    check "publish_artifacts.yml has concurrency block" "fail"
  fi
else
  check "publish_artifacts.yml has concurrency block (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 6: Sonatype vars NOT at workflow level in publish_artifacts.yml
# Workflow-level env is the block directly under the top-level 'env:' key,
# before the 'jobs:' key. We extract that block and check for OSSRH/SONATYPE.
# ---------------------------------------------------------------------------
if [ -f "$PUBLISH_FILE" ]; then
  # Extract lines between top-level env: and either jobs: or permissions:
  WORKFLOW_ENV=$(sed -n '/^env:/,/^[a-z]/{ /^env:/d; /^[a-z]/d; p; }' "$PUBLISH_FILE")
  SONATYPE_AT_WORKFLOW=$(echo "$WORKFLOW_ENV" | grep -cE '(OSSRH_|SONATYPE_)' || true)
  if [ "$SONATYPE_AT_WORKFLOW" -eq 0 ]; then
    check "Sonatype vars NOT at workflow level in publish_artifacts.yml" "pass"
  else
    echo "    Found $SONATYPE_AT_WORKFLOW Sonatype var(s) at workflow level"
    check "Sonatype vars NOT at workflow level in publish_artifacts.yml" "fail"
  fi
else
  check "Sonatype vars NOT at workflow level (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 7: Sonatype vars ARE in android-pipeline job in publish_artifacts.yml
# ---------------------------------------------------------------------------
if [ -f "$PUBLISH_FILE" ]; then
  # Extract the android-pipeline job block (from 'android-pipeline:' to next top-level job)
  ANDROID_JOB=$(sed -n '/^  android-pipeline:/,/^  [a-z].*:$/p' "$PUBLISH_FILE")
  OSSRH_IN_JOB=$(echo "$ANDROID_JOB" | grep -c 'OSSRH_USERNAME' || true)
  SONATYPE_IN_JOB=$(echo "$ANDROID_JOB" | grep -c 'SONATYPE_STAGING_PROFILE_ID' || true)
  if [ "$OSSRH_IN_JOB" -ge 1 ] && [ "$SONATYPE_IN_JOB" -ge 1 ]; then
    check "Sonatype vars in android-pipeline job" "pass"
  else
    echo "    OSSRH count in job: $OSSRH_IN_JOB, SONATYPE count: $SONATYPE_IN_JOB"
    check "Sonatype vars in android-pipeline job" "fail"
  fi
else
  check "Sonatype vars in android-pipeline job (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 8: codeql.yml has fewer than 10 env vars (trimmed)
# ---------------------------------------------------------------------------
CODEQL_FILE="$WORKFLOW_DIR/codeql.yml"
if [ -f "$CODEQL_FILE" ]; then
  # Count lines in the workflow-level env block that look like VAR_NAME: value
  CODEQL_ENV=$(sed -n '/^env:/,/^[a-z]/{ /^env:/d; /^[a-z]/d; p; }' "$CODEQL_FILE")
  ENV_COUNT=$(echo "$CODEQL_ENV" | grep -cE '^\s+[A-Z_]+:' || true)
  if [ "$ENV_COUNT" -lt 10 ]; then
    check "codeql.yml has fewer than 10 env vars (found $ENV_COUNT)" "pass"
  else
    echo "    Found $ENV_COUNT env vars (expected < 10)"
    check "codeql.yml has fewer than 10 env vars (found $ENV_COUNT)" "fail"
  fi
else
  check "codeql.yml has fewer than 10 env vars (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 9: auto_merge_dependabot_pr.yml is unchanged (check git diff)
# ---------------------------------------------------------------------------
DEPENDABOT_FILE="$WORKFLOW_DIR/auto_merge_dependabot_pr.yml"
if [ -f "$DEPENDABOT_FILE" ]; then
  DIFF_LINES=$(cd "$REPO_ROOT" && git diff origin/main -- .github/workflows/auto_merge_dependabot_pr.yml | wc -l | tr -d ' ')
  if [ "$DIFF_LINES" -eq 0 ]; then
    check "auto_merge_dependabot_pr.yml unchanged from main" "pass"
  else
    echo "    Found $DIFF_LINES lines of diff against origin/main"
    check "auto_merge_dependabot_pr.yml unchanged from main" "fail"
  fi
else
  check "auto_merge_dependabot_pr.yml unchanged (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 10: actionlint passes on all remaining workflows
# ---------------------------------------------------------------------------
if command -v actionlint >/dev/null 2>&1; then
  LINT_FAILED=0
  for wf in "$WORKFLOW_DIR"/*.yml; do
    wf_name=$(basename "$wf")
    # Skip auto_merge_dependabot_pr.yml -- has pre-existing shellcheck info
    # and is explicitly out of scope for this cleanup
    if [ "$wf_name" = "auto_merge_dependabot_pr.yml" ]; then
      continue
    fi
    if ! actionlint "$wf" >/dev/null 2>&1; then
      echo "    actionlint failed on: $wf_name"
      LINT_FAILED=1
    fi
  done
  if [ "$LINT_FAILED" -eq 0 ]; then
    check "actionlint passes on all remaining workflows" "pass"
  else
    check "actionlint passes on all remaining workflows" "fail"
  fi
else
  echo "  SKIP: actionlint not installed"
  SKIPPED=$((SKIPPED + 1))
fi

# ---------------------------------------------------------------------------
# Test 11: on_push_workflow.yml has no OSSRH/SONATYPE/BLACKDUCK/DETECT vars
# ---------------------------------------------------------------------------
ON_PUSH_FILE="$WORKFLOW_DIR/on_push_workflow.yml"
if [ -f "$ON_PUSH_FILE" ]; then
  STALE_VARS=$(grep -cE '^\s+(OSSRH_|SONATYPE_|BLACKDUCK_|DETECT_)' "$ON_PUSH_FILE" || true)
  if [ "$STALE_VARS" -eq 0 ]; then
    check "on_push_workflow.yml has no publishing/scanning vars" "pass"
  else
    echo "    Found $STALE_VARS stale var declarations"
    check "on_push_workflow.yml has no publishing/scanning vars" "fail"
  fi
else
  check "on_push_workflow.yml var cleanup (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 12: nightly_workflow.yml has no OSSRH/SONATYPE/BLACKDUCK/DETECT vars
# ---------------------------------------------------------------------------
NIGHTLY_FILE="$WORKFLOW_DIR/nightly_workflow.yml"
if [ -f "$NIGHTLY_FILE" ]; then
  STALE_NIGHTLY=$(grep -cE '^\s+(OSSRH_|SONATYPE_|BLACKDUCK_|DETECT_)' "$NIGHTLY_FILE" || true)
  if [ "$STALE_NIGHTLY" -eq 0 ]; then
    check "nightly_workflow.yml has no publishing/scanning vars" "pass"
  else
    echo "    Found $STALE_NIGHTLY stale var declarations"
    check "nightly_workflow.yml has no publishing/scanning vars" "fail"
  fi
else
  check "nightly_workflow.yml var cleanup (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 13: publish_artifacts.yml has ANDROID_RELEASE_* in android-pipeline job
# ---------------------------------------------------------------------------
if [ -f "$PUBLISH_FILE" ]; then
  ANDROID_JOB_BLOCK=$(sed -n '/^  android-pipeline:/,/^  [a-z].*:$/p' "$PUBLISH_FILE")
  ANDROID_RELEASE_IN_JOB=$(echo "$ANDROID_JOB_BLOCK" | grep -c 'ANDROID_RELEASE_KEY_PASSWORD' || true)
  if [ "$ANDROID_RELEASE_IN_JOB" -ge 1 ]; then
    check "ANDROID_RELEASE_* vars in android-pipeline job" "pass"
  else
    check "ANDROID_RELEASE_* vars in android-pipeline job" "fail"
  fi
else
  check "ANDROID_RELEASE_* in android-pipeline job (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 14: publish_artifacts.yml has no ANDROID_RELEASE_* at workflow level
# ---------------------------------------------------------------------------
if [ -f "$PUBLISH_FILE" ]; then
  ANDROID_AT_WORKFLOW=$(echo "$WORKFLOW_ENV" | grep -cE 'ANDROID_RELEASE_' || true)
  if [ "$ANDROID_AT_WORKFLOW" -eq 0 ]; then
    check "ANDROID_RELEASE_* NOT at workflow level in publish_artifacts.yml" "pass"
  else
    echo "    Found $ANDROID_AT_WORKFLOW ANDROID_RELEASE_* var(s) at workflow level"
    check "ANDROID_RELEASE_* NOT at workflow level in publish_artifacts.yml" "fail"
  fi
else
  check "ANDROID_RELEASE_* NOT at workflow level (file missing)" "fail"
fi

# ---------------------------------------------------------------------------
# Test 15: publish_artifacts.yml concurrency has cancel-in-progress: false
# ---------------------------------------------------------------------------
if [ -f "$PUBLISH_FILE" ]; then
  if grep -A2 '^concurrency:' "$PUBLISH_FILE" | grep -q 'cancel-in-progress: false'; then
    check "publish_artifacts.yml concurrency cancel-in-progress is false" "pass"
  else
    check "publish_artifacts.yml concurrency cancel-in-progress is false" "fail"
  fi
else
  check "publish_artifacts.yml concurrency (file missing)" "fail"
fi

echo ""
echo "=== Results: $PASSED passed, $FAILED failed, $SKIPPED skipped ==="

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
exit 0
