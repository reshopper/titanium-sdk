# Titanium SDK Lifecycle Tracking

Dette dokument beskriver den nye debug lifecycle tracking funktionalitet i Titanium SDK, som hjælper udviklere med at identificere memory leaks og forstå object lifecycle.

## Oversigt

Lifecycle tracking er et debug værktøj der holder styr på creation og destruction af proxyer og views i Titanium apps. Det er kun aktivt i debug builds og har zero performance impact i release builds.

## Android Implementation

### API Metoder

Tracking funktionaliteten er tilgængelig gennem `Ti.Platform` API'en:

```javascript
// Få antal live proxyer
const proxyCount = Ti.Platform.getLifecycleProxyCount();

// Få antal live views  
const viewCount = Ti.Platform.getLifecycleViewCount();

// Print detaljerede statistikker
Ti.Platform.printLifecycleStats();

// Reset alle counters
Ti.Platform.resetLifecycleStats();

// Få proxy ID for et view
const view = Ti.UI.createView({ id: 'mySpecialView' });
const proxyId = Ti.Platform.getViewProxyId(view);
Ti.API.info('View proxy ID: ' + proxyId);
```

### View/Proxy ID Tracking

Systemet holder styr på forskellige typer IDs for bedre debugging:

#### 🆔 **ID Typer**

1. **JavaScript Custom ID**: `view.id = 'myView'`
2. **Proxy ID**: Titanium internal ID (f.eks. `proxy$123`)
3. **Android View ID**: Native Android view ID (f.eks. `2131034`)
4. **Hash Code**: Object hash for unique identification

#### 📋 **ID Information i Logs**

```
[LIFECYCLE] VIEW CREATED: TiUILabel (hash: 67890) [androidId: 2131034] [proxyId: proxy$124] [customId: myLabel] - Total: 89
```

- `hash`: Object hash code
- `androidId`: Native Android view ID (kun Android)
- `proxyId`: Titanium proxy ID
- `customId`: JavaScript custom ID (hvis sat)

### Automatisk Tracking

Systemet tracker automatisk:
- **Proxy creation/destruction** i `KrollProxy.setupProxy()` og `release()`
- **View creation/destruction** i `TiUIView` constructor og `release()`

### Output Eksempel

```
[LIFECYCLE] PROXY CREATED: TiUIView (hash: 12345) [ID: proxy$123] [customId: myView] - Total: 156
[LIFECYCLE] VIEW CREATED: TiUILabel (hash: 67890) [androidId: 2131034] [proxyId: proxy$124] [customId: myLabel] - Total: 89

=== LIFECYCLE STATS ===
Total Proxies: 156
Total Views: 89
Proxy breakdown:
  TiUIView: 45
  TiUILabel: 23
  TiUIButton: 12
  TiWindowProxy: 8
View breakdown:
  TiUIView: 34
  TiUILabel: 23
  TiUIImageView: 15
=======================
```

## iOS Implementation

På iOS er der basic tracking tilgængeligt gennem den eksisterende `PROXY_MEMORY_TRACK` funktionalitet:

```objective-c
#if PROXY_MEMORY_TRACK == 1
    NSLog(@"[LIFECYCLE] PROXY CREATED: %@ - Total proxies: %ld", 
          NSStringFromClass([self class]), (long)proxyCount);
#endif
```

## Brug Cases

### 1. Memory Leak Detection

```javascript
// Tag initial counts
const initial = Ti.Platform.getLifecycleProxyCount();

// Udfør operationer der kan lække memory
createAndDestroyViews();

// Check for leaks
const final = Ti.Platform.getLifecycleProxyCount();
if (final > initial) {
    Ti.API.warn('Potential memory leak detected!');
    Ti.Platform.printLifecycleStats();
}
```

### 2. Performance Monitoring

```javascript
// Monitor object creation patterns
Ti.App.addEventListener('app:monitor', function() {
    Ti.Platform.printLifecycleStats();
});
```

### 3. Development Debugging

```javascript
// Print stats ved app lifecycle events
Ti.App.addEventListener('pause', function() {
    Ti.API.info('App paused - current object counts:');
    Ti.Platform.printLifecycleStats();
});
```

## Teknisk Implementation

### Android Classes

- **`KrollLifecycleTracker`** - Centraliseret tracking utility
- **`KrollProxy`** - Integreret tracking i `setupProxy()` og `release()`
- **`TiUIView`** - Integreret tracking i constructor og `release()`
- **`PlatformModule`** - Eksponerer API til JavaScript

### Thread Safety

Implementationen bruger:
- `AtomicInteger` for counters
- `synchronized` blocks for maps
- Thread-safe operations overalt

### Performance

- Zero overhead i release builds (`Log.isDebugModeEnabled()` check)
- Minimal overhead i debug builds
- Atomic operations for optimal performance

## Begrænsninger

1. **Android Only** - Fuld implementation kun på Android
2. **Debug Only** - Kun aktiv når debug mode er enabled
3. **No Historical Data** - Tracker kun current state, ikke historik

## Fremtidige Forbedringer

1. **iOS Full Implementation** - Komplet tracking på iOS
2. **Historical Tracking** - Gem data over tid
3. **Automatic Alerts** - Advarsler ved høje counts
4. **Web Dashboard** - Visual interface til tracking data
5. **Integration med Developer Tools** - Built-in debugging interface

## Konklusion

Lifecycle tracking gør det betydeligt nemmere at:
- Identificere memory leaks under udvikling
- Forstå object lifecycle patterns
- Debug performance problemer
- Optimere memory usage

Systemet er designet til at være non-intrusive og kun hjælpe under udvikling, uden at påvirke production performance. 