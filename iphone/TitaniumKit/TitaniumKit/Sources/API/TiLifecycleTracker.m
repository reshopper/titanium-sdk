/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiLifecycleTracker.h"

#ifdef DEBUG

@implementation TiLifecycleTracker {
  NSMutableDictionary *_proxyClassCounts;
  NSMutableDictionary *_viewClassCounts;
  NSInteger _totalProxyCount;
  NSInteger _totalViewCount;
  NSLock *_lock;
}

+ (instancetype)sharedTracker
{
  static TiLifecycleTracker *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [[self alloc] init];
  });
  return sharedInstance;
}

- (instancetype)init
{
  if (self = [super init]) {
    _proxyClassCounts = [[NSMutableDictionary alloc] init];
    _viewClassCounts = [[NSMutableDictionary alloc] init];
    _totalProxyCount = 0;
    _totalViewCount = 0;
    _lock = [[NSLock alloc] init];
  }
  return self;
}

- (void)dealloc
{
  [_proxyClassCounts release];
  [_viewClassCounts release];
  [_lock release];
  [super dealloc];
}

+ (void)trackProxyCreated:(id)proxy className:(NSString *)className
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  NSInteger count = [[tracker->_proxyClassCounts objectForKey:className] integerValue];
  [tracker->_proxyClassCounts setObject:@(count + 1) forKey:className];
  tracker->_totalProxyCount++;

  NSLog(@"[LIFECYCLE] PROXY CREATED: %@ (%@) - Total: %ld", className, proxy, (long)tracker->_totalProxyCount);

  [tracker->_lock unlock];
}

+ (void)trackProxyDestroyed:(id)proxy className:(NSString *)className
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  NSInteger count = [[tracker->_proxyClassCounts objectForKey:className] integerValue];
  if (count > 0) {
    [tracker->_proxyClassCounts setObject:@(count - 1) forKey:className];
    tracker->_totalProxyCount--;
  }

  NSLog(@"[LIFECYCLE] PROXY DESTROYED: %@ (%@) - Total: %ld", className, proxy, (long)tracker->_totalProxyCount);

  [tracker->_lock unlock];
}

+ (void)trackViewCreated:(id)view className:(NSString *)className
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  NSInteger count = [[tracker->_viewClassCounts objectForKey:className] integerValue];
  [tracker->_viewClassCounts setObject:@(count + 1) forKey:className];
  tracker->_totalViewCount++;

  NSLog(@"[LIFECYCLE] VIEW CREATED: %@ (%@) - Total: %ld", className, view, (long)tracker->_totalViewCount);

  [tracker->_lock unlock];
}

+ (void)trackViewDestroyed:(id)view className:(NSString *)className
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  NSInteger count = [[tracker->_viewClassCounts objectForKey:className] integerValue];
  if (count > 0) {
    [tracker->_viewClassCounts setObject:@(count - 1) forKey:className];
    tracker->_totalViewCount--;
  }

  NSLog(@"[LIFECYCLE] VIEW DESTROYED: %@ (%@) - Total: %ld", className, view, (long)tracker->_totalViewCount);

  [tracker->_lock unlock];
}

+ (void)printStats
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  NSLog(@"=== LIFECYCLE STATS ===");
  NSLog(@"Total Proxies: %ld", (long)tracker->_totalProxyCount);
  NSLog(@"Total Views: %ld", (long)tracker->_totalViewCount);

  NSLog(@"Proxy breakdown:");
  for (NSString *className in tracker->_proxyClassCounts) {
    NSInteger count = [[tracker->_proxyClassCounts objectForKey:className] integerValue];
    if (count > 0) {
      NSLog(@"  %@: %ld", className, (long)count);
    }
  }

  NSLog(@"View breakdown:");
  for (NSString *className in tracker->_viewClassCounts) {
    NSInteger count = [[tracker->_viewClassCounts objectForKey:className] integerValue];
    if (count > 0) {
      NSLog(@"  %@: %ld", className, (long)count);
    }
  }
  NSLog(@"=======================");

  [tracker->_lock unlock];
}

+ (NSInteger)liveProxyCount
{
  return [self sharedTracker]->_totalProxyCount;
}

+ (NSInteger)liveViewCount
{
  return [self sharedTracker]->_totalViewCount;
}

+ (void)reset
{
  TiLifecycleTracker *tracker = [self sharedTracker];
  [tracker->_lock lock];

  [tracker->_proxyClassCounts removeAllObjects];
  [tracker->_viewClassCounts removeAllObjects];
  tracker->_totalProxyCount = 0;
  tracker->_totalViewCount = 0;

  NSLog(@"[LIFECYCLE] Stats reset");

  [tracker->_lock unlock];
}

@end

#endif