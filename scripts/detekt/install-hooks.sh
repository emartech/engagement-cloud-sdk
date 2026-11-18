#!/usr/bin/env bash
set -euo pipefail

root=$(git rev-parse --show-toplevel)
cd "$root"
if [[ ! -x .githooks/pre-commit ]]; then
  echo >&2 'Repository pre-commit hook is missing or not executable.'
  exit 1
fi
current=$(git config --get core.hooksPath || true)
conflict=''
case "$current" in
  .githooks|"$root/.githooks") ;;
  '')
    directory=$(dirname "$(git rev-parse --git-path hooks/pre-commit)")
    for hook in "$directory"/*; do
      [[ -f "$hook" && -x "$hook" && "$hook" != *.sample ]] || continue
      conflict="existing executable hook: $hook"
      break
    done
    ;;
  *) conflict="existing core.hooksPath: $current" ;;
esac
if [[ -n "$conflict" && ${CONFIRM_REPLACE_HOOKS:-0} != 1 ]]; then
  echo >&2 "Refusing to replace $conflict"
  echo >&2 'Migrate it first, or explicitly confirm: CONFIRM_REPLACE_HOOKS=1 make install-git-hooks'
  exit 1
fi
git config --local core.hooksPath .githooks
echo 'Repository-local pre-commit hook installed (core.hooksPath=.githooks).'
