// Fil: TiUIView+TraitCollectionFix.m
#import "TiUIView+TraitCollectionFix.h"
#import <TitaniumKit/TiGradient.h>
#import <TitaniumKit/TiProxy.h>
#import <TitaniumKit/TiUIView.h>
#import <TitaniumKit/TiUtils.h>

@implementation TiUIView (TraitCollectionFix)
// This implementation will override the original in TiUIView
- (void)traitCollectionDidChange:(UITraitCollection *)previousTraitCollection
{
  //[super traitCollectionDidChange:previousTraitCollection];

  BOOL isInBackground = UIApplication.sharedApplication.applicationState == UIApplicationStateBackground;
  BOOL isDifferentColor = [self.traitCollection hasDifferentColorAppearanceComparedToTraitCollection:previousTraitCollection];

  if (!isDifferentColor || isInBackground) {
    return;
  }

  // Redraw the border color
  id borderColor = [self.proxy valueForKey:@"borderColor"];
  if (borderColor != nil) {
    [self refreshBorder:[TiUtils colorValue:borderColor]._color shouldRefreshWidth:NO];
  }

  // Redraw the view shadow color
  id viewShadowColor = [self.proxy valueForKey:@"viewShadowColor"];
  if (viewShadowColor != nil) {
    [self setViewShadowColor_:viewShadowColor];
  }

  TiGradient *backgroundGradient = [self.proxy valueForKey:@"backgroundGradient"];

  // Tjek om backgroundGradient er et TiGradient objekt før clearCache kaldes
  if (backgroundGradient != nil && backgroundGradient != [NSNull null] && [backgroundGradient isKindOfClass:[TiGradient class]]) {
    // Guard the colors to handle a case where gradients have no custom
    // colors applied
    id colors = [(TiGradient *)backgroundGradient valueForKey:@"colors"]; // Cast til TiGradient for at tilgå properties/metoder
    if (colors != nil) {
      [(TiGradient *)backgroundGradient clearCache]; // Nu sikkert at kalde clearCache
      [(TiGradient *)backgroundGradient setColors:colors];
      [self setBackgroundGradient_:(TiGradient *)backgroundGradient];
    }
  }
}

@end
