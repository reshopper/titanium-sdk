<rule>
name: titanium_controller_listener_cleanup
description: Ensure Titanium/global listeners are paired with proper teardown in controllers/libs
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "Ti\\.[A-Za-z]+\\.addEventListener\\("
actions:
  - type: suggest
    message: |
      Found global Titanium listener(s). Ensure you keep stable handler references and remove them during teardown.

      Recommended pattern (controller/lib):

      ```ts
      // keep stable refs
      const onAppPause = function onAppPause(e: any): void { /* ... */ }
      Ti.App.addEventListener('pause', onAppPause)

      export function teardown(): void {
        Ti.App.removeEventListener('pause', onAppPause)
      }

      // Alloy controller hint (if applicable)
      if ($ && typeof $.on === 'function') {
        $.on('close', teardown)
      }
      ```
</rule>

<rule>
name: titanium_controller_view_listeners_cleanup
description: Ensure view/proxy addEventListener have matching removeEventListener on close/teardown
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "\.addEventListener\\(.*\)"
actions:
  - type: suggest
    conditions:
      - pattern: "\.addEventListener\\(.*\)"
    message: |
      Event listeners on views/proxies should be removed to avoid leaks. Use named handlers and unregister them on teardown/close.

      ```ts
      const onClick = function onClick(e: any): void { /* ... */ }
      $.button.addEventListener('click', onClick)

      export function teardown(): void {
        $.button.removeEventListener('click', onClick)
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_controller_anonymous_handlers
description: Discourage anonymous inline handlers that cannot be removed cleanly
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "addEventListener\\\([^,]+,\\s*(function\\s*\\(|\\(.*\\)\\s*=>)"
actions:
  - type: suggest
    message: |
      Anonymous handlers make cleanup difficult. Assign handlers to named functions/consts so they can be removed via removeEventListener.

      ```ts
      // bad
      Ti.App.addEventListener('resume', function(e: any): void { /* ... */ })

      // good
      const onResume = function onResume(e: any): void { /* ... */ }
      Ti.App.addEventListener('resume', onResume)
      // later in teardown(): Ti.App.removeEventListener('resume', onResume)
      ```
</rule>

<rule>
name: titanium_controller_timers_cleanup
description: Ensure setTimeout/setInterval are cleared on teardown
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "set(Time|Interval)out?\\("
actions:
  - type: suggest
    message: |
      Timers should be stored and cleared during teardown to prevent background execution.

      ```ts
      let intervalId: ReturnType<typeof setInterval> | null = null
      intervalId = setInterval(tick, 1000)

      export function teardown(): void {
        if (intervalId) {
          clearInterval(intervalId)
          intervalId = null
        }
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_controller_xhr_cleanup
description: Ensure HTTPClient is aborted and callbacks cleared on teardown
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "Ti\\.Network\\.createHTTPClient\\("
actions:
  - type: suggest
    message: |
      Keep a reference to HTTPClient, abort pending requests and clear callbacks on teardown to avoid leaks.

      ```ts
      let client: Titanium.Network.HTTPClient | null = Ti.Network.createHTTPClient({
        onload: onLoad,
        onerror: onError,
        timeout: 10000
      })

      function onLoad(): void { /* ... */ }
      function onError(e: any): void { /* ... */ }

      export function teardown(): void {
        if (client) {
          try { client.abort() } catch (_) {}
          client.onload = null
          client.onerror = null
          client = null
        }
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_controller_media_cleanup
description: Ensure media resources are stopped/released and listeners removed
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "Ti\\.Media\\.create(AudioPlayer|Sound|VideoPlayer)"
actions:
  - type: suggest
    message: |
      Media resources should be stopped and released on teardown. Remove any attached listeners as well.

      ```ts
      let sound: Titanium.Media.Sound | null = Ti.Media.createSound({ url: 'file.wav' })

      export function teardown(): void {
        if (sound) {
          try { sound.stop() } catch (_) {}
          if (sound.removeEventListener) {
            // remove named listeners you added earlier
          }
          sound = null
        }
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_controller_missing_teardown
description: Recommend adding a teardown/destroy function when long-lived resources are detected
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "(Ti\\.[A-Za-z]+\\.addEventListener|setInterval\\(|setTimeout\\(|Ti\\.Network\\.createHTTPClient|Ti\\.Media\\.create(AudioPlayer|Sound|VideoPlayer))"
actions:
  - type: suggest
    conditions:
      - pattern: "(export\\s+function\\s+(teardown|destroy)\\s*\\(|export\\s+const\\s+(teardown|destroy)\\s*=|export\\s+default\\s+function\\s+(teardown|destroy)\\s*\\(|exports\\.(teardown|destroy)|module\\.exports\\.(teardown|destroy))"
        negate: true
    message: |
      This file uses long-lived resources but has no explicit teardown/destroy. Add one and unregister/clear resources there.

      ```ts
      export function teardown(): void {
        // remove listeners, clear timers, abort XHR, stop media, null references
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_alloy_close_binding_hint
description: For Alloy controllers, bind teardown to the top-level view's close event
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "\$\\.(on|off|trigger)|Alloy\\.createController|Alloy\\.createWidget|Alloy\\.CFG"
actions:
  - type: suggest
    message: |
      In Alloy controllers, hook teardown into the top-level view's 'close' to avoid leaks.

      ```ts
      export function teardown(): void { /* ... */ }
      if ($ && typeof $.on === 'function') {
        $.on('close', teardown)
      }
      ```
</rule>

<rule>
name: titanium_controller_teardown_return_type
description: Ensure exported teardown has explicit void return type in TypeScript
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "export\\s+(function\\s+teardown|const\\s+teardown)"
actions:
  - type: suggest
    conditions:
      - pattern: "export\\s+function\\s+teardown\\s*\(.*\)\s*:\\s*void"
        negate: true
      - pattern: "export\\s+const\\s+teardown\\s*=\\s*\(.*\)\\s*:\\s*void"
        negate: true
    message: |
      Prefer an explicit `void` return type on teardown to make the API clear and help tooling.

      ```ts
      export function teardown(): void {
        // cleanup
      }
      // or
      export const teardown: () => void = () => {
        // cleanup
      }
      ```
</rule>

<rule>
name: titanium_controller_listener_pairing_check
description: Ensure files with addEventListener also include corresponding removeEventListener (heuristic)
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "\.addEventListener\("
actions:
  - type: suggest
    conditions:
      - pattern: "removeEventListener\("
        negate: true
    message: |
      This file contains one or more `addEventListener` calls but no `removeEventListener`. Ensure each added listener is removed during teardown using the same handler reference.

      Recommended TypeScript pattern:

      ```ts
      const onResume = (e: any): void => { /* ... */ }
      Ti.App.addEventListener('resume', onResume)

      export function teardown(): void {
        Ti.App.removeEventListener('resume', onResume)
      }
      if ($ && typeof $.on === 'function') $.on('close', teardown)
      ```
</rule>

<rule>
name: titanium_controller_listener_pairing_per_event
description: Heuristically ensure each addEventListener(event, handler) has matching removeEventListener(event, handler)
filters:
  - type: file_extension
    pattern: "\.(ts|tsx)$"
  - type: content
    pattern: "\.addEventListener\(\s*(['\"][^'\"]+['\"])\s*,\s*([A-Za-z_$][A-Za-z0-9_$]*)\s*\)"
actions:
  - type: suggest
    conditions:
      - pattern: "\.removeEventListener\(\s*\1\s*,\s*\2\s*\)"
        negate: true
    message: |
      Listener appears to be added with event {1} and handler {2} but no corresponding `removeEventListener({1}, {2})` was found. Add removal in `teardown()`.

      Example:
      ```ts
      const {2} = (e: any): void => { /* ... */ }
      // ... later
      export function teardown(): void {
        someTarget.removeEventListener({1}, {2})
      }
      ```
</rule>
