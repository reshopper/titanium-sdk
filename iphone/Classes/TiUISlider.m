/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#ifdef USE_TI_UISLIDER

#import "TiUISlider.h"
#import "TiUISliderProxy.h"
#import <TitaniumKit/ImageLoader.h>
#import <TitaniumKit/TiUtils.h>

@implementation TiUISlider

#ifdef TI_USE_AUTOLAYOUT
- (void)initializeTiLayoutView
{
  [super initializeTiLayoutView];
  [self setDefaultHeight:TiDimensionAutoSize];
  [self setDefaultWidth:TiDimensionAutoFill];
}
#endif

- (void)dealloc
{
  [sliderView removeTarget:self action:@selector(sliderChanged:) forControlEvents:UIControlEventValueChanged];
  [sliderView removeTarget:self action:@selector(sliderBegin:) forControlEvents:UIControlEventTouchDown];
  [sliderView removeTarget:self action:@selector(sliderEnd:) forControlEvents:(UIControlEventTouchUpInside | UIControlEventTouchUpOutside | UIControlEventTouchCancel)];
  RELEASE_TO_NIL(sliderView);
  RELEASE_TO_NIL(lastTouchUp);
  RELEASE_TO_NIL(steps);
  [super dealloc];
}

- (void)layoutSubviews
{
  [super layoutSubviews];
  [sliderView setFrame:[self bounds]];
}

- (UISlider *)sliderView
{
  if (sliderView == nil) {
    sliderView = [[UISlider alloc] initWithFrame:[self bounds]];

    // We have to do this to force the slider subviews to appear, in the case where value<=min==0.
    // If the slider doesn't register a value change (or already have its subviews drawn in a nib) then
    // it will NEVER draw them.
    [sliderView setValue:0.1 animated:NO];
    [sliderView setValue:0 animated:NO];

    [sliderView addTarget:self action:@selector(sliderChanged:) forControlEvents:UIControlEventValueChanged];
    [sliderView addTarget:self action:@selector(sliderBegin:) forControlEvents:UIControlEventTouchDown];
    [sliderView addTarget:self action:@selector(sliderEnd:) forControlEvents:(UIControlEventTouchUpInside | UIControlEventTouchUpOutside | UIControlEventTouchCancel)];
    [self addSubview:sliderView];
    lastTouchUp = [[NSDate alloc] init];
    lastTimeInterval = 1.0; // Short-circuit so that we don't ignore the first fire

    thumbImageState = UIControlStateNormal;
    leftTrackImageState = UIControlStateNormal;
    rightTrackImageState = UIControlStateNormal;

    // Initialize lastFiredValue
    lastFiredValue = NAN;
  }
  return sliderView;
}

- (id)accessibilityElement
{
  return [self sliderView];
}

- (BOOL)hasTouchableListener
{
  // since this guy only works with touch events, we always want them
  // just always return YES no matter what listeners we have registered
  return YES;
}

- (void)setThumb:(id)value forState:(UIControlState)state
{
  [[self sliderView] setThumbImage:[TiUtils image:value proxy:[self proxy]] forState:state];
}

- (void)setRightTrack:(id)value forState:(UIControlState)state
{
  NSURL *url = [TiUtils toURL:value proxy:[self proxy]];
  if (url == nil) {
    DebugLog(@"[WARN] could not find image: %@", [url absoluteString]);
    return;
  }

  UIImage *ret = [[ImageLoader sharedLoader] loadImmediateStretchableImage:url withLeftCap:rightTrackLeftCap topCap:rightTrackTopCap];

  [[self sliderView] setMaximumTrackImage:ret forState:state];
}

- (void)setLeftTrack:(id)value forState:(UIControlState)state
{
  NSURL *url = [TiUtils toURL:value proxy:[self proxy]];
  if (url == nil) {
    DebugLog(@"[WARN] could not find image: %@", [url absoluteString]);
    return;
  }

  UIImage *ret = [[ImageLoader sharedLoader] loadImmediateStretchableImage:url withLeftCap:leftTrackLeftCap topCap:leftTrackTopCap];

  [[self sliderView] setMinimumTrackImage:ret forState:state];
}

#pragma mark View controller stuff

- (void)setThumbImage_:(id)value
{
  [self setThumb:value forState:UIControlStateNormal];

  if ((thumbImageState & UIControlStateSelected) == 0) {
    [self setThumb:value forState:UIControlStateSelected];
  }
  if ((thumbImageState & UIControlStateHighlighted) == 0) {
    [self setThumb:value forState:UIControlStateHighlighted];
  }
  if ((thumbImageState & UIControlStateDisabled) == 0) {
    [self setThumb:value forState:UIControlStateDisabled];
  }
}

- (void)setSelectedThumbImage_:(id)value
{
  [self setThumb:value forState:UIControlStateSelected];
  thumbImageState = thumbImageState | UIControlStateSelected;
}

- (void)setHighlightedThumbImage_:(id)value
{
  [self setThumb:value forState:UIControlStateHighlighted];
  thumbImageState = thumbImageState | UIControlStateHighlighted;
}

- (void)setDisabledThumbImage_:(id)value
{
  [self setThumb:value forState:UIControlStateDisabled];
  thumbImageState = thumbImageState | UIControlStateSelected;
}

- (void)setLeftTrackImage_:(id)value
{
  [self setLeftTrack:value forState:UIControlStateNormal];

  if ((leftTrackImageState & UIControlStateSelected) == 0) {
    [self setLeftTrack:value forState:UIControlStateSelected];
  }
  if ((leftTrackImageState & UIControlStateHighlighted) == 0) {
    [self setLeftTrack:value forState:UIControlStateHighlighted];
  }
  if ((leftTrackImageState & UIControlStateDisabled) == 0) {
    [self setLeftTrack:value forState:UIControlStateDisabled];
  }
}

- (void)setSelectedLeftTrackImage_:(id)value
{
  [self setLeftTrack:value forState:UIControlStateSelected];
  leftTrackImageState = leftTrackImageState | UIControlStateSelected;
}

- (void)setHighlightedLeftTrackImage_:(id)value
{
  [self setLeftTrack:value forState:UIControlStateHighlighted];
  leftTrackImageState = leftTrackImageState | UIControlStateHighlighted;
}

- (void)setDisabledLeftTrackImage_:(id)value
{
  [self setLeftTrack:value forState:UIControlStateDisabled];
  leftTrackImageState = leftTrackImageState | UIControlStateDisabled;
}

- (void)setRightTrackImage_:(id)value
{
  [self setRightTrack:value forState:UIControlStateNormal];

  if ((rightTrackImageState & UIControlStateSelected) == 0) {
    [self setRightTrack:value forState:UIControlStateSelected];
  }
  if ((rightTrackImageState & UIControlStateHighlighted) == 0) {
    [self setRightTrack:value forState:UIControlStateHighlighted];
  }
  if ((rightTrackImageState & UIControlStateDisabled) == 0) {
    [self setRightTrack:value forState:UIControlStateDisabled];
  }
}

- (void)setSelectedRightTrackImage_:(id)value
{
  [self setRightTrack:value forState:UIControlStateSelected];
  rightTrackImageState = rightTrackImageState | UIControlStateSelected;
}

- (void)setHighlightedRightTrackImage_:(id)value
{
  [self setRightTrack:value forState:UIControlStateHighlighted];
  rightTrackImageState = rightTrackImageState | UIControlStateHighlighted;
}

- (void)setDisabledRightTrackImage_:(id)value
{
  [self setRightTrack:value forState:UIControlStateDisabled];
  rightTrackImageState = rightTrackImageState | UIControlStateDisabled;
}

- (void)setLeftTrackLeftCap_:(id)value
{
  leftTrackLeftCap = TiDimensionFromObject(value);
}
- (void)setLeftTrackTopCap_:(id)value
{
  leftTrackTopCap = TiDimensionFromObject(value);
}
- (void)setRightTrackLeftCap_:(id)value
{
  rightTrackLeftCap = TiDimensionFromObject(value);
}
- (void)setRightTrackTopCap_:(id)value
{
  rightTrackTopCap = TiDimensionFromObject(value);
}

- (void)setMin_:(id)value
{
  [[self sliderView] setMinimumValue:[TiUtils floatValue:value]];
}

- (void)setMax_:(id)value
{
  [[self sliderView] setMaximumValue:[TiUtils floatValue:value]];
}

- (void)_setValue:(id)value
{
  if ([value isKindOfClass:[NSNumber class]]) {
    [[self sliderView] setValue:[TiUtils floatValue:value] animated:NO];
  } else if ([value isKindOfClass:[NSArray class]]) {
    CGFloat newValue = [TiUtils floatValue:[value objectAtIndex:0]];
    NSDictionary *properties = (NSDictionary *)[value objectAtIndex:1];

    [[self sliderView] setValue:newValue animated:[TiUtils boolValue:@"animated" properties:properties def:NO]];
  }

  [self sliderChanged:[self sliderView] isTrusted:NO];
}

- (void)setEnabled_:(id)value
{
  [[self sliderView] setEnabled:[TiUtils boolValue:value]];
}

- (void)setTrackTintColor_:(id)value
{
  UIColor *newColor = [[TiUtils colorValue:value] _color];
  [[self sliderView] setMaximumTrackTintColor:newColor];
}

// Add steps support for iOS
- (void)setSteps_:(id)value
{
  if (value == nil) {
    RELEASE_TO_NIL(steps);
    snapToSteps = NO;
    return;
  }

  if ([value isKindOfClass:[NSArray class]]) {
    // Array of specific step values
    NSArray *stepArray = (NSArray *)value;
    NSMutableArray *stepValuesArray = [NSMutableArray arrayWithCapacity:[stepArray count]];

    for (id stepValue in stepArray) {
      [stepValuesArray addObject:[NSNumber numberWithFloat:[TiUtils floatValue:stepValue]]];
    }

    // Sort steps array to ensure proper ordering
    [stepValuesArray sortUsingComparator:^NSComparisonResult(NSNumber *a, NSNumber *b) {
      return [a compare:b];
    }];

    RELEASE_TO_NIL(steps);
    steps = [[NSArray arrayWithArray:stepValuesArray] retain];
    snapToSteps = [steps count] > 0;

    // Auto-set min/max from array bounds
    if (snapToSteps) {
      float minStep = [[steps firstObject] floatValue];
      float maxStep = [[steps lastObject] floatValue];
      [[self sliderView] setMinimumValue:minStep];
      [[self sliderView] setMaximumValue:maxStep];
    }

  } else if ([value isKindOfClass:[NSNumber class]]) {
    // Count of equal steps - auto-generate 0 to (stepCount-1)
    int stepCount = [TiUtils intValue:value];
    if (stepCount > 1) {
      NSMutableArray *stepValuesArray = [NSMutableArray arrayWithCapacity:stepCount];

      // Generate steps from 0 to stepCount-1 (e.g., 7 steps = [0,1,2,3,4,5,6])
      for (int i = 0; i < stepCount; i++) {
        [stepValuesArray addObject:[NSNumber numberWithFloat:(float)i]];
      }

      RELEASE_TO_NIL(steps);
      steps = [[NSArray arrayWithArray:stepValuesArray] retain];
      snapToSteps = YES;

      // Auto-set min/max
      [[self sliderView] setMinimumValue:0];
      [[self sliderView] setMaximumValue:(float)(stepCount - 1)];
    } else {
      NSLog(@"[WARN] Step count must be greater than 1");
      RELEASE_TO_NIL(steps);
      snapToSteps = NO;
    }
  } else {
    NSLog(@"[WARN] Steps property must be an array of numbers or a step count number");
    RELEASE_TO_NIL(steps);
    snapToSteps = NO;
  }
}

- (float)findNearestStep:(float)value
{
  if (!snapToSteps || steps == nil || [steps count] == 0) {
    return value;
  }

  float nearestStep = [[steps objectAtIndex:0] floatValue];
  float minDistance = fabsf(value - nearestStep);

  for (NSNumber *step in steps) {
    float stepValue = [step floatValue];
    float distance = fabsf(value - stepValue);
    if (distance < minDistance) {
      minDistance = distance;
      nearestStep = stepValue;
    }
  }

  return nearestStep;
}

// Find which step index a value corresponds to
- (int)findStepIndex:(float)value
{
  if (!snapToSteps || steps == nil || [steps count] == 0) {
    return 0;
  }

  for (int i = 0; i < [steps count]; i++) {
    float stepValue = [[steps objectAtIndex:i] floatValue];
    if (fabsf(value - stepValue) < 0.001f) {
      return i;
    }
  }

  return 0;
}

// Only change step when we've actually REACHED/CROSSED into a new step
// Not just when we're closer to it
- (float)findStepForValue:(float)value fromCurrentStep:(float)currentStep
{
  if (!snapToSteps || steps == nil || [steps count] == 0) {
    return value;
  }

  // First time - find initial step based on value
  if (isnan(currentStep)) {
    return [self findNearestStep:value];
  }

  // Find index of current step
  int currentIndex = [self findStepIndex:currentStep];

  // Check if we've reached the next step (moving right/up)
  if (currentIndex < (int)[steps count] - 1) {
    float nextStep = [[steps objectAtIndex:currentIndex + 1] floatValue];
    if (value >= nextStep) {
      // We've reached or passed the next step
      // But check if we've gone even further (skipped steps)
      return [self findStepForValue:value fromCurrentStep:nextStep];
    }
  }

  // Check if we've reached the previous step (moving left/down)
  if (currentIndex > 0) {
    float prevStep = [[steps objectAtIndex:currentIndex - 1] floatValue];
    if (value <= prevStep) {
      // We've reached or passed the previous step
      // But check if we've gone even further (skipped steps)
      return [self findStepForValue:value fromCurrentStep:prevStep];
    }
  }

  // Haven't crossed into a new step - stay on current
  return currentStep;
}

- (CGFloat)verifyHeight:(CGFloat)suggestedHeight
{
  CGFloat result = [[self sliderView] sizeThatFits:CGSizeZero].height;

  // iOS 7 DP3 sizeThatFits always returns zero for regular slider
  if (result == 0) {
    result = 30.0;
  }
  return result;
}

USE_PROXY_FOR_VERIFY_AUTORESIZING

#pragma mark Delegates

- (void)sliderChanged:(id)sender
{
  [self sliderChanged:sender isTrusted:YES];
}

- (void)sliderChanged:(id)sender isTrusted:(BOOL)isTrusted
{
  UISlider *slider = (UISlider *)sender;
  float currentValue = [slider value];
  float valueToReport = currentValue;

  // If steps are enabled, only change step when we've actually REACHED a new step
  // Not just when we're closer to it
  if (snapToSteps && [steps count] > 0) {
    valueToReport = [self findStepForValue:currentValue fromCurrentStep:lastFiredValue];
  }

  // Only fire change event if step actually changed
  BOOL shouldFireEvent = isnan(lastFiredValue) || fabsf(valueToReport - lastFiredValue) > 0.001f;

  if (shouldFireEvent) {
    lastFiredValue = valueToReport;

    // Determine the value to return - step index or actual value
    float returnValue = valueToReport;
    if (stepValues && snapToSteps) {
      returnValue = [self findNearestStepIndex:valueToReport];
    }

    NSNumber *newValue = [NSNumber numberWithFloat:returnValue];
    [self.proxy replaceValue:newValue forKey:@"value" notification:NO];

    if ([self.proxy _hasListeners:@"change"]) {
      [self.proxy fireEvent:@"change" withObject:[NSDictionary dictionaryWithObjectsAndKeys:newValue, @"value", NUMBOOL(isTrusted), @"isTrusted", nil]];
    }
  }
}

- (void)sliderBegin:(id)sender
{
  isTracking = YES;
  NSNumber *newValue = [NSNumber numberWithFloat:[(UISlider *)sender value]];
  if ([[self proxy] _hasListeners:@"touchstart"]) {
    [[self proxy] fireEvent:@"touchstart" withObject:[NSDictionary dictionaryWithObject:newValue forKey:@"value"]];
  }
  if ([[self proxy] _hasListeners:@"start"]) {
    [[self proxy] fireEvent:@"start" withObject:[NSDictionary dictionaryWithObject:newValue forKey:@"value"] propagate:NO reportSuccess:NO errorCode:0 message:nil];
  }
}

- (void)sliderEnd:(id)sender
{
  isTracking = NO;

  UISlider *slider = (UISlider *)sender;
  float finalValue = [slider value];
  float snappedValue = finalValue;

  // Snap to nearest step at release
  if (snapToSteps) {
    snappedValue = [self findNearestStep:finalValue];

    // Visually snap the slider
    if (fabsf(finalValue - snappedValue) > 0.001f) {
      [slider setValue:snappedValue animated:YES];
    }

    // Fire change event if snapped value is different from last fired value
    if (fabsf(snappedValue - lastFiredValue) > 0.001f) {
      lastFiredValue = snappedValue;

      float returnValue = snappedValue;
      if (stepValues) {
        returnValue = [self findNearestStepIndex:snappedValue];
      }

      NSNumber *newValue = [NSNumber numberWithFloat:returnValue];
      [self.proxy replaceValue:newValue forKey:@"value" notification:NO];

      if ([self.proxy _hasListeners:@"change"]) {
        [self.proxy fireEvent:@"change" withObject:[NSDictionary dictionaryWithObjectsAndKeys:newValue, @"value", NUMBOOL(YES), @"isTrusted", nil]];
      }
    }
  }

  // APPLE BUG: Sometimes in a double-click our 'UIControlEventTouchUpInside' event is fired more than once.  This is
  // ALWAYS indicated by a sub-0.1s difference between the clicks, and results in an additional fire of the event.
  // We have to track the PREVIOUS (not current) interval and prevent these ugly misfires!

  NSDate *now = [[NSDate alloc] init];
  NSTimeInterval currentTimeInterval = [now timeIntervalSinceDate:lastTouchUp];
  if (!(lastTimeInterval < 0.1 && currentTimeInterval < 0.1)) {
    float valueForEvent = snapToSteps ? snappedValue : finalValue;
    NSNumber *newValue = [NSNumber numberWithFloat:valueForEvent];
    if ([[self proxy] _hasListeners:@"touchend"]) {
      [[self proxy] fireEvent:@"touchend" withObject:[NSDictionary dictionaryWithObject:newValue forKey:@"value"]];
    }
    if ([[self proxy] _hasListeners:@"stop"]) {
      [[self proxy] fireEvent:@"stop" withObject:[NSDictionary dictionaryWithObject:newValue forKey:@"value"] propagate:NO reportSuccess:NO errorCode:0 message:nil];
    }
  }
  lastTimeInterval = currentTimeInterval;
  RELEASE_TO_NIL(lastTouchUp);
  lastTouchUp = now;
}

// Add stepValues property support
- (void)setStepValues_:(id)value
{
  stepValues = [TiUtils boolValue:value def:NO];
}

- (int)findNearestStepIndex:(float)value
{
  if (!snapToSteps || steps == nil || [steps count] == 0) {
    return 0;
  }

  int nearestIndex = 0;
  float minDistance = fabsf(value - [[steps objectAtIndex:0] floatValue]);

  for (int i = 1; i < [steps count]; i++) {
    float stepValue = [[steps objectAtIndex:i] floatValue];
    float distance = fabsf(value - stepValue);
    if (distance < minDistance) {
      minDistance = distance;
      nearestIndex = i;
    }
  }

  return nearestIndex;
}

@end

#endif
