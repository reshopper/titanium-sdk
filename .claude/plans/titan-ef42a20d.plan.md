<!-- ef42a20d-b1a7-4a60-84b4-06b72bcf18b4 c7c310fa-4a64-4a78-b4c1-d854b3cc6286 -->
# Titanium OTA (iOS+Android) med HTTPS, SHA-256 og Ed25519

### Omfang og mål

- Formål: Sikker OTA af JS/asset-bundle uden app store release.
- Begrænsning: Kun JS/assets. Ingen native kode, moduler, permissions eller `tiapp.xml` via OTA.

### Arkitektur (kort)

- Server: Host zip’et bundle + `manifest.json` (version, binary-range, SHA-256, Ed25519-signatur).
- Klient: Bootstrap ved app-start henter manifest, downloader zip, verificerer, installerer atomisk og kører en single `app.bundle.js`. Fallback/rollback ved fejl.
- Bundling: TypeScript → CommonJS (tsc). Micro‑bundler i CI samler CJS-moduler i én `app.bundle.js` (ingen webpack/rollup).
- Inspiration: Plug‑and‑play som RN’s hot update (download, verifikation, atomisk aktivering, rollback), men implementeret i Titanium’s JS‑lag fremfor native bundle‑switch. Se referenceideen i `react-native-ota-hot-update` (download/rollback/crash‑guard). [Link](https://github.com/vantuan88291/react-native-ota-hot-update).

### Modulært design: `ti.ota` (plug‑and‑play)

- Integration: Tilføj `require('ota/boot')` øverst i `Resources/app.js`.
- Offentligt API (foreslået):
  - `configure(options)`, `check()`, `download()`, `verify()`, `install()`, `activate()`
  - `markHealthy()`, `rollback()`, `getState()`
  - Events: `updateAvailable`, `downloadProgress`, `installed`, `activated`, `rolledBack`, `error`
- Intern struktur:
  - `downloader.js` (HTTPS, retry/backoff, failover)
  - `verify.js` (SHA‑256, Ed25519 keyring)
  - `installer.js` (unzip, atomisk switch, cleanup, disk‑quota)
  - `state.js` (aktiv/forrige version, crash counter, healthy mark)
  - `runtime.js` (eval af single bundle, assetUrlResolver)
  - `boot.js` (sekvensering, staged rollout, kanaler, fallback)

### Lifecycle‑eksempel (kort)

```javascript
// Resources/app.js
const OTA = require('ota/boot');

OTA.initialize({
  manifestUrl: 'https://cdn.example.com/prod/manifest.json',
  channel: 'production',
  publicKeys: { main: '<ed25519-public-key-hex>' }
}, function onReady(runtime) {
  runtime.start();
  const win = Ti.UI.createWindow({ backgroundColor: '#fff' });
  win.addEventListener('postlayout', () => OTA.markHealthy());
  win.open();
}, function onError(err) {
  Ti.API.error('OTA failed: ' + err.message);
  // Fallback til embedded app
});
```

### TypeScript → CJS → Micro‑bundler

- Udvikling: ESM i TypeScript. Transpilér til CJS med `tsc` (commonjs output, relative requires).
- Micro‑bundler (CI): Node‑kompatibelt (Bun valgfrit senere) script, der:
  - Resolver CJS `require()` grafen fra entry (typisk transpilet `app.js`/`index.js`).
  - Pakker moduler i registry `{id: (module, exports, require) => {...}}` + minimal runtime‑`require` (cache, cyc. deps).
  - Indlejrer `assetUrlResolver` (+ lille `Alloy`‑shim hvis Alloy bruges).
- Dynamiske imports: Frarådes; præ‑resolve til statiske requires i transpilering hvis nødvendigt.

### Alloy (hvis relevant)

- Pipeline: `alloy compile` → `tsc` → micro‑bundler.
- Aliaser: map `alloy/*` til compiled outputs.
- Shim: lille global `Alloy`‑shim i bundlen.

### Sikkerhed

- Integritet: SHA‑256 + Ed25519 (signatur over sha256). Strict afvisning ved mismatch.
- Key rotation: `publicKeyId` + app‑keyring (flere public keys). Rotation via ny binary eller (valgfrit) 2‑of‑N underskrift på særligt rotate‑manifest.
- Pinning (opt‑in): iOS `ti.urlsession`/custom; Android OkHttp/helper eller network‑security‑config.
- Risikominimering: 1–5% rollout, hurtig kill switch, (valgfrit) host‑allowlist før healthy mark.

### Drift

- CDN failover: Prioriteret liste af manifest‑URL’er + exponential backoff + cache/TTL af sidste gyldige manifest.
- Disk space: Max 2 bundles og/eller max MB; preflight; slet ældste før download.
- Metrics hooks: `check`, `downloadStart/End`, `verifyOk/Fail`, `installOk/Fail`, `activate`, `healthy`, `rollback` (+ versioner/varigheder/fejlkoder).

### Debug/developer mode

- `ota.config`: `debugMode`, `localManifestUrl`, `skipHealthyCheck`, verbose logging.
- Kun til lokal udvikling; signatur‑check kan slås fra i debug.

### Proof‑of‑Concept (Node‑first)

- Eval af bundlet JS i Titanium‑kontekst.
- SHA‑256 + Ed25519 verifikation i ren JS.
- Simpel healthy/rollback‑mekanisme (næste launch).
- Bemærk: Bun er valgfri og udskydes til senere verifikation; POC køres under Node for lav risiko.

### Build/packager (CI)

- `build/ota/packager.js` (Node):
  - Kør `alloy compile` (hvis relevant) → `tsc` → micro‑bundler → zip.
  - Beregn sha256, signer Ed25519 (tweetnacl), generér manifest, upload til CDN.
  - Kanaler/staged rollout.
- Bun (valgfri, senere): Kør packager under Bun og sammenlign output‑hash mod Node for determinisme.

### Test og verifikation

- Unit: verify/signatur, state/rollback, keyring/rotation, asset resolver.
- Integration: offline, korrupt zip, forkert signatur, crash‑on‑first‑run, rollback.
- QA: TestFlight/Play Internal med 1–5% rollout, øges gradvist.

### Leverancer i repo

- `docs/ota/README.md`
- `build/ota/packager.js` (+ npm scripts)
- `templates/app/ota/*` (inkl. Alloy‑guide)
- `tests/Resources/ota-demo/*`

### Tidsestimat

- POC: 1–2 dage
- Klient‑OTA (JS): 3–5 dage
- CI/packager, docs, templates, tests/QA: 3–4 dage
- Total: 7–10 dage

## To‑dos (prioriteret)

### Phase 1: POC (Node‑only)

- [ ] Eval bundlet JS i Titanium context
- [ ] SHA‑256 + Ed25519 verifikation i JS
- [ ] Healthy mark + rollback på næste launch

### Phase 2: Klient

- [ ] Implementér `state.js` (aktiv/forrige, healthy, crashes)
- [ ] Implementér `verify.js` (SHA‑256, Ed25519, keyring, keyId)
- [ ] Implementér `installer.js` (JS‑unzip, atomisk switch, cleanup, quota)
- [ ] Implementér `downloader.js` (HTTPS, retry/backoff, progress, failover)
- [ ] Implementér `runtime.js` (eval, assetUrlResolver, feature flag)
- [ ] Implementér `boot.js` (boot‑sekvens, rollback‑gate, kanaler, staged rollout)

### Phase 3: Tooling

- [ ] Design `manifest.json` + validering (incl. schemaVersion/minBundleVersion/deprecationDate)
- [ ] Definér `ota.config` og keyring (flere public keys)
- [ ] Implementér `build/ota/packager.js` (micro‑bundler, zip, sha256, ed25519 sign)
- [ ] Alloy‑pipeline + `Alloy`‑shim og aliaser (kun hvis Alloy)

### Phase 4: Polish

- [ ] Metrics hooks + event‑skema
- [ ] CDN failover + manifest TTL/cache
- [ ] Templates + `docs/ota/README.md` (inkl. Alloy‑guide)
- [ ] Integrationstests + demo‑scenarier

### Valgfrit (efter stabil POC)

- [ ] Kør packager under Bun og sammenlign output‑hash mod Node (determinisme)
- [ ] Tilføj runtime‑detektion i packager (Node/Bun)

### To-dos

- [ ] Byg POC: eval bundlet JS i Titanium context
- [ ] Byg POC: SHA-256 + Ed25519 verifikation i JS
- [ ] Byg POC: healthy mark + rollback på næste launch
- [ ] Definér ota.config og keyring (flere public keys)
- [ ] Design manifest.json og validering
- [ ] Implementér build/ota/packager.js (zip, sha256, sign)
- [ ] Implementér downloader.js (HTTPS, retry/backoff, progress, failover)
- [ ] Implementér verify.js (SHA-256, Ed25519, keyring)
- [ ] Implementér installer.js (JS-unzip, atomisk switch, cleanup, quota)
- [ ] Implementér state.js (aktiv/forrige, healthy, crashes)
- [ ] Implementér runtime.js (eval, assetUrlResolver, healthy API)
- [ ] Implementér boot.js (sequential boot, rollback gate, staged rollout)
- [ ] Alloy compile pipeline, aliaser og Alloy-shim
- [ ] Detekter Ti.include og lav migrations-check/polyfill
- [ ] Metrics hooks og event-skema
- [ ] CDN failover og manifest TTL/cache
- [ ] Templates og docs/ota/README.md inkl. Alloy-guide
- [ ] Integrationstests og demo-scenarier