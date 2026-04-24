# Fix for ANR in JavaObject::isWeak() during Property Access

## Problem Description

An ANR (Application Not Responding) was occurring when accessing view properties from JavaScript, particularly in timer callbacks or frequently executed code. The issue manifested as a deadlock in the main thread.

### Stack Trace Analysis

```
"main" tid=1 Native
  #04  pc 0x0000000000840178  titanium::JavaObject::isWeak+44
  #05  pc 0x00000000008400e8  titanium::JavaObject::getJavaObject+16
  #06  pc 0x000000000088cc08  titanium::TiViewProxy::getter_height+428
```

The main thread was blocked waiting on a condition variable during a JNI call from `JavaObject::isWeak()` to `ReferenceTable.isStrongReference()`.

### Root Causes

1. **Expensive JNI Call in Hot Path**: The `isWeak()` method was making a JNI call to Java on every property access, even though the weak/strong state is already tracked by the `isWeakRef_` member variable.

2. **Unsynchronized HashMap Access**: The `ReferenceTable.references` HashMap was being accessed from multiple threads without proper synchronization, leading to potential race conditions and deadlocks.

3. **Deadlock Risk**: When the main thread holds locks and makes a JNI call, it can create a deadlock if the Java side is waiting on a resource held by another thread.

## Solution

### 1. Cached Weak State (JavaObject.cpp)

Changed `JavaObject::isWeak()` to rely solely on the cached `isWeakRef_` member variable instead of making expensive JNI calls:

```cpp
bool JavaObject::isWeak()
{
    // Only rely on our cached state to avoid expensive JNI calls from hot paths (property getters)
    // The isWeakRef_ flag is set explicitly in MakeJavaWeak() and cleared in MakeJavaStrong()
    return isWeakRef_;
}
```

**Benefits:**
- Eliminates JNI call from property getter hot path
- Prevents potential deadlocks from cross-boundary calls
- Improves performance significantly for property access

**Safety:**
- The `isWeakRef_` flag is already correctly managed by `MakeJavaWeak()` and `MakeJavaStrong()`
- No change in semantics - just using the existing cached state

### 2. Thread-Safe ReferenceTable (ReferenceTable.java)

Added `synchronized` keyword to all public methods in `ReferenceTable`:

- `createReference()`
- `destroyReference()`
- `makeWeakReference()`
- `makeSoftReference()`
- `clearReference()`
- `getReference()`
- `isStrongReference()`

**Benefits:**
- Prevents race conditions on HashMap access
- Ensures atomic operations on reference state changes
- Prevents potential corruption of the references map

**Performance Impact:**
- Minimal, as these operations are not in the hot path anymore (due to fix #1)
- The synchronization overhead is acceptable for correctness

### 3. Optimized isStrongReference()

Changed `isStrongReference()` to avoid unnecessary unwrapping:

```java
public static synchronized boolean isStrongReference(long key)
{
    Object ref = references.get(key);
    // Don't unwrap weak references - just check the type directly
    return !(ref instanceof Reference);
}
```

**Benefits:**
- Avoids calling `.get()` on WeakReference which could trigger GC
- More efficient type checking

## Impact

### Before
- ANRs occurring when accessing view properties from timers
- Main thread blocking on JNI calls
- Potential deadlocks in multi-threaded scenarios

### After
- No JNI calls during property access (cached state)
- Thread-safe reference management
- Significantly reduced ANR risk
- Better performance for property getters

## Testing Recommendations

1. **Stress Test Property Access**: Create timers that rapidly access view properties
2. **Multi-threaded Access**: Test concurrent access to proxies from different threads
3. **GC Behavior**: Verify that weak reference handling still works correctly
4. **Memory Leaks**: Ensure no references are leaked due to synchronization changes

## Related Files

- `android/runtime/v8/src/native/JavaObject.cpp`
- `android/runtime/v8/src/java/org/appcelerator/kroll/runtime/v8/ReferenceTable.java`

## Migration Notes

This is a **backward compatible** fix that doesn't change any APIs or behavior - it only fixes the internal implementation to prevent deadlocks and improve performance.
