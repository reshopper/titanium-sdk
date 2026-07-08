Update fork from upstream release (GA)

Purpose
- Sync this fork with the latest upstream GA release branch of `tidev/titanium-sdk` (e.g. `13_0_0_GA`).

Usage
- Trigger this command and optionally include a release URL in the message, e.g. `@https://github.com/tidev/titanium-sdk/tree/13_0_0_GA`.
- If a URL is provided, use that branch name. Otherwise, detect the newest upstream GA branch matching `^\d+_\d+_\d+_GA$`.

Steps
1) Ensure we are at the repo root: `/Users/jonasfunk/Udvikling/titanium-sdk`.
2) Verify `upstream` remote points to `https://github.com/tidev/titanium-sdk.git`. If missing, add it.
3) Fetch upstream branches and tags.
4) Determine `releaseBranch`:
   - If a release URL was supplied, extract the branch name from `/tree/<branch>`.
   - Else, list `upstream/*_GA` branches and pick the highest version by numeric sort.
5) Ensure the base branch is `master` (or `main` if used). Start from `master` for this repo.
6) Create a new branch: `chore/sync-upstream-<releaseBranch>` from `master`.
7) Merge: `upstream/<releaseBranch>` into the new branch with a no-ff merge commit.
8) If conflicts occur, stop and request manual resolution in-editor.
9) Optional quick validation: run `npm ci` and `npm run lint:docs`.
10) Push the sync branch to origin.

Shell (non-interactive)
```bash
# repo root
cd /Users/jonasfunk/Udvikling/titanium-sdk

# ensure upstream
git remote get-url upstream >/dev/null 2>&1 || git remote add upstream https://github.com/tidev/titanium-sdk.git
git fetch upstream --tags --prune

# derive release branch
if [ -n "$RELEASE_URL" ]; then
  RELEASE_BRANCH=$(echo "$RELEASE_URL" | sed -n 's#.*/tree/\([^/?#]*\).*#\1#p')
else
  RELEASE_BRANCH=$(git branch -r | sed -n 's# *upstream/\(.*_GA\)$#\1#p' | \
    awk -F'_' '{ printf("%03d_%03d_%03d_GA %s\n", $1,$2,$3,$0) }' | \
    sort -r | head -n1 | awk '{print $2}')
fi

test -n "$RELEASE_BRANCH" || { echo "No GA release branch found"; exit 1; }
echo "Using upstream release branch: $RELEASE_BRANCH"

# ensure base
git checkout master
git pull --ff-only origin master || true

# create sync branch
SYNC_BRANCH="chore/sync-upstream-$RELEASE_BRANCH"
git checkout -b "$SYNC_BRANCH"

# merge upstream release
git merge --no-ff "upstream/$RELEASE_BRANCH" -m "chore: sync upstream $RELEASE_BRANCH"

# optional quick checks
npm ci
npm run lint:docs || true

# push
git push -u origin "$SYNC_BRANCH"

echo "Created and pushed $SYNC_BRANCH. Open a PR to merge into master."
```

Notes
- Keep merges clean; prefer resolving conflicts in-editor.
- If your local default branch is `main`, substitute `master` with `main` above.
