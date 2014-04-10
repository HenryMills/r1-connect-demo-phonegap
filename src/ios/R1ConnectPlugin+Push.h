#import "R1ConnectPlugin.h"

@interface R1ConnectPlugin (Push)

- (void) addPushObservers;
- (void) removePushObservers;

- (void) observePushValueForKeyPath:(NSString *)keyPath;

@end
