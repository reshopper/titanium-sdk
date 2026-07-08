Create GitHub Release

Purpose
- Create a new GitHub release on the fork with updated version, changelog, and release notes.

Usage
- Trigger this command and include the version number in your message, e.g. `13.3.0`.
- Optionally specify `--prerelease` for RC/beta releases.

Steps
1) Ensure we are at the repo root: `/Users/jonasfunk/Udvikling/titanium-sdk`.
2) Verify `gh` CLI is authenticated.
3) Extract version from user message (e.g. `13.3.0`).
4) Update version in `package.json` to the new version.
5) Run `npm install --package-lock-only` to update `package-lock.json`.
6) Find the last release commit by searching for "chore(release): bump version" in git log.
7) List all commits since the last release using `git log <last-release-commit>..HEAD --oneline --no-merges`.
8) Update `CHANGELOG.md` with new features (feat), bug fixes (fix), and community credits based on commits.
9) Commit changes with message: `chore(release): bump version to VERSION and update changelog`.
10) Push changes to origin/master.
11) Check if tag already exists - abort if it does.
12) Create release with CHANGELOG.md as release notes.
13) Upload SDK zip file from `dist/mobilesdk-VERSION-osx.zip` to the release.
14) Open the release URL in browser for review.

CHANGELOG Format
```markdown
# [VERSION](https://github.com/tidev/titanium_mobile/compare/PREV_VERSION...VERSION) (YYYY-MM-DD)

## About this release

Titanium SDK VERSION is a minor release of the SDK.

## New Features

### JavaScript
* **Feature**: Description ([commit](url))

### Android
* **Feature**: Description ([commit](url))

### iOS
* **Feature**: Description ([commit](url))

## Bug Fixes

### Android
* **Component**: Description ([commit](url))

### iOS
* **Component**: Description ([commit](url))

## Community Credits

* author
  * commit message ([hash](url))
```

Commit Categorization
- `feat(platform):` → New Features under platform section
- `fix(platform):` → Bug Fixes under platform section
- `feat(js):` → New Features under JavaScript section
- `chore:`, `docs:` → Usually not included in changelog
- Include commit hash links in Community Credits

Shell Commands Reference
```bash
# Find last release commit
git log --oneline --grep="chore(release): bump version" | head -1

# List commits since last release (replace COMMIT_HASH)
git log COMMIT_HASH..HEAD --oneline --no-merges

# Show commit details
git show COMMIT_HASH --stat -q
git log COMMIT_HASH -1 --format="%B"

# Commit and push changes
git add -A
git commit -m "chore(release): bump version to $VERSION and update changelog"
git push origin master

# Create release with changelog
gh release create "$VERSION" --title "$VERSION" --notes-file CHANGELOG.md --latest

# Upload SDK zip file to release
gh release upload "$VERSION" "dist/mobilesdk-$VERSION-osx.zip" --clobber
```

Pre-release variant
```bash
# For RC/beta releases, add --prerelease flag
gh release create "$VERSION" \
  --title "$VERSION" \
  --notes-file CHANGELOG.md \
  --prerelease
```

Notes
- Always update `package.json` and `package-lock.json` before creating release.
- Use `--notes-file CHANGELOG.md` to include the full changelog in release notes.
- Use `--prerelease` for RC, beta, or other non-stable releases.
- Use `--target <branch>` if releasing from a branch other than master.
- The SDK zip file should be built before running this command (located in `dist/mobilesdk-VERSION-osx.zip`).
- Use `--clobber` flag to overwrite existing assets if re-uploading.
- You can edit the release notes on GitHub after creation.
