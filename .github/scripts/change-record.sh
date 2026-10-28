#!/usr/bin/env bash
# The rules a pull request follows for recording its change (changes/README.md,
# CONTRIBUTING.md), checked so that a contributor learns them from the pull
# request itself rather than from a conflict.
#
#   change-record.sh BASE_REF HEAD_SHA HEAD_BRANCH
#
# BASE_REF is the branch merged into (origin/main), HEAD_SHA the pull request's
# own last commit, HEAD_BRANCH its branch name. Prints GitHub annotations;
# exits 1 when a rule is broken.
set -euo pipefail

base="$1"
head="$2"
branch="$3"

history=elasticore-project/elasticore-base/src/main/resources/elasticore-guide/elasticore-history.md
fork=$(git merge-base "$base" "$head")
changed=$(git diff --name-only "$fork" "$head")
added=$(git diff --name-only --diff-filter=A "$fork" "$head")
failed=0

# A release branch assembles the fragments into the history; nothing else edits it.
release=no
case "$branch" in version/*) release=yes ;; esac

if [ "$release" = no ] && printf '%s\n' "$changed" | grep -qxF "$history"; then
  echo "::error file=$history,title=Record the change in changes/, not in the history::This pull request edits elasticore-history.md. Every pull request used to edit it at the same lines, so they conflicted with each other. Write the entry as a new file changes/<topic>.md instead - same ###/#### form, no version heading (changes/README.md) - and leave the history as it is: the release moves the fragments into it."
  failed=1
fi

# A change to what the project ships says what it changed.
shipped=$(printf '%s\n' "$changed" | grep -E '^elasticore-project/[^/]+/src/main/' | grep -vxF "$history" || true)
fragments=$(printf '%s\n' "$added" | grep -E '^changes/[^/]+\.md$' | grep -vxF 'changes/README.md' || true)
if [ "$release" = no ] && [ -n "$shipped" ] && [ -z "$fragments" ]; then
  first=$(printf '%s\n' "$shipped" | head -1)
  echo "::error title=No change record::This pull request changes what ElastiCORE ships ($first and $(printf '%s\n' "$shipped" | wc -l | tr -d ' ') file(s) in all) but adds no changes/<topic>.md. Add one: why, what changed, how it was checked, the files (changes/README.md has the form)."
  failed=1
fi

# A copy of a guide in a sample's blueprint is written by the build; committed, it conflicts.
copies=$(printf '%s\n' "$added" | grep -E '^elasticore-project/[^/]+/src/main/resources/blueprint/elasticore-[^/]+\.md$' || true)
if [ -n "$copies" ]; then
  echo "::error title=Guide copies are not committed::$(printf '%s\n' "$copies" | head -1) is written by the build from elasticore-base/src/main/resources/elasticore-guide/ and is in .gitignore - remove it from the pull request (git rm --cached)."
  failed=1
fi

# Far behind main: a warning, not a failure - but the pull request is checked
# against a main that has moved on, and it is the likeliest to conflict.
behind=$(git rev-list --count "$head..$base")
if [ "$behind" -gt 10 ]; then
  echo "::warning title=Behind main::This branch is $behind commits behind main. Rebase it on the latest main (git fetch origin && git rebase origin/main) so it is checked against what it will be merged into."
fi

if [ "$failed" = 0 ]; then
  echo "change record: ok (fragments: ${fragments:-none needed}; behind main: $behind)"
fi
exit "$failed"
