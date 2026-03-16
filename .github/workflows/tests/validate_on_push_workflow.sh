#!/usr/bin/env bash
# Validation script for on_push_workflow.yml
# Tests that the CI workflow has correct trigger configuration, filename, and concurrency.
# Exit 0 = all checks pass, Exit 1 = one or more checks failed.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
WORKFLOW_DIR="$REPO_ROOT/.github/workflows"
WORKFLOW_FILE="$WORKFLOW_DIR/on_push_workflow.yml"
OLD_WORKFLOW_FILE="$WORKFLOW_DIR/on_push_worklow.yml"

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

echo "=== On-Push Workflow Validation ==="
echo ""

# Test 1: Filename typo is fixed (new file exists)
if [ -f "$WORKFLOW_FILE" ]; then
  check "Workflow file renamed to on_push_workflow.yml" "pass"
else
  check "Workflow file renamed to on_push_workflow.yml" "fail"
fi

# Test 2: Old filename with typo no longer exists
if [ -f "$OLD_WORKFLOW_FILE" ]; then
  check "Old file on_push_worklow.yml removed" "fail"
else
  check "Old file on_push_worklow.yml removed" "pass"
fi

# Tests 3-7 require the new file to exist
if [ ! -f "$WORKFLOW_FILE" ]; then
  echo ""
  echo "  Skipping content checks -- workflow file not found at expected path."
  echo ""
  echo "=== Results: $PASSED passed, $FAILED failed, $SKIPPED skipped ==="
  exit 1
fi

# Test 3: Push trigger restricts to main branch only
# Anchor to the on: trigger block by matching 'push:' preceded by 2-space indent (top-level trigger)
if grep -A1 '^  push:' "$WORKFLOW_FILE" | grep -q 'branches:.*\[main\]'; then
  check "Push trigger restricted to branches: [main]" "pass"
else
  check "Push trigger restricted to branches: [main]" "fail"
fi

# Test 4: No branches-ignore pattern (the old overly-broad trigger)
if grep -q 'branches-ignore' "$WORKFLOW_FILE"; then
  check "No branches-ignore pattern present" "fail"
else
  check "No branches-ignore pattern present" "pass"
fi

# Test 5: pull_request trigger exists
if grep -q 'pull_request:' "$WORKFLOW_FILE"; then
  check "pull_request trigger present" "pass"
else
  check "pull_request trigger present" "fail"
fi

# Test 6: workflow_dispatch trigger exists
if grep -q 'workflow_dispatch:' "$WORKFLOW_FILE"; then
  check "workflow_dispatch trigger present" "pass"
else
  check "workflow_dispatch trigger present" "fail"
fi

# Test 7: Concurrency group exists
if grep -q 'concurrency:' "$WORKFLOW_FILE"; then
  check "Concurrency group present" "pass"
else
  check "Concurrency group present" "fail"
fi

# Test 8: cancel-in-progress only for PRs (not unconditional true)
if grep -qE "cancel-in-progress:.*github\.event_name\s*==\s*['\"]pull_request['\"]" "$WORKFLOW_FILE"; then
  check "cancel-in-progress conditional on pull_request event" "pass"
elif grep -q 'cancel-in-progress: true' "$WORKFLOW_FILE"; then
  check "cancel-in-progress conditional on pull_request event" "fail"
else
  check "cancel-in-progress conditional on pull_request event" "fail"
fi

# Test 9: actionlint passes on the workflow file
if command -v actionlint >/dev/null 2>&1; then
  if actionlint "$WORKFLOW_FILE" 2>&1; then
    check "actionlint validation passes" "pass"
  else
    check "actionlint validation passes" "fail"
  fi
else
  echo "  SKIP: actionlint not installed"
  SKIPPED=$((SKIPPED + 1))
fi

# Test 10: All existing job names unchanged (build, test, lint, reporting)
for job in "build:" "test:" "lint:" "reporting:"; do
  if grep -q "^  $job" "$WORKFLOW_FILE"; then
    check "Job '$job' still present" "pass"
  else
    check "Job '$job' still present" "fail"
  fi
done

# Test 11: Trigger automerge step preserved
if grep -q 'Trigger automerge' "$WORKFLOW_FILE"; then
  check "Trigger automerge step preserved" "pass"
else
  check "Trigger automerge step preserved" "fail"
fi

echo ""
echo "=== Results: $PASSED passed, $FAILED failed, $SKIPPED skipped ==="

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
exit 0
