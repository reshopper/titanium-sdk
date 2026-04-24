/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#if defined(USE_TI_UINAVIGATIONWINDOW)

#import "TiUINavigationWindowProxy.h"
#import "TiUINavigationWindowInternal.h"
#import <TitaniumKit/KrollPromise.h>
#import <TitaniumKit/TiApp.h>

@implementation TiUINavigationWindowProxy

- (void)_destroy
{
  if (fullWidthBackGestureRecognizer != nil) {
    [fullWidthBackGestureRecognizer setDelegate:nil];
    [navController.view removeGestureRecognizer:fullWidthBackGestureRecognizer];
  }

  RELEASE_TO_NIL(rootWindow);
  RELEASE_TO_NIL(navController);
  RELEASE_TO_NIL(current);
  RELEASE_TO_NIL(fullWidthBackGestureRecognizer);
  RELEASE_TO_NIL(insertedWindows);

  [super _destroy];
}

- (void)_initWithProperties:(NSDictionary *)properties
{
  [super _initWithProperties:properties];
}

- (NSString *)apiName
{
  return @"Ti.UI.NavigationWindow";
}

- (NSArray *)windows
{
  NSMutableArray *windowProxies = [NSMutableArray array];
  for (UIViewController *viewController in [navController viewControllers]) {
    if ([viewController isKindOfClass:[TiViewController class]]) {
      TiViewProxy *proxy = [(TiViewController *)viewController proxy];
      if (proxy != nil && [proxy isKindOfClass:[TiWindowProxy class]]) {
        [windowProxies addObject:proxy];
      }
    }
  }
  return windowProxies;
}

- (void)popGestureStateHandler:(UIGestureRecognizer *)recognizer
{
  UIGestureRecognizerState curState = recognizer.state;

  switch (curState) {
  case UIGestureRecognizerStateBegan:
    transitionWithGesture = YES;
    break;
  case UIGestureRecognizerStateEnded:
  case UIGestureRecognizerStateCancelled:
  case UIGestureRecognizerStateFailed:
    transitionWithGesture = NO;
    break;
  default:
    break;
  }
}

#pragma mark - TiOrientationController

- (TiOrientationFlags)orientationFlags
{
  if ([self isModal]) {
    return [super orientationFlags];
  } else {
    for (id thisController in [[navController viewControllers] reverseObjectEnumerator]) {
      if (![thisController isKindOfClass:[TiViewController class]]) {
        continue;
      }
      TiWindowProxy *thisProxy = (TiWindowProxy *)[(TiViewController *)thisController proxy];
      if ([thisProxy conformsToProtocol:@protocol(TiOrientationController)]) {
        TiOrientationFlags result = [thisProxy orientationFlags];
        if (result != TiOrientationNone) {
          return result;
        }
      }
    }
    return _supportedOrientations;
  }
}

#pragma mark - TiTab Protocol

- (id)tabGroup
{
  return nil;
}

- (UINavigationController *)controller
{
  if (navController == nil) {
    navController = [[UINavigationController alloc] initWithRootViewController:[self rootController]];
    navController.delegate = self;
    [TiUtils configureController:navController withObject:self];
    [navController.interactivePopGestureRecognizer addTarget:self action:@selector(popGestureStateHandler:)];
    [[navController interactivePopGestureRecognizer] setDelegate:self];
  }
  return navController;
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
  // Check if current window is the first (root) in the navigation stack
  UIViewController *firstController = [[navController viewControllers] firstObject];
  BOOL isRootWindow = NO;
  if ([firstController isKindOfClass:[TiViewController class]]) {
    TiViewProxy *firstProxy = [(TiViewController *)firstController proxy];
    isRootWindow = (firstProxy == current);
  }

  if (current != nil && !isRootWindow) {
    return [TiUtils boolValue:[current valueForKey:@"swipeToClose"] def:YES];
  }
  return !isRootWindow;
}

- (KrollPromise *)openWindow:(NSArray *)args
{
  TiWindowProxy *window = [args objectAtIndex:0];
  ENSURE_TYPE(window, TiWindowProxy);

  JSContext *context = [self currentContext];

  if (window == rootWindow) {
    [rootWindow windowWillOpen];
    [rootWindow windowDidOpen];
    return [KrollPromise resolved:@[] inContext:context];
  }

  // If no root window exists yet, set this window as root and update the nav controller.
  if (rootWindow == nil) {
    rootWindow = [window retain];
    [rootWindow setIsManaged:YES];
    [rootWindow setTab:(TiViewProxy<TiTab> *)self];
    [rootWindow setParentOrientationController:self];
    [rootWindow open:nil];

    if (navController != nil) {
      TiThreadPerformOnMainThread(
          ^{
            [navController setViewControllers:@[ [rootWindow hostingController] ] animated:NO];
          },
          YES);
    }

    [rootWindow windowWillOpen];
    [rootWindow windowDidOpen];
    return [KrollPromise resolved:@[] inContext:context];
  }

  [window setIsManaged:YES];
  [window setTab:(TiViewProxy<TiTab> *)self];
  [window setParentOrientationController:self];
  // Send to open. Will come back after _handleOpen returns true.
  if (![window opening]) {
    args = ([args count] > 1) ? [args objectAtIndex:1] : nil;
    if (args != nil) {
      args = [NSArray arrayWithObject:args];
    }
    return [window open:args]; // return underlying promise
  }

  KrollPromise *promise = [[[KrollPromise alloc] initInContext:context] autorelease];
  BOOL animated = args != nil && [args count] > 1 ? [TiUtils boolValue:@"animated" properties:[args objectAtIndex:1] def:YES] : YES;
  [[[TiApp app] controller] dismissKeyboard];
  TiThreadPerformOnMainThread(
      ^{
        [self pushOnUIThread:@[ window, [NSNumber numberWithBool:animated], promise ]];
      },
      YES);
  return promise;
}

- (KrollPromise *)closeWindow:(NSArray *)args
{
  TiWindowProxy *window = [args objectAtIndex:0];
  ENSURE_TYPE(window, TiWindowProxy);

  JSContext *context = [self currentContext];

  // Check if this window is the first (root) in the navigation stack
  // We check the actual stack position rather than the original rootWindow variable
  // because insertWindow can change which window is at position 0
  UIViewController *firstController = [[navController viewControllers] firstObject];
  BOOL isCurrentRoot = NO;
  if ([firstController isKindOfClass:[TiViewController class]]) {
    TiViewProxy *firstProxy = [(TiViewController *)firstController proxy];
    isCurrentRoot = (firstProxy == window);
  }

  if (isCurrentRoot && ![[TiApp app] willTerminate]) {
    DebugLog(@"[ERROR] Can not close the root window of the NavigationWindow. Close the NavigationWindow instead.");
    return [KrollPromise rejectedWithErrorMessage:@"Can not close the root window of the NavigationWindow. Close the NavigationWindow instead." inContext:context];
  }

  KrollPromise *promise = [[[KrollPromise alloc] initInContext:context] autorelease];
  BOOL animated = args != nil && [args count] > 1 ? [TiUtils boolValue:@"animated" properties:[args objectAtIndex:1] def:YES] : YES;
  TiThreadPerformOnMainThread(
      ^{
        [self popOnUIThread:@[ window, [NSNumber numberWithBool:animated], promise ]];
      },
      YES);
  return promise;
}

- (void)popToRootWindow:(id)args
{
  ENSURE_SINGLE_ARG_OR_NIL(args, NSDictionary);

  TiThreadPerformOnMainThread(
      ^{
        [navController popToRootViewControllerAnimated:[TiUtils boolValue:@"animated" properties:args def:NO]];
      },
      YES);
}

- (KrollPromise *)insertWindow:(NSArray *)args
{
  TiWindowProxy *window = [args objectAtIndex:0];
  ENSURE_TYPE(window, TiWindowProxy);

  NSNumber *indexNum = [args objectAtIndex:1];
  ENSURE_TYPE(indexNum, NSNumber);
  NSInteger index = [indexNum integerValue];

  JSContext *context = [self currentContext];
  KrollPromise *promise = [[KrollPromise alloc] initInContext:context];

  // Retain promise for async use (window is retained by the block capture)
  [promise retain];

  TiThreadPerformOnMainThread(
      ^{
        [self insertWindowOnUIThread:window atIndex:index promise:promise retryCount:0];
      },
      NO);

  return [promise autorelease];
}

- (void)insertWindowOnUIThread:(TiWindowProxy *)window atIndex:(NSInteger)index promise:(KrollPromise *)promise retryCount:(NSInteger)retryCount
{
  // Use [self controller] to ensure navController is initialized (lazy loading)
  UINavigationController *nav = [self controller];

  DebugLog(@"[DEBUG] insertWindowOnUIThread called (retry: %ld) - transitionIsAnimating: %d, transitionWithGesture: %d, navController: %@",
      (long)retryCount, transitionIsAnimating, transitionWithGesture, nav);

  // Wait for any ongoing transition, but give up after 50 retries (5 seconds)
  if ((transitionIsAnimating || transitionWithGesture) && retryCount < 50) {
    DebugLog(@"[DEBUG] insertWindowOnUIThread: waiting for transition to complete (retry %ld)...", (long)retryCount);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
      [self insertWindowOnUIThread:window atIndex:index promise:promise retryCount:retryCount + 1];
    });
    return;
  }

  // Check if navController is still nil (should not happen after calling [self controller])
  if (!nav) {
    NSLog(@"[ERROR] insertWindowOnUIThread: navController is nil, cannot insert window");
    if (promise != nil) {
      [promise rejectWithErrorMessage:@"NavigationWindow is not open"];
    }
    [promise release];
    return;
  }

  // If we've been waiting too long, log a warning but proceed anyway
  if (retryCount >= 50) {
    NSLog(@"[WARN] insertWindowOnUIThread: timed out waiting for transition, proceeding anyway (transitionIsAnimating: %d, transitionWithGesture: %d)",
        transitionIsAnimating, transitionWithGesture);
    // Reset the flags to allow the insert
    transitionIsAnimating = NO;
    transitionWithGesture = NO;
  }

  DebugLog(@"[DEBUG] insertWindowOnUIThread: proceeding with insert");

  @try {
    // Check if window is already in the stack
    UIViewController *windowController = [window hostingController];
    NSArray *currentControllers = [nav viewControllers];
    DebugLog(@"[DEBUG] insertWindowOnUIThread: current stack size: %lu", (unsigned long)[currentControllers count]);

    if ([currentControllers containsObject:windowController]) {
      NSLog(@"[WARN] Window is already in the navigation stack. Skipping insert.");
      if (promise != nil) {
        [promise rejectWithErrorMessage:@"Window is already in the navigation stack"];
      }
      [promise release];
      return;
    }

    // Prepare the window (same as openWindow does)
    [window setIsManaged:YES];
    [window setTab:(TiViewProxy<TiTab> *)self];
    [window setParentOrientationController:self];

    DebugLog(@"[DEBUG] insertWindowOnUIThread: triggering window lifecycle");

    // Trigger window lifecycle - this initializes the view
    [window windowWillOpen];
    [window windowDidOpen];

    // Force load the view to ensure it's fully initialized
    // This prevents crashes when UIKit queries the view controller
    [windowController loadViewIfNeeded];

    // Get current stack and insert at the desired position
    NSMutableArray *controllers = [NSMutableArray arrayWithArray:currentControllers];

    // Validate index bounds
    NSInteger adjustedIndex = index;
    if (adjustedIndex < 0) {
      adjustedIndex = 0;
    }
    if (adjustedIndex > (NSInteger)[controllers count]) {
      adjustedIndex = [controllers count];
    }

    DebugLog(@"[DEBUG] insertWindowOnUIThread: inserting at index %ld (requested: %ld)", (long)adjustedIndex, (long)index);

    // Insert at the desired position
    [controllers insertObject:windowController atIndex:adjustedIndex];

    // Apply the new stack without animation
    [nav setViewControllers:controllers animated:NO];

    DebugLog(@"[DEBUG] insertWindowOnUIThread: stack updated, new size: %lu", (unsigned long)[[nav viewControllers] count]);

    // Retain the window in our array since TiViewController doesn't retain its proxy
    if (insertedWindows == nil) {
      insertedWindows = [[NSMutableArray alloc] init];
    }
    [insertedWindows addObject:window];

    // After setViewControllers:animated:NO, UIKit may not call didShowViewController
    // for the current top window. We need to ensure the top window maintains focus.
    UIViewController *topVC = [nav topViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
      TiViewProxy *topProxy = [(TiViewController *)topVC proxy];
      if (topProxy != nil && [topProxy isKindOfClass:[TiWindowProxy class]]) {
        TiWindowProxy *topWindow = (TiWindowProxy *)topProxy;
        // Update current to the top window
        RELEASE_TO_NIL(current);
        current = [topWindow retain];
        // Ensure the top window has focus (gainFocus checks internally if already focused)
        if (focussed) {
          [topWindow gainFocus];
        }
      }
    }

    DebugLog(@"[DEBUG] insertWindowOnUIThread: resolving promise");
    if (promise != nil) {
      [promise resolve:@[]];
      DebugLog(@"[DEBUG] insertWindowOnUIThread: promise resolved");
    }
  } @catch (NSException *ex) {
    NSLog(@"[ERROR] insertWindow failed: %@", ex.description);
    if (promise != nil) {
      [promise rejectWithErrorMessage:ex.description];
    }
  }

  // Release promise (window is now retained in insertedWindows array)
  [promise release];
  DebugLog(@"[DEBUG] insertWindowOnUIThread: complete");
}

- (void)windowClosing:(TiWindowProxy *)window animated:(BOOL)animated
{
  // NO OP NOW
}

#pragma mark - UINavigationControllerDelegate

#ifdef USE_TI_UIIOSTRANSITIONANIMATION
- (id<UIViewControllerAnimatedTransitioning>)navigationController:(UINavigationController *)navigationController
                                  animationControllerForOperation:(UINavigationControllerOperation)operation
                                               fromViewController:(UIViewController *)fromVC
                                                 toViewController:(UIViewController *)toVC
{
  if ([toVC isKindOfClass:[TiViewController class]]) {
    TiViewController *toViewController = (TiViewController *)toVC;
    if ([[toViewController proxy] isKindOfClass:[TiWindowProxy class]]) {
      TiWindowProxy *windowProxy = (TiWindowProxy *)[toViewController proxy];
      return [windowProxy transitionAnimation];
    }
  }
  return nil;
}
#endif

- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
  if ([TiUtils isIOSVersionOrGreater:@"11.2"]) {
    navigationController.navigationBar.tintAdjustmentMode = UIViewTintAdjustmentModeNormal;
    navigationController.navigationBar.tintAdjustmentMode = UIViewTintAdjustmentModeAutomatic;
  }

  if (!transitionWithGesture) {
    transitionIsAnimating = YES;
  }
  if (current != nil) {
    UIViewController *curController = [current hostingController];
    NSArray *curStack = [navController viewControllers];
    BOOL winclosing = NO;
    if (![curStack containsObject:curController]) {
      winclosing = YES;
    } else {
      NSUInteger curIndex = [curStack indexOfObject:curController];
      if (curIndex > 1) {
        UIViewController *currentPopsTo = [curStack objectAtIndex:(curIndex - 1)];
        if (currentPopsTo == viewController) {
          winclosing = YES;
        }
      }
    }
    if (winclosing) {
      // TIMOB-15033. Have to call windowWillClose so any keyboardFocussedProxies resign
      // as first responders. This is ok since tab is not nil so no message will be sent to
      // hosting controller.
      [current windowWillClose];
    }
  }
  TiWindowProxy *theWindow = (TiWindowProxy *)[(TiViewController *)viewController proxy];
  if ((theWindow != rootWindow) && [theWindow opening]) {
    [theWindow windowWillOpen];
    [theWindow windowDidOpen];
  }
  navController.view.backgroundColor = theWindow.view.backgroundColor;
}

- (void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
  transitionIsAnimating = NO;
  transitionWithGesture = NO;
  if (current != nil) {
    UIViewController *oldController = [current hostingController];

    if (![[navController viewControllers] containsObject:oldController]) {
      [current setTab:nil];
      [current setParentOrientationController:nil];
      [current close:nil];
    }
  }
  RELEASE_TO_NIL(current);
  TiWindowProxy *theWindow = (TiWindowProxy *)[(TiViewController *)viewController proxy];
  current = [theWindow retain];
  [self childOrientationControllerChangedFlags:current];
  if (focussed) {
    [current gainFocus];
  }
}

#pragma mark - Private API

- (void)_setFrame:(CGRect)bounds
{
  if (navController != nil) {
    [[navController view] setFrame:bounds];
  }
}

- (UIViewController *)rootController
{
  if (rootWindow == nil) {
    id window = [self valueForKey:@"window"];
    if (window != nil && [window isKindOfClass:[TiWindowProxy class]]) {
      rootWindow = [window retain];
      [rootWindow setIsManaged:YES];
      [rootWindow setTab:(TiViewProxy<TiTab> *)self];
      [rootWindow setParentOrientationController:self];
      [rootWindow open:nil];
    } else {
      // No root window set yet. Create a temporary empty controller.
      // It will be replaced when openWindow is called.
      return [[[UIViewController alloc] init] autorelease];
    }
  }
  return [rootWindow hostingController];
}

- (void)pushOnUIThread:(NSArray *)args
{
  if (transitionIsAnimating || transitionWithGesture || !navController) {
    [self performSelector:_cmd withObject:args afterDelay:0.1];
    return;
  }
  if (!transitionWithGesture) {
    transitionIsAnimating = YES;
  }

  KrollPromise *promise = [args objectAtIndex:2];
  @try {
    TiWindowProxy *window = [args objectAtIndex:0];
    // Prevent UIKit  crashes when trying to push a window while it's already in the nav stack (e.g. on really slow devices)
    if ([[[self rootController].navigationController viewControllers] containsObject:window.hostingController]) {
      NSLog(@"[WARN] Trying to push a view controller that is already in the navigation window controller stack. Skipping open …");
      return;
    }

    BOOL animated = [[args objectAtIndex:1] boolValue];
    [navController pushViewController:[window hostingController] animated:animated];
    if (promise != nil) {
      [promise resolve:@[]];
    }
  } @catch (NSException *ex) {
    NSLog(@"[ERROR] %@", ex.description);
    if (promise != nil) {
      [promise rejectWithErrorMessage:ex.description];
    }
  }
}

- (void)popOnUIThread:(NSArray *)args
{
  if (transitionIsAnimating || transitionWithGesture) {
    [self performSelector:_cmd withObject:args afterDelay:0.1];
    return;
  }
  TiWindowProxy *window = [args objectAtIndex:0];
  BOOL animated = [[args objectAtIndex:1] boolValue];
  KrollPromise *promise = [args objectAtIndex:2];

  if (window == current) {
    if (animated && !transitionWithGesture) {
      transitionIsAnimating = YES;
    }
    [navController popViewControllerAnimated:animated];
    if (promise != nil) {
      [promise resolve:@[]];
    }
  } else {
    // FIXME: forward/chain the underlying promise from [window close:] done internally here rather than assume success
    [self closeWindow:window animated:NO];
    if (promise != nil) {
      [promise resolve:@[]];
    }
  }
}

- (void)closeWindow:(TiWindowProxy *)window animated:(BOOL)animated
{
  [window retain];
  UIViewController *windowController = [[window hostingController] retain];

  // Manage the navigation controller stack
  NSMutableArray *newControllerStack = [NSMutableArray arrayWithArray:[navController viewControllers]];
  [newControllerStack removeObject:windowController];
  [navController setViewControllers:newControllerStack animated:animated];
  [window setTab:nil];
  [window setParentOrientationController:nil];

  // Remove from inserted windows array if present
  if (insertedWindows != nil) {
    [insertedWindows removeObject:window];
  }

  // for this to work right, we need to sure that we always have the tab close the window
  // and not let the window simply close by itself. this will ensure that we tell the
  // tab that we're doing that
  [window close:nil];
  RELEASE_TO_NIL_AUTORELEASE(window);
  RELEASE_TO_NIL(windowController);
}

- (void)cleanNavStack
{
  TiThreadPerformOnMainThread(
      ^{
        if (navController != nil) {
          [navController setDelegate:nil];
          NSArray *currentControllers = [navController viewControllers];
          [navController setViewControllers:[NSArray array]];

          for (UIViewController *viewController in currentControllers) {
            // Safely check if this is a TiViewController with a valid proxy
            if (![viewController isKindOfClass:[TiViewController class]]) {
              continue;
            }
            TiViewProxy *proxy = [(TiViewController *)viewController proxy];
            if (proxy == nil || ![proxy isKindOfClass:[TiWindowProxy class]]) {
              continue;
            }
            TiWindowProxy *win = (TiWindowProxy *)proxy;
            [win setTab:nil];
            [win setParentOrientationController:nil];
            [[win close:nil] flush];
          }
          // Remove navigation controller from parent controller
          [navController willMoveToParentViewController:nil];
          [navController.view removeFromSuperview];
          [navController removeFromParentViewController];
          RELEASE_TO_NIL(navController);
          RELEASE_TO_NIL(rootWindow);
          RELEASE_TO_NIL(current);
          // Release inserted windows array
          RELEASE_TO_NIL(insertedWindows);
        }
      },
      YES);
}

#pragma mark - TiWindowProtocol
- (void)viewWillAppear:(BOOL)animated
{
  if (navController && [self viewAttached]) {
    UIViewController *parentController = [self windowHoldingController];
    [parentController addChildViewController:navController];
    [navController didMoveToParentViewController:parentController];
    [navController viewWillAppear:animated];
  }
  [super viewWillAppear:animated];
}
- (void)viewWillDisappear:(BOOL)animated
{
  if ([self viewAttached]) {
    [navController viewWillDisappear:animated];
  }
  [super viewWillDisappear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
  if ([self viewAttached]) {
    [navController viewDidAppear:animated];
  }
  [super viewDidAppear:animated];
}
- (void)viewDidDisappear:(BOOL)animated
{
  if ([self viewAttached]) {
    [navController viewDidDisappear:animated];
  }
  [super viewDidDisappear:animated];
}

- (BOOL)homeIndicatorAutoHide
{
  UIViewController *topVC = [navController topViewController];
  if ([topVC isKindOfClass:[TiViewController class]]) {
    TiViewProxy *theProxy = [(TiViewController *)topVC proxy];
    if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
      return [(id<TiWindowProtocol>)theProxy homeIndicatorAutoHide];
    }
  }
  return [super homeIndicatorAutoHide];
}

- (BOOL)hidesStatusBar
{
  UIViewController *topVC = [navController topViewController];
  if ([topVC isKindOfClass:[TiViewController class]]) {
    TiViewProxy *theProxy = [(TiViewController *)topVC proxy];
    if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
      return [(id<TiWindowProtocol>)theProxy hidesStatusBar];
    }
  }
  return [super hidesStatusBar];
}

- (UIStatusBarStyle)preferredStatusBarStyle;
{
  UIViewController *topVC = [navController topViewController];
  if ([topVC isKindOfClass:[TiViewController class]]) {
    TiViewProxy *theProxy = [(TiViewController *)topVC proxy];
    if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
      return [(id<TiWindowProtocol>)theProxy preferredStatusBarStyle];
    }
  }
  return [super preferredStatusBarStyle];
}

- (void)gainFocus
{
  UIViewController *topVC = [navController topViewController];
  if ([topVC isKindOfClass:[TiViewController class]]) {
    TiViewProxy *theProxy = [(TiViewController *)topVC proxy];
    if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
      [(id<TiWindowProtocol>)theProxy gainFocus];
    }
  }
  [super gainFocus];
}

- (void)resignFocus
{
  UIViewController *topVC = [navController topViewController];
  if ([topVC isKindOfClass:[TiViewController class]]) {
    TiViewProxy *theProxy = [(TiViewController *)topVC proxy];
    if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
      [(id<TiWindowProtocol>)theProxy resignFocus];
    }
  }
  [super resignFocus];
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
  if ([self viewAttached]) {
    [navController viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
  }
  [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
}

- (void)systemLayoutFittingSizeDidChangeForChildContentContainer:(id<UIContentContainer>)container
{
  if ([self viewAttached]) {
    [navController systemLayoutFittingSizeDidChangeForChildContentContainer:container];
  }
  [super systemLayoutFittingSizeDidChangeForChildContentContainer:container];
}

- (void)willTransitionToTraitCollection:(UITraitCollection *)newCollection withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
  if ([self viewAttached]) {
    [navController willTransitionToTraitCollection:newCollection withTransitionCoordinator:coordinator];
  }
  [super willTransitionToTraitCollection:newCollection withTransitionCoordinator:coordinator];
}

- (void)preferredContentSizeDidChangeForChildContentContainer:(id<UIContentContainer>)container
{
  if ([self viewAttached]) {
    [navController preferredContentSizeDidChangeForChildContentContainer:container];
  }
  [super preferredContentSizeDidChangeForChildContentContainer:container];
}

#pragma mark - TiViewProxy overrides
- (TiUIView *)newView
{
  CGRect frame = [self appFrame];
  TiUINavigationWindowInternal *win = [[TiUINavigationWindowInternal alloc] initWithFrame:frame];
  return win;
}

- (void)windowWillOpen
{
  UIView *nview = [[self controller] view];
  [nview setFrame:[[self view] bounds]];
  [[self view] addSubview:nview];
  [super windowWillOpen];
}

- (void)windowDidClose
{
  [self cleanNavStack];
  [super windowDidClose];
}

- (void)willChangeSize
{
  [super willChangeSize];

  // TODO: Shouldn't this be not through UI? Shouldn't we retain the windows ourselves?
  for (UIViewController *thisController in [navController viewControllers]) {
    if ([thisController isKindOfClass:[TiViewController class]]) {
      TiViewProxy *thisProxy = [(TiViewController *)thisController proxy];
      [thisProxy willChangeSize];
    }
  }
}

@end

#endif
