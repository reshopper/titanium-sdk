<!-- 75267b44-f1ec-4d31-81af-34fe236d84d5 b4a30852-7cb9-45e8-8673-a8295b9bdb50 -->
# Plan: Incremental JavaScript Encryption

## Problem

Nuværende system krypterer ALLE JavaScript filer (3,295 filer) ved hvert build, selvom kun få/ingen filer er ændret. Dette spilder ~20-25 sekunder på second run builds.

**Kritisk insight:** Android-builden tømmer `app/src/main` ved hvert build, så vi SKAL bruge et vedvarende cache directory udenfor `src/main` for at få incremental builds til at virke.

## Løsning

Opret en ny `EncryptJsTask` der:

1. Krypterer til vedvarende cache directory (`buildEncryptedAssetsDir`)
2. Kun re-krypterer ændrede filer (incremental build)
3. Kopierer fra cache til `src/main` efter kryptering (hurtig operation)

## Implementering

### 1. Opret cache directory i _build.js

**Fil:** `android/cli/commands/_build.js`

I builder initialization (omkring linje 500-600 hvor andre build directories defineres):

```javascript
this.buildEncryptedAssetsDir = path.join(this.buildDir, 'assets-encrypted');
```

### 2. Opret ny task fil

**Fil:** `cli/lib/tasks/encrypt-js-task.js`

Implementer `EncryptJsTask` class der:

- Extends `IncrementalFileTask` (fra appc-tasks)
- Tager input fra `builder.jsFilesToEncrypt` array
- **KRITISK:** Mapper source files fra `buildAssetsDir` til `.bin` output i **vedvarende cache directory** (`buildEncryptedAssetsDir`), IKKE direkte til `buildAppMainAssetsResourcesDir`
- Constructor initialiserer Cloak instance
- Implementerer `doFullTaskRun()` - krypterer alle filer til cache med ét samlet titaniumprep hook
- Implementerer `doIncrementalTaskRun(changedFiles)` - kun krypterer created/changed filer til cache med ét samlet titaniumprep hook
- Implementerer `processFile(filePath)` - krypterer enkelt fil med Cloak til cache
- Implementerer `requiresFullBuild()` - tjekker om `encryptJS`, `abis`, eller Cloak version har ændret sig (gem i data.json)
- Håndterer deleted files ved at fjerne tilsvarende `.bin` filer fra cache
- Eksponerer `cloak` instance for senere AssetCryptImpl.java generation
- Gemmer state i `data.json`: encryptJS flag, abis, cloak version

**Key pattern - ét samlet hook omslag:**

```javascript
class EncryptJsTask extends IncrementalFileTask {
	constructor(options) {
		options.name = 'encrypt-js';
		options.inputFiles = [];
		options.files.forEach((info, _file) => options.inputFiles.push(info.src));
		super(options);
		
		this.files = options.files;
		this.builder = options.builder;
		this.Cloak = require('ti.cloak').default;
		this.cloak = new this.Cloak();
		this.encryptJS = options.encryptJS;
	}
	
	async doFullTaskRun() {
		// Wrapper hele krypteringen i ét titaniumprep hook
		const hook = this.builder.cli.createHook('build.android.titaniumprep', this.builder, async (exe, args, opts, next) => {
			try {
				await Promise.all(
					Array.from(this.inputFiles).map(filePath => 
						limit(() => this.processFile(filePath))
					)
				);
				next();
			} catch (e) {
				next(e);
			}
		});
		return util.promisify(hook)(null, [this.builder.tiapp.guid, ''], {});
	}
	
	async doIncrementalTaskRun(changedFiles) {
		if (this.requiresFullBuild()) {
			return this.doFullTaskRun();
		}
		
		// Samme hook-pattern for incremental
		const hook = this.builder.cli.createHook('build.android.titaniumprep', this.builder, async (exe, args, opts, next) => {
			try {
				const deletedFiles = this.filterFilesByStatus(changedFiles, 'deleted');
				await Promise.all(deletedFiles.map(filePath => limit(() => this.handleDeletedFile(filePath))));
				
				const updatedFiles = this.filterFilesByStatus(changedFiles, ['created', 'changed']);
				await Promise.all(updatedFiles.map(filePath => limit(() => this.processFile(filePath))));
				
				next();
			} catch (e) {
				next(e);
			}
		});
		return util.promisify(hook)(null, [this.builder.tiapp.guid, ''], {});
	}
	
	async processFile(filePathAndName) {
		const file = this.resolveRelativePath(filePathAndName);
		const info = this.files.get(file);
		
		// Krypter til CACHE, ikke src/main
		const cacheDest = path.join(this.builder.buildEncryptedAssetsDir, file + '.bin');
		
		this.logger.debug(__('Encrypting: %s', info.src.cyan));
		await fs.ensureDir(path.dirname(cacheDest));
		await this.cloak.encryptFile(info.src, cacheDest);
	}
	
	requiresFullBuild() {
		// Tjek om kritiske parametre har ændret sig
		const currentState = {
			encryptJS: this.builder.encryptJS,
			abis: JSON.stringify(this.builder.abis),
			cloakVersion: this.Cloak.version || '1.0.0'
		};
		
		if (!this.data.lastState) return true;
		
		return this.data.lastState.encryptJS !== currentState.encryptJS
			|| this.data.lastState.abis !== currentState.abis
			|| this.data.lastState.cloakVersion !== currentState.cloakVersion;
	}
	
	async afterTaskAction() {
		await super.afterTaskAction();
		// Gem state for næste build
		this.data.lastState = {
			encryptJS: this.builder.encryptJS,
			abis: JSON.stringify(this.builder.abis),
			cloakVersion: this.Cloak.version || '1.0.0'
		};
	}
}
```

### 3. Modificer _build.js - encryptJSFiles()

**Fil:** `android/cli/commands/_build.js`

#### A. Import ny task (top af fil, omkring linje 30-50)

```javascript
const EncryptJsTask = require('../../cli/lib/tasks/encrypt-js-task');
```

#### B. Refactor encryptJSFiles() metode (linje 2800-2854)

**Nuværende kode krypterer direkte til src/main (bliver tømt):**

```2800:2854:android/cli/commands/_build.js
AndroidBuilder.prototype.encryptJSFiles = async function encryptJSFiles() {
	if (!this.jsFilesToEncrypt.length) {
		return;
	}
	const Cloak = require('ti.cloak').default;
	const cloak = this.encryptJS ? new Cloak() : null;
	
	this.logger.info('Encrypting javascript assets...');
	
	const hook = this.cli.createHook('build.android.titaniumprep', this, async function (exe, args, opts, next) {
		try {
			await Promise.all(
				this.jsFilesToEncrypt.map(async file => {
					const from = path.join(this.buildAssetsDir, file);
					const to = path.join(this.buildAppMainAssetsResourcesDir, file + '.bin');
					this.logger.debug(__('Encrypting: %s', from.cyan));
					await fs.ensureDir(path.dirname(to));
					this.unmarkBuildDirFile(to);
					return await cloak.encryptFile(from, to);
				})
			);
			// ... encryption key & AssetCryptImpl.java generation
		}
	});
}
```

**Ny kode med cache + copy:**

```javascript
AndroidBuilder.prototype.encryptJSFiles = async function encryptJSFiles() {
	if (!this.jsFilesToEncrypt.length) {
		return;
	}
	
	this.logger.info('Encrypting javascript assets...');
	
	// Build file map for EncryptJsTask
	const filesToEncrypt = new Map();
	for (const file of this.jsFilesToEncrypt) {
		filesToEncrypt.set(file, {
			src: path.join(this.buildAssetsDir, file),
			dest: path.join(this.buildAppMainAssetsResourcesDir, file + '.bin')
		});
	}
	
	// Run incremental encryption task (writes to cache)
	const task = new EncryptJsTask({
		incrementalDirectory: path.join(this.buildTiIncrementalDir, 'encrypt-js'),
		logger: this.logger,
		builder: this,
		files: filesToEncrypt,
		encryptJS: this.encryptJS
	});
	
	await task.run();
	
	// Copy encrypted files from cache to src/main (fast operation)
	this.logger.debug('Copying encrypted files to app assets');
	await Promise.all(
		this.jsFilesToEncrypt.map(async file => {
			const cacheFile = path.join(this.buildEncryptedAssetsDir, file + '.bin');
			const destFile = path.join(this.buildAppMainAssetsResourcesDir, file + '.bin');
			
			await fs.ensureDir(path.dirname(destFile));
			await fs.copyFile(cacheFile, destFile);
			this.unmarkBuildDirFile(destFile);
		})
	);
	
	// Generate AssetCryptImpl.java + encryption key (always needed, src/main is wiped)
	return this.generateAssetCryptImpl(task.cloak);
};
```

### 4. Opret generateAssetCryptImpl() metode

**Fil:** `android/cli/commands/_build.js`

Ekstraher AssetCryptImpl.java generation + encryption key til separat metode:

```javascript
AndroidBuilder.prototype.generateAssetCryptImpl = async function generateAssetCryptImpl(cloak) {
	this.logger.info('Writing encryption key...');
	await cloak.setKey('android', this.abis, path.join(this.buildAppMainDir, 'jniLibs'));
	
	// Generate 'AssetCryptImpl.java' from template
	const assetCryptDest = path.join(this.buildGenAppIdDir, 'AssetCryptImpl.java');
	this.unmarkBuildDirFile(assetCryptDest);
	await fs.ensureDir(this.buildGenAppIdDir);
	await fs.writeFile(
		assetCryptDest,
		ejs.render(
			await fs.readFile(path.join(this.templatesDir, 'AssetCryptImpl.java'), 'utf8'),
			{
				appid: this.appid,
				assets: this.jsFilesToEncrypt.map(f => f.replace(/\\/g, '/')),
				salt: cloak.salt
			}
		)
	);
};
```

### 5. Håndter cache directory lifecycle

Sørg for at `buildEncryptedAssetsDir` ikke tømmes ved builds:

- Den oprettes hvis den ikke findes
- Den bevares mellem builds (ligesom `buildAssetsDir`)
- IncrementalFileTask håndterer automatisk cleanup af udgåede filer

### 6. Test scenarios

**Test 1: Clean build**

- Alle 3,295 filer krypteres til cache
- Alle filer kopieres fra cache til src/main
- AssetCryptImpl.java genereres korrekt
- Build tid: ~5m 6s (ingen ændring)

**Test 2: Rebuild uden ændringer**

- 0 filer krypteres (incremental skip)
- Alle 3,295 filer kopieres fra cache til src/main (~2-3s)
- AssetCryptImpl.java genereres (identisk content)
- Build tid: ~10-15s (20-25s hurtigere)

**Test 3: Rebuild med én ændret fil**

- 1 fil re-krypteres til cache
- Alle 3,295 filer kopieres fra cache til src/main (~2-3s)
- AssetCryptImpl.java genereres med alle filer listede
- Build tid: ~12-18s (17-23s hurtigere)

**Test 4: Encryption toggle ændring**

- `requiresFullBuild()` returnerer true (encryptJS ændret)
- Alle filer re-krypteres
- Full rebuild trigges

**Test 5: ABI matrix ændring**

- `requiresFullBuild()` returnerer true (abis ændret)
- Alle filer re-krypteres med nye ABIs
- Full rebuild trigges

## Forventet Performance Forbedring

| Scenario | Før | Efter | Forbedring |

|----------|-----|-------|------------|

| First run | 5m 6s | 5m 6s | Ingen ændring |

| Second run (ingen ændringer) | 35s | ~10-15s | ~20-25s hurtigere |

| Second run (få filer ændret) | 35s | ~12-18s | ~17-23s hurtigere |

**Breakdown af second run efter:**

- Kryptering: 0s (skipped) eller ~1-2s (få filer)
- Copy fra cache: ~2-3s (3,295 små filer)
- AssetCryptImpl.java generation: <1s
- Gradle: ~6s (UP-TO-DATE)

## Kompatibilitet og Risici

### Bevaret kompatibilitet:

- `build.android.titaniumprep` hook kaldes stadig (ét samlet kald)
- `this.jsFilesToEncrypt` array bevares (bruges af generateRequireIndex)
- Hook signature uændret: `(exe, args, opts, next)`
- AssetCryptImpl.java output identisk

### Minimerede risici:

- Cache directory isoleret fra src/main (ingen race conditions)
- IncrementalFileTask er battle-tested (bruges allerede til JS/CSS/resources)
- Ét samlet hook-kald (ingen per-fil hooks = færre plugin breakage risks)
- State tracking i data.json (encryptJS, abis, version) sikrer konsistens

## Filer der ændres

1. **Ny fil:** `cli/lib/tasks/encrypt-js-task.js` (~300 linjer)
2. **Modificeres:** `android/cli/commands/_build.js`:

   - Tilføj `buildEncryptedAssetsDir` i initialization
   - Import EncryptJsTask (top)
   - Refactor encryptJSFiles() til cache + copy pattern (linje 2800-2854)
   - Opret generateAssetCryptImpl() metode (~30 linjer)

### To-dos

- [ ] Opret cli/lib/tasks/encrypt-js-task.js med IncrementalFileTask pattern
- [ ] Implementer doFullTaskRun() og doIncrementalTaskRun() metoder i EncryptJsTask
- [ ] Implementer processFile() metode der bruger Cloak til at kryptere filer
- [ ] Refactor encryptJSFiles() i _build.js til at bruge ny EncryptJsTask
- [ ] Ekstraher AssetCryptImpl.java generation til separat generateAssetCryptImpl() metode
- [ ] Bevare build.android.titaniumprep hook kompatibilitet i ny implementation
- [ ] Test clean build, rebuild uden ændringer, rebuild med få ændringer