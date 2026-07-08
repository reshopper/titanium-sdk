# Bun Migration Plan - Titanium SDK

**Status:** 🟡 POC Fase  
**Sværhedsgrad:** Medium-Høj  
**Estimeret Tidsforbrug:** 2-3 uger POC + 4-6 uger stabilisering  
**Mål:** Dual-runtime support (Bun + Node) med Node som fallback for native builds

---

## Executive Summary

Titanium SDK skal understøtte Bun som alternativt runtime for JavaScript build-scripts, mens Node fastholdes til native compilation (V8, node-gyp, iOS/Android toolchains). Strategien er "dual-runtime" med feature gates og gradvis udrulning.

**Kerneprincip:** Bun for JS-operations, Node for native operations, ingen breaking changes.

---

## 📊 Risikoanalyse

### Generelle Risici (Bun vs Node)

| Område | Risiko | Årsag | Mitigering |
|--------|--------|-------|------------|
| **Node API Kompatibilitet** | Medium | Bun dækker ikke 100% af Node APIs (worker_threads, loader hooks) | Runtime detection + Node fallback |
| **Native Moduler** | Høj | node-gyp, N-API, postinstall kan fejle under Bun | Force Node for alt native compilation |
| **npm Scripts** | Lav-Medium | `npm_*` env-vars mangler i Bun | Explicit env-var configuration |
| **CJS/ESM Loading** | Lav | Bun håndterer CJS, men kan have subtile forskelle | Test alle require/import patterns |
| **Buffer/Streams** | Medium | Binær håndtering kan afvige | Force Node for APK/IPA packaging |
| **process.execPath** | Medium | Scripts kan antage Node's semantik | Explicit runtime checks |
| **Child Processes** | Lav-Medium | PATH og env propagering kan afvige | Explicit env forwarding |

### Titanium SDK-Specifikke Risici

| Komponent | Risiko | Kritikalitet | Strategi |
|-----------|--------|--------------|----------|
| **V8 Runtime Compilation** | ⛔ Kritisk | BLOCKER | **Kun Node** - Aldrig Bun |
| **node-gyp Builds** | ⛔ Kritisk | BLOCKER | **Kun Node** - Aldrig Bun |
| **iOS Native Builds** | ⛔ Kritisk | BLOCKER | **Kun Node** - Aldrig Bun |
| **Android Gradle Integration** | 🔴 Høj | Vigtig | Node standard, test Bun grundigt |
| **APK/IPA Packaging** | 🔴 Høj | Vigtig | Node standard, verify bit-identisk |
| **Build Scripts (20+)** | 🟡 Medium | Moderat | Test individuelt, whitelisting |
| **EJS Template Rendering** | 🟡 Medium | Moderat | Test rendering output |
| **Documentation Generation** | 🟢 Lav | Lav | Safe for Bun |
| **Linting/Formatting** | 🟢 Lav | Lav | Safe for Bun |

---

## 🎯 Safe/Unsafe Targets

### ✅ Bun-Safe Targets (Lav Risiko)

```bash
# Linting & Formatting
npx eslint → bunx eslint
npx clang-format → bunx clang-format

# Testing (JS-only)
npm test → bun test (kun JS tests, ikke native)

# Documentation
build/lib/docs.js → Bun-safe
apidoc generation → Bun-safe

# Utilities
build/lib/utils.js → Bun-safe (test grundigt)
```

### ⚠️ Test Required (Medium Risiko)

```bash
# Build orchestration
build/scons*.js → Test individuelt
build/lib/builder.js → Test med mock data

# Package management
cli/lib/*.js → Test uden native deps
```

### ❌ Node-Only Targets (Høj-Kritisk Risiko)

```bash
# V8 Runtime
android/runtime/v8/** → KUN NODE

# Native Compilation
*/node-gyp → KUN NODE
*/prebuild → KUN NODE
**/binding.gyp → KUN NODE

# iOS Builds
iphone/cli/** → KUN NODE (indtil verificeret)
build/lib/ios.js → KUN NODE

# Android Builds
build/lib/android/** → KUN NODE (indtil verificeret)
android/titanium/genBootstrap.js → KUN NODE

# Binary Packaging
build/lib/packager.js → KUN NODE (bit-identisk krav)
```

---

## 📅 Implementeringsplan

### **Fase 0: Præ-POC Analyse** (Dag 1-2)

**Mål:** Kortlægge alle build-scripts og native dependencies

#### Opgaver:
1. **Inventory Scripts**
   ```bash
   # Liste alle build entry points
   find build/ -name "*.js" -type f > build-scripts.txt
   find cli/ -name "*.js" -type f >> build-scripts.txt
   
   # Find alle node-gyp dependencies
   grep -r "node-gyp" . --include="*.json" --include="*.js"
   grep -r "binding.gyp" .
   
   # Find alle native module imports
   grep -r "require.*\.node" .
   ```

2. **Kategoriser Scripts**
   - Pure JS (safe for Bun)
   - File I/O heavy (test required)
   - Native dependencies (Node-only)
   - Child process heavy (test required)

3. **Dokumentér Dependencies**
   ```bash
   # Liste alle dependencies med native builds
   npm ls --all | grep -E "(node-gyp|prebuild|native)"
   ```

#### Deliverables:
- [ ] `docs/BUILD_SCRIPTS_INVENTORY.md` - Komplet liste
- [ ] `docs/NATIVE_DEPENDENCIES.md` - Native deps katalog
- [ ] `docs/RUNTIME_COMPATIBILITY_MATRIX.md` - Script kategorisering

---

### **Fase 1: Foundation** (Dag 3-5)

**Mål:** Opret runtime detection og Node fallback infrastruktur

#### 1.1 Runtime Detection Helper

**Fil:** `build/lib/runtime-detect.js`

```javascript
/**
 * Runtime detection and Node fallback utilities for Titanium SDK
 * 
 * Provides dual-runtime support (Bun + Node) with automatic fallback
 * for critical operations that require Node.
 */

const { spawnSync, spawn } = require('child_process');
const path = require('path');

// Runtime detection
const isBun = !!process.versions.bun;
const isNode = !isBun && !!process.versions.node;

// Runtime info
const runtimeInfo = {
	name: isBun ? 'Bun' : 'Node',
	version: isBun ? process.versions.bun : process.versions.node,
	execPath: process.execPath
};

// Environment configuration
const config = {
	// User preference (can be overridden)
	preferredRuntime: process.env.TI_RUNTIME || (isBun ? 'bun' : 'node'),
	
	// Native operations ALWAYS use Node
	nativeRuntime: process.env.TI_NATIVE_RUNTIME || 'node',
	
	// Verbose logging
	verbose: process.env.TI_BUILD_VERBOSE === '1' || process.env.TI_BUILD_VERBOSE === 'true'
};

/**
 * Log runtime decision (if verbose)
 */
function logRuntime(operation, runtime, reason) {
	if (config.verbose) {
		console.log(`[Runtime] ${operation}: ${runtime} (${reason})`);
	}
}

/**
 * Execute script under Node (synchronous)
 * 
 * @param {string} scriptPath - Path to script
 * @param {string[]} args - Arguments
 * @param {object} options - spawn options
 * @returns {object} spawn result
 */
function requireNode(scriptPath, args = [], options = {}) {
	const nodePath = process.env.TI_NODE_PATH || 'node';
	
	logRuntime(
		scriptPath,
		'Node (forced)',
		'Native operation or explicit Node requirement'
	);
	
	if (isNode) {
		// Already in Node, just require it
		return require(path.resolve(scriptPath));
	}
	
	// Force Node execution
	const result = spawnSync(nodePath, [scriptPath, ...args], {
		stdio: 'inherit',
		env: { ...process.env, TI_RUNTIME: 'node' },
		...options
	});
	
	if (result.error) {
		throw new Error(`Failed to execute ${scriptPath} under Node: ${result.error.message}`);
	}
	
	return result;
}

/**
 * Spawn process under Node (async)
 * 
 * @param {string} scriptPath - Path to script
 * @param {string[]} args - Arguments
 * @param {object} options - spawn options
 * @returns {ChildProcess} spawned process
 */
function spawnNode(scriptPath, args = [], options = {}) {
	const nodePath = process.env.TI_NODE_PATH || 'node';
	
	logRuntime(
		scriptPath,
		'Node (forced, async)',
		'Native operation or explicit Node requirement'
	);
	
	return spawn(nodePath, [scriptPath, ...args], {
		stdio: 'inherit',
		env: { ...process.env, TI_RUNTIME: 'node' },
		...options
	});
}

/**
 * Check if current operation should use Node
 * 
 * @param {string} operation - Operation name
 * @returns {boolean}
 */
function shouldUseNode(operation) {
	// Critical operations that MUST use Node
	const nodeOnlyPatterns = [
		/node-gyp/i,
		/binding\.gyp/i,
		/\.node$/,
		/v8.*build/i,
		/native.*compile/i,
		/prebuild/i,
		/postinstall/i,
		/genBootstrap/i,
		/android.*gradle/i,
		/ios.*build/i,
		/packager/i
	];
	
	return nodeOnlyPatterns.some(pattern => pattern.test(operation));
}

/**
 * Ensure we're running under Node, or switch to it
 * 
 * @param {string} scriptPath - Current script path
 * @param {string[]} args - Script arguments
 */
function ensureNode(scriptPath, args = []) {
	if (isNode) {
		return; // Already Node 
	}
	
	logRuntime(
		scriptPath,
		'Node (switching)',
		'Critical operation requires Node'
	);
	
	// Re-exec under Node
	const nodePath = process.env.TI_NODE_PATH || 'node';
	const result = spawnSync(nodePath, [scriptPath, ...args], {
		stdio: 'inherit',
		env: { ...process.env, TI_RUNTIME: 'node' }
	});
	
	process.exit(result.status || 0);
}

/**
 * Get shimmed environment variables for npm compatibility
 * 
 * Bun doesn't set npm_* variables, so we shim them
 */
function getNpmCompatEnv() {
	if (isNode) {
		return process.env; // Node already has them
	}
	
	// Shim npm_* variables for Bun
	return {
		...process.env,
		npm_config_registry: process.env.npm_config_registry || 'https://registry.npmjs.org/',
		npm_node_execpath: process.execPath,
		npm_execpath: process.execPath,
		npm_config_user_agent: `bun/${process.versions.bun} (titanium-sdk)`
	};
}

module.exports = {
	// Runtime info
	isBun,
	isNode,
	runtimeInfo,
	config,
	
	// Node execution
	requireNode,
	spawnNode,
	ensureNode,
	
	// Helpers
	shouldUseNode,
	getNpmCompatEnv,
	logRuntime
};
```

#### 1.2 Node Native Wrapper Script

**Fil:** `scripts/node-native.sh`

```bash
#!/usr/bin/env bash
#
# node-native.sh - Force Node runtime for native operations
#
# Usage: ./scripts/node-native.sh script.js [args...]
#

set -e

# Use TI_NODE_PATH if set, otherwise 'node'
NODE_PATH="${TI_NODE_PATH:-node}"

# Check Node is available
if ! command -v "$NODE_PATH" &> /dev/null; then
    echo "Error: Node.js not found at '$NODE_PATH'" >&2
    echo "Set TI_NODE_PATH environment variable to Node.js binary path" >&2
    exit 1
fi

# Log if verbose
if [ "${TI_BUILD_VERBOSE}" = "1" ]; then
    echo "[node-native] Executing under Node: $NODE_PATH $*" >&2
fi

# Execute under Node with all args
exec "$NODE_PATH" "$@"
```

```bash
chmod +x scripts/node-native.sh
```

#### 1.3 Environment Configuration

**Fil:** `.env.example`

```bash
# Titanium SDK Build Runtime Configuration
# Copy to .env and customize as needed

# ============================================================================
# Runtime Selection
# ============================================================================

# Preferred runtime for JavaScript operations
# Options: "node" | "bun"
# Default: Auto-detect (bun if available, else node)
# Note: Native operations always use Node regardless of this setting
TI_RUNTIME=node

# Runtime for native operations (LOCKED to node)
# DO NOT CHANGE unless you know what you're doing
# Native compilation (V8, node-gyp, iOS/Android) requires Node
TI_NATIVE_RUNTIME=node

# Path to Node.js binary (for forced Node execution)
# Default: "node" (from PATH)
# TI_NODE_PATH=/usr/local/bin/node

# Path to Bun binary (if not in PATH)
# Default: "bun" (from PATH)
# TI_BUN_PATH=/usr/local/bin/bun

# ============================================================================
# Build Verbosity
# ============================================================================

# Enable verbose runtime logging
# Options: "0" | "1" | "true" | "false"
# Default: "0"
# When enabled, logs which runtime is used for each operation
TI_BUILD_VERBOSE=0

# ============================================================================
# Bun-Specific Configuration
# ============================================================================

# Bun install behavior
# Options: "force-node" | "allow-bun"
# Default: "force-node" (safer for native deps)
# Warning: "allow-bun" may break native module installation
TI_INSTALL_RUNTIME=force-node

# ============================================================================
# CI/CD Configuration
# ============================================================================

# Force specific runtime in CI
# Useful for testing both runtimes in matrix
# CI_RUNTIME=node

# Skip runtime detection in CI
# Always use specified runtime
# CI_SKIP_DETECTION=false

# ============================================================================
# Development Flags
# ============================================================================

# Allow experimental Bun features
# Default: "false"
# Warning: May cause unexpected behavior
TI_BUN_EXPERIMENTAL=false

# Fail fast on runtime incompatibility
# Default: "true"
# When true, exit immediately if incompatible runtime detected
TI_FAIL_ON_INCOMPATIBILITY=true
```

#### 1.4 Git Ignore Updates

**Fil:** `.gitignore` (tilføj)

```gitignore
# Runtime configuration (user-specific)
.env

# Bun lockfile (ikke check ind før stable)
bun.lockb

# Runtime logs
runtime-*.log
```

#### Deliverables:
- [x] `build/lib/runtime-detect.js` - Runtime detection
- [x] `scripts/node-native.sh` - Node wrapper
- [x] `.env.example` - Configuration template
- [ ] `.gitignore` - Updated

---

### **Fase 2: Wire Safe Targets** (Dag 6-8)

**Mål:** Integrér runtime detection i safe targets (linting, docs, utils)

#### 2.1 Update Package Scripts

**Fil:** `package.json` (tilføj runtime-aware scripts)

```json
{
  "scripts": {
    "// Runtime Detection": "Scripts that auto-detect runtime",
    "lint": "node -e \"require('./build/lib/runtime-detect').isBun ? require('child_process').spawnSync('bunx', ['eslint', '.'], {stdio:'inherit'}) : require('child_process').spawnSync('npx', ['eslint', '.'], {stdio:'inherit'})\"",
    
    "// Safe Targets": "Bun-safe operations",
    "lint:bun": "bunx eslint .",
    "lint:node": "npx eslint .",
    "docs:bun": "bun run build/lib/docs.js",
    "docs:node": "node build/lib/docs.js",
    
    "// Native Targets": "Node-only operations",
    "build:v8": "./scripts/node-native.sh android/runtime/v8/build.js",
    "build:android:native": "./scripts/node-native.sh build/lib/android/build-native.js",
    "build:ios:native": "./scripts/node-native.sh build/lib/ios/build-native.js",
    
    "// Composite Builds": "Multi-stage builds with runtime switching",
    "build:android": "npm run build:android:native && npm run package:android",
    "build:ios": "npm run build:ios:native && npm run package:ios"
  }
}
```

#### 2.2 Update Build Scripts

**Fil:** `build/scons.js` (eksempel integration)

```javascript
const { isBun, isNode, ensureNode, shouldUseNode, runtimeInfo } = require('./lib/runtime-detect');

console.log(`Running under ${runtimeInfo.name} ${runtimeInfo.version}`);

// Check if this operation requires Node
if (shouldUseNode('build-android-native')) {
	ensureNode(__filename, process.argv.slice(2));
}

// Rest of existing scons.js code...
```

#### 2.3 Test Matrix

```bash
# Test linting under both runtimes
npm run lint:node   # Should pass
npm run lint:bun    # Should pass

# Test documentation under both runtimes
npm run docs:node   # Should pass
npm run docs:bun    # Should pass (verify output identical)

# Verify native builds only use Node
npm run build:v8    # Must use Node (verify in logs)
```

#### Deliverables:
- [ ] `package.json` - Updated scripts
- [ ] `build/scons.js` - Runtime detection integrated
- [ ] Test results documented

---

### **Fase 3: Logging & Verification** (Dag 9-11)

**Mål:** Comprehensive logging og output verification

#### 3.1 Build Logger

**Fil:** `build/lib/logger.js`

```javascript
const { runtimeInfo, config } = require('./runtime-detect');
const fs = require('fs');
const path = require('path');

const logFile = path.join(__dirname, '../../runtime-build.log');

function log(level, operation, message, metadata = {}) {
	const timestamp = new Date().toISOString();
	const entry = {
		timestamp,
		level,
		runtime: runtimeInfo.name,
		runtimeVersion: runtimeInfo.version,
		operation,
		message,
		...metadata
	};
	
	// Console output if verbose
	if (config.verbose) {
		console.log(`[${level}] ${operation}: ${message}`);
	}
	
	// Always log to file
	fs.appendFileSync(logFile, JSON.stringify(entry) + '\n');
}

module.exports = {
	info: (op, msg, meta) => log('INFO', op, msg, meta),
	warn: (op, msg, meta) => log('WARN', op, msg, meta),
	error: (op, msg, meta) => log('ERROR', op, msg, meta),
	runtime: (op, runtime, reason) => log('RUNTIME', op, `Using ${runtime}`, { reason })
};
```

#### 3.2 Output Verification Script

**Fil:** `scripts/verify-runtime-output.js`

```javascript
#!/usr/bin/env node
/**
 * Verify outputs are identical between Node and Bun builds
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function hashFile(filePath) {
	const content = fs.readFileSync(filePath);
	return crypto.createHash('sha256').update(content).digest('hex');
}

function compareOutputs(nodeOutput, bunOutput) {
	const nodeHash = hashFile(nodeOutput);
	const bunHash = hashFile(bunOutput);
	
	console.log(`Node output: ${nodeHash}`);
	console.log(`Bun output:  ${bunHash}`);
	
	if (nodeHash === bunHash) {
		console.log('✅ Outputs are identical');
		return true;
	} else {
		console.log('❌ Outputs differ');
		return false;
	}
}

// Example usage:
// compareOutputs('dist/node-build/docs.html', 'dist/bun-build/docs.html');

module.exports = { compareOutputs, hashFile };
```

#### Deliverables:
- [ ] `build/lib/logger.js` - Build logger
- [ ] `scripts/verify-runtime-output.js` - Verification tool
- [ ] Test logs comparing Node vs Bun outputs

---

### **Fase 4: Testing Protocol** (Dag 12-14)

**Mål:** Systematisk test af alle targets

#### 4.1 Test Checklist

**Fil:** `docs/BUN_TESTING_PROTOCOL.md`

```markdown
# Bun Testing Protocol

## Phase 1: Safe Targets (Must Pass)

### Linting
- [ ] `npm run lint:node` passes
- [ ] `npm run lint:bun` passes
- [ ] Exit codes identical
- [ ] Output identical

### Documentation
- [ ] `npm run docs:node` passes
- [ ] `npm run docs:bun` passes
- [ ] Generated HTML byte-identical (hash comparison)

### Utilities
- [ ] `build/lib/utils.js` under Node
- [ ] `build/lib/utils.js` under Bun
- [ ] Outputs identical

## Phase 2: Build Scripts (Test Individually)

For each script in `build/scons-*.js`:
- [ ] Run under Node (baseline)
- [ ] Run under Bun
- [ ] Compare logs
- [ ] Compare outputs
- [ ] Document differences
- [ ] Categorize: Safe/Unsafe/Needs-Work

## Phase 3: Native Builds (Must Use Node)

### Android V8
- [ ] Verify uses Node (check logs)
- [ ] Build completes
- [ ] Binaries identical to baseline

### Android Gradle
- [ ] Verify uses Node
- [ ] APK builds
- [ ] APK hash identical to baseline

### iOS Native
- [ ] Verify uses Node
- [ ] Build completes
- [ ] IPA hash identical to baseline

## Phase 4: End-to-End

### Full Android Build
- [ ] Clean build under Node (baseline)
- [ ] Clean build with Bun (safe targets) + Node (native)
- [ ] Compare APK hashes
- [ ] Compare build logs
- [ ] Performance comparison

### Full iOS Build
- [ ] Clean build under Node (baseline)
- [ ] Clean build with Bun (safe targets) + Node (native)
- [ ] Compare IPA hashes
- [ ] Compare build logs
- [ ] Performance comparison

## Acceptance Criteria

✅ All tests must pass:
- [ ] No regressions in Node-only builds
- [ ] Bun builds match Node builds (where applicable)
- [ ] Native builds always use Node
- [ ] No silent failures
- [ ] Build times acceptable (not significantly slower)
```

#### 4.2 Automated Test Runner

**Fil:** `scripts/test-runtimes.sh`

```bash
#!/usr/bin/env bash
#
# test-runtimes.sh - Automated runtime testing
#

set -e

echo "=== Titanium SDK Runtime Testing ==="
echo ""

# Setup
export TI_BUILD_VERBOSE=1
RESULTS_DIR="test-results/runtime-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTS_DIR"

# Phase 1: Safe Targets
echo "Phase 1: Testing Safe Targets..."

echo "  - Linting (Node)..."
TI_RUNTIME=node npm run lint > "$RESULTS_DIR/lint-node.log" 2>&1

echo "  - Linting (Bun)..."
TI_RUNTIME=bun npm run lint > "$RESULTS_DIR/lint-bun.log" 2>&1

echo "  - Comparing lint outputs..."
diff "$RESULTS_DIR/lint-node.log" "$RESULTS_DIR/lint-bun.log" > "$RESULTS_DIR/lint-diff.log" || true

# Phase 2: Documentation
echo "Phase 2: Testing Documentation Generation..."

echo "  - Docs (Node)..."
TI_RUNTIME=node npm run docs > "$RESULTS_DIR/docs-node.log" 2>&1

echo "  - Docs (Bun)..."
TI_RUNTIME=bun npm run docs > "$RESULTS_DIR/docs-bun.log" 2>&1

echo "  - Hashing outputs..."
node scripts/verify-runtime-output.js > "$RESULTS_DIR/docs-verification.log" 2>&1

# Phase 3: Native (should all use Node)
echo "Phase 3: Testing Native Builds (Node-only)..."

echo "  - V8 Build..."
npm run build:v8 > "$RESULTS_DIR/v8-build.log" 2>&1

# Verify it used Node
if grep -q "Runtime.*Node" "$RESULTS_DIR/v8-build.log"; then
    echo "    ✅ V8 build correctly used Node"
else
    echo "    ❌ V8 build did NOT use Node - CRITICAL ERROR"
    exit 1
fi

echo ""
echo "=== Test Results Summary ==="
echo "Results saved to: $RESULTS_DIR"
echo ""
echo "Review:"
echo "  - lint-diff.log (should be empty or trivial)"
echo "  - docs-verification.log (should show identical hashes)"
echo "  - v8-build.log (should show 'Using Node')"
```

#### Deliverables:
- [ ] `docs/BUN_TESTING_PROTOCOL.md` - Test protocol
- [ ] `scripts/test-runtimes.sh` - Automated tests
- [ ] Test results for all safe targets

---

### **Fase 5: CI/CD Integration** (Dag 15-17)

**Mål:** CI matrix til dual-runtime testing

#### 5.1 GitHub Actions Workflow (hvis relevant)

**Fil:** `.github/workflows/bun-poc.yml`

```yaml
name: Bun POC Testing

on:
  pull_request:
    paths:
      - 'build/**'
      - 'cli/**'
      - '.github/workflows/bun-poc.yml'
  workflow_dispatch:

jobs:
  test-safe-targets:
    name: Test Safe Targets (${{ matrix.runtime }})
    runs-on: ubuntu-latest
    strategy:
      matrix:
        runtime: [node, bun]
        node-version: [20.x]
        bun-version: [1.0.x]
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: ${{ matrix.node-version }}
      
      - name: Setup Bun
        if: matrix.runtime == 'bun'
        uses: oven-sh/setup-bun@v1
        with:
          bun-version: ${{ matrix.bun-version }}
      
      - name: Install Dependencies
        run: npm ci
      
      - name: Run Linting
        env:
          TI_RUNTIME: ${{ matrix.runtime }}
          TI_BUILD_VERBOSE: 1
        run: npm run lint
      
      - name: Generate Documentation
        env:
          TI_RUNTIME: ${{ matrix.runtime }}
          TI_BUILD_VERBOSE: 1
        run: npm run docs
      
      - name: Upload Build Logs
        uses: actions/upload-artifact@v3
        with:
          name: build-logs-${{ matrix.runtime }}
          path: runtime-build.log

  verify-native-node-only:
    name: Verify Native Builds Use Node
    runs-on: macos-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 20.x
      
      - name: Setup Bun
        uses: oven-sh/setup-bun@v1
        with:
          bun-version: 1.0.x
      
      - name: Install Dependencies
        run: npm ci
      
      - name: Build V8 (Should Use Node)
        env:
          TI_RUNTIME: bun
          TI_BUILD_VERBOSE: 1
        run: npm run build:v8
      
      - name: Verify Node Was Used
        run: |
          if grep -q "Runtime.*Node" runtime-build.log; then
            echo "✅ V8 build correctly used Node"
          else
            echo "❌ V8 build did NOT use Node"
            exit 1
          fi
```

#### Deliverables:
- [ ] `.github/workflows/bun-poc.yml` - CI workflow
- [ ] CI runs successfully for both runtimes
- [ ] Native builds verified to use Node

---

### **Fase 6: Documentation** (Dag 18-19)

**Mål:** Omfattende dokumentation

#### 6.1 Developer Guide

**Fil:** `docs/BUN_DEVELOPER_GUIDE.md`

```markdown
# Bun Developer Guide - Titanium SDK

## Quick Start

### Using Bun for Safe Operations

```bash
# Linting
TI_RUNTIME=bun npm run lint

# Documentation
TI_RUNTIME=bun npm run docs

# Testing (JS-only)
TI_RUNTIME=bun npm test
```

### Native Builds (Always Node)

```bash
# These automatically use Node regardless of TI_RUNTIME
npm run build:v8
npm run build:android:native
npm run build:ios:native
```

## Configuration

Copy `.env.example` to `.env` and customize:

```bash
TI_RUNTIME=bun          # Prefer Bun for JS operations
TI_BUILD_VERBOSE=1      # Log runtime decisions
```

## Script Development

When creating new build scripts:

```javascript
const { shouldUseNode, ensureNode } = require('../lib/runtime-detect');

// Check if this operation requires Node
if (shouldUseNode('my-native-operation')) {
	ensureNode(__filename, process.argv.slice(2));
}

// Your script code...
```

## Troubleshooting

### "Operation X failed under Bun"

1. Check if operation is native (should use Node)
2. Force Node: `TI_RUNTIME=node npm run ...`
3. Report issue with logs

### "Build output differs between runtimes"

1. Run verification: `node scripts/verify-runtime-output.js`
2. If critical, force Node for that operation
3. Report issue for investigation

## Performance Tips

- Use Bun for: linting, testing, documentation
- Use Node for: native compilation, packaging
- Benchmark before switching defaults
```

#### 6.2 FAQ

**Fil:** `docs/BUN_FAQ.md`

```markdown
# Bun Migration FAQ

## General Questions

### Why Bun?
- Faster script execution for JS-heavy operations
- Better developer experience (faster iteration)
- Modern runtime with good Node compatibility

### Why keep Node?
- Native compilation (node-gyp, V8) requires Node
- 100% compatibility guarantee for critical paths
- Gradual migration reduces risk

### Is this production-ready?
- Not yet - POC phase
- Safe targets tested and verified
- Native builds always use Node

## Technical Questions

### Which operations use Bun?
- Linting (eslint)
- Documentation generation
- JS-only testing
- Utility scripts (when verified)

### Which operations use Node?
- V8 runtime compilation
- node-gyp builds
- iOS/Android native compilation
- Binary packaging (APK/IPA)

### How do I force Node?
```bash
TI_RUNTIME=node npm run build
```

### How do I force Bun?
```bash
TI_RUNTIME=bun npm run lint
```

### What if Bun breaks something?
```bash
# Immediate fallback
TI_RUNTIME=node npm run ...

# Report issue with:
# 1. Operation that failed
# 2. Error message
# 3. runtime-build.log
```

## Contribution Guidelines

### Adding Bun support to a script

1. Test thoroughly under both runtimes
2. Add runtime detection if needed
3. Document in script comments
4. Update testing protocol
5. PR with test results

### Reporting issues

Include:
- Script/operation name
- Runtime version (bun --version / node --version)
- Error message
- runtime-build.log
- Steps to reproduce
```

#### Deliverables:
- [ ] `docs/BUN_DEVELOPER_GUIDE.md` - Developer guide
- [ ] `docs/BUN_FAQ.md` - FAQ
- [ ] This file: `docs/BUN_MIGRATION_PLAN.md` - Migration plan

---

## 📊 Success Metrics

### POC Success (After Phase 6)

- [ ] Runtime detection works in 100% of cases
- [ ] All safe targets tested (lint, docs, utils)
- [ ] Native builds verified to use Node
- [ ] No regressions in Node-only builds
- [ ] Documentation complete
- [ ] CI pipeline running

### Production Ready (Future)

- [ ] 50+ build scripts tested individually
- [ ] Full Android build works (Bun + Node hybrid)
- [ ] Full iOS build works (Bun + Node hybrid)
- [ ] APK/IPA hashes verified identical
- [ ] Performance benchmarks show improvement
- [ ] 3+ months of stable usage
- [ ] Zero critical issues reported

---

## 🔄 Rollback Plan

### Immediate Rollback (If Critical Issue)

```bash
# 1. Set global default back to Node
export TI_RUNTIME=node

# 2. Update .env
echo "TI_RUNTIME=node" > .env

# 3. Rebuild everything
npm run clean
npm run build
```

### Partial Rollback (Specific Operation)

```bash
# Disable Bun for specific operation
TI_RUNTIME=node npm run problematic-operation
```

### Full Rollback (Abandon Bun)

1. Remove `.env` Bun configuration
2. Remove Bun from CI workflows
3. Document lessons learned
4. Keep runtime-detect.js for future attempts

---

## 📝 Open Questions & Risks

### Unresolved Questions

1. **Performance impact:** Is Bun actually faster for our use case?
   - Need benchmarks on full builds
   - May vary by operation type

2. **Long-term support:** How stable is Bun's Node API compatibility?
   - Monitor Bun releases for breaking changes
   - Have contingency for API divergence

3. **Team adoption:** Will developers use/trust Bun?
   - Training needed
   - Clear documentation critical

### Known Risks

1. **Subtle runtime differences** may emerge over time
   - Mitigation: Extensive testing, logging, verification
   
2. **Bun breaking changes** in updates
   - Mitigation: Pin Bun version, test before upgrading

3. **Confusion about which runtime to use**
   - Mitigation: Clear documentation, verbose logging

---

## 🎯 Next Steps

### Immediate (This Week)

1. [ ] Review and approve this plan
2. [ ] Create tracking issue/epic
3. [ ] Assign Phase 1 tasks
4. [ ] Setup development environment

### Short-term (Next 2 Weeks)

1. [ ] Complete Phase 1-3 (Foundation)
2. [ ] Test safe targets
3. [ ] Document findings
4. [ ] Team review meeting

### Medium-term (Next Month)

1. [ ] Complete Phase 4-6 (Testing, CI, Docs)
2. [ ] Expand safe target list
3. [ ] Performance benchmarks
4. [ ] Decision: Proceed to production or iterate

---

## 📞 Contact & Support

**Questions about this plan:** @jonasfunk  
**Runtime issues:** File issue with `[Bun]` prefix  
**Documentation:** See `docs/BUN_*.md` files

---

**Last Updated:** 2025-10-09  
**Status:** 🟡 POC Phase - Not Production Ready  
**Next Review:** After Phase 3 completion
