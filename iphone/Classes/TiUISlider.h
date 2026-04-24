/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#ifdef USE_TI_UISLIDER

#import <TitaniumKit/TiUIView.h>

@interface TiUISlider : TiUIView <LayoutAutosizing> {
  @private
  UISlider *sliderView;
  NSDate *lastTouchUp;
  NSTimeInterval lastTimeInterval;

  UIControlState thumbImageState;
  UIControlState rightTrackImageState;
  UIControlState leftTrackImageState;
  TiDimension leftTrackLeftCap;
  TiDimension leftTrackTopCap;
  TiDimension rightTrackLeftCap;
  TiDimension rightTrackTopCap;

  // Steps support for iOS
  NSArray<NSNumber *> *steps;
  BOOL snapToSteps;
  // Track last fired value to prevent duplicate events
  float lastFiredValue;
  // Return step indices instead of values when enabled
  BOOL stepValues;
  // Track if user is currently dragging
  BOOL isTracking;
}

/**
 * Internal method used to trigger the value-change from the proxy instead.
 * This is required in order to handle complex arguments, e.g. number and
 * animated-flag in one command.
 */
- (void)_setValue:(id)value;

@end

#endif
