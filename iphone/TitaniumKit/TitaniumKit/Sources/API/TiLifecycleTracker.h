/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import <Foundation/Foundation.h>

#ifdef DEBUG

/**
 * Debug tracker for monitoring proxy and view lifecycle
 * Helps identify memory leaks and lifecycle issues during development
 */
@interface TiLifecycleTracker : NSObject

/**
 * Get the shared instance
 */
+ (instancetype)sharedTracker;

/**
 * Track proxy creation
 */
+ (void)trackProxyCreated:(id)proxy className:(NSString *)className;

/**
 * Track proxy destruction
 */
+ (void)trackProxyDestroyed:(id)proxy className:(NSString *)className;

/**
 * Track view creation
 */
+ (void)trackViewCreated:(id)view className:(NSString *)className;

/**
 * Track view destruction
 */
+ (void)trackViewDestroyed:(id)view className:(NSString *)className;

/**
 * Print current stats
 */
+ (void)printStats;

/**
 * Get current proxy count
 */
+ (NSInteger)liveProxyCount;

/**
 * Get current view count
 */
+ (NSInteger)liveViewCount;

/**
 * Reset all counters
 */
+ (void)reset;

@end

// Convenience macros
#define TRACK_PROXY_CREATED(proxy, className) [TiLifecycleTracker trackProxyCreated:proxy className:className]
#define TRACK_PROXY_DESTROYED(proxy, className) [TiLifecycleTracker trackProxyDestroyed:proxy className:className]
#define TRACK_VIEW_CREATED(view, className) [TiLifecycleTracker trackViewCreated:view className:className]
#define TRACK_VIEW_DESTROYED(view, className) [TiLifecycleTracker trackViewDestroyed:view className:className]
#define PRINT_LIFECYCLE_STATS() [TiLifecycleTracker printStats]

#else

// No-op macros for release builds
#define TRACK_PROXY_CREATED(proxy, className)
#define TRACK_PROXY_DESTROYED(proxy, className)
#define TRACK_VIEW_CREATED(view, className)
#define TRACK_VIEW_DESTROYED(view, className)
#define PRINT_LIFECYCLE_STATS()

#endif