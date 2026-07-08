---
description: Post-Change Validation Rules
globs: **/*.js, **/*.java, **/*.objc, **/*.swift
alwaysApply: false
---
# Post-Change Validation Rules
# Post-Change Validation Rules
# Post-Change Validation Rules

## Overview
After making any changes to files in the Titanium SDK codebase, you MUST run the appropriate validation checks to ensure code quality and consistency before considering the work complete.

## Required Checks by File Type

### JavaScript Files (*.js)
Run ESLint on all modified JavaScript files:
```bash
npx eslint path/to/modified/file.js --fix
```

### Java Files (android/**/*.java)
Run checkstyle validation on all modified Java files:
```bash
node ./build/scons gradlew checkJavaStyle --args --console plain -PchangedFiles='comma,separated,list,of,changed,java,files'
```

### Objective-C/Header Files (iphone/**/*.{m,h})
Run clang-format on all modified Objective-C and header files:
```bash
npx clang-format -style=file -i path/to/modified/file.m
```

### Swift Files (iphone/Classes/**/*.swift)
Run clang-format on all modified Swift files:
```bash
npx clang-format -style=file -i path/to/modified/file.swift
```

## Validation Workflow

1. **After making any code changes**, immediately run the appropriate validation commands for the file types you modified
2. **Fix all linting/style errors** before proceeding
3. **Re-run the checks** to ensure all issues are resolved
4. **Only then** consider the changes ready for commit

## Common Issues and Fixes

### JavaScript (ESLint)
- **Trailing spaces**: Remove all trailing whitespace
- **Missing newline at EOF**: Add a newline at the end of the file
- **Array bracket spacing**: Ensure spaces after `[` and before `]` (e.g., `[ 'item1', 'item2' ]`)
- **Function parentheses spacing**: Add space before function parentheses
- **Unused variables**: Remove or prefix with underscore

### Java (Checkstyle)
- **Left curly brace placement**: Move `{` to new line
- **Trailing whitespace**: Remove all trailing spaces
- **Unused imports**: Remove unused import statements
- **Line length**: Keep lines under the specified limit

### Objective-C/Swift (clang-format)
- **Indentation**: Use consistent spacing
- **Bracket placement**: Follow project style guidelines
- **Method spacing**: Proper spacing around methods and properties

## Automation Commands

For convenience, you can run checks on all staged files:

```bash
# Check all staged JavaScript files
git diff --cached --name-only --diff-filter=ACM | grep '\.js$' | xargs npx eslint --fix

# Check all staged Java files
CHANGED_JAVA_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.java$' | tr '\n' ',' | sed 's/,$//')
if [ -n "$CHANGED_JAVA_FILES" ]; then
  node ./build/scons gradlew checkJavaStyle --args --console plain -PchangedFiles="$CHANGED_JAVA_FILES"
fi

# Format all staged Objective-C/Header files
git diff --cached --name-only --diff-filter=ACM | grep '\.\(m\|h\)$' | xargs npx clang-format -style=file -i
```

## Pre-commit Hook Integration

The repository uses Husky pre-commit hooks with lint-staged that automatically run these checks:

### Lint-staged Configuration
```json
{
  "android/**/*.java": [
    "node ./build/scons gradlew checkJavaStyle --args --console plain -PchangedFiles='${files}'"
  ],
  "iphone/**/*.{m,h}": [
    "npx clang-format -style=file -i"
  ],
  "iphone/Classes/**/*.swift": [
    "npx clang-format -style=file -i"
  ],
  "*.js": [
    "eslint --fix"
  ]
}
```

### Common Pre-commit Failures and Fixes

**ESLint Failures:**
- Run `npx eslint --fix` on JavaScript files
- Manually fix remaining issues like unused variables
- Add newlines at end of files
- Remove trailing whitespace with `sed -i '' 's/[[:space:]]*$//' filename.js`

**Checkstyle Failures:**
- Move opening braces `{` to new lines for methods
- Remove trailing whitespace with `sed -i '' 's/[[:space:]]*$//' filename.java`
- Remove unused imports
- Fix line length violations

**Quick Fix Commands:**
```bash
# Remove trailing whitespace from all staged files
git diff --cached --name-only | xargs sed -i '' 's/[[:space:]]*$//'

# Fix ESLint issues automatically
git diff --cached --name-only --diff-filter=ACM | grep '\.js$' | xargs npx eslint --fix

# Format all staged code files
git diff --cached --name-only --diff-filter=ACM | grep '\.\(m\|h\)$' | xargs npx clang-format -style=file -i
```

## Git Hook Behavior and Recovery

### Important: Git Hook File Reversion
**CRITICAL**: When pre-commit hooks fail, Git automatically reverts ALL staged files to their original state. This means:

1. **Your fixes are lost** when hooks fail
2. **You must re-apply all fixes** after each failed commit attempt
3. **Always re-stage files** with `git add -A` after fixing issues

### Recovery Process After Hook Failures:
```bash
# 1. Git hooks failed and reverted files - re-stage everything
git add -A

# 2. Fix all issues comprehensively in one go
npx eslint example/stackview_example.js tests/Resources/*.js --fix
sed -i '' 's/[[:space:]]*$//' android/modules/ui/src/java/ti/modules/titanium/ui/*.java
npx clang-format -style=file -i iphone/Classes/*.{h,m}

# 3. Re-stage the fixed files
git add -A

# 4. Verify all checks pass before committing
npx eslint example/stackview_example.js tests/Resources/*.js
node ./build/scons gradlew checkJavaStyle --args --console plain -PchangedFiles="comma,separated,files"

# 5. Commit with proper conventional commit format
git commit -m "feat: your commit message"
```

## Commit Message Format

The repository enforces **Conventional Commits** format. Your commit messages MUST follow this pattern:

```
<type>: <description>

[optional body]
```

### Valid Types:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

### Examples:
```bash
# ✅ Correct
git commit -m "feat: add StackView implementation for Android and iOS"
git commit -m "fix: resolve memory leak in TiUIStackView"
git commit -m "docs: update StackView API documentation"

# ❌ Incorrect (will be rejected)
git commit -m "Add StackView implementation"
git commit -m "Fixed bug"
```

## Mandatory Rule

**NEVER** consider code changes complete without running and passing these validation checks. This ensures:
- Consistent code style across the project
- Early detection of potential issues
- Compliance with project standards
- Smooth integration with CI/CD pipelines

The checks that failed in your commit attempt MUST be fixed before the code can be committed.

**If pre-commit hooks fail:**
1. Read the error output carefully
2. Run the appropriate fix commands above
3. **Re-stage ALL files** with `git add -A` (hooks revert everything)
4. Verify all checks pass manually
5. Use conventional commit format
6. Attempt the commit again

## Persistence Strategy

Due to git hook behavior, it's recommended to:

1. **Fix ALL file types at once** rather than incrementally
2. **Test ALL validations** before attempting commit
3. **Use comprehensive fix commands** that handle multiple issues
4. **Always re-stage after hook failures**
5. **Verify commit message format** before committing
