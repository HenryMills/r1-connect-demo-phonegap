#import "R1ConnectPlugin.h"

@interface R1ConnectPlugin (LocationService)

- (void) addLocationObservers;
- (void) removeLocationObservers;

- (void) observeLocationValueForKeyPath:(NSString *)keyPath;

@end
