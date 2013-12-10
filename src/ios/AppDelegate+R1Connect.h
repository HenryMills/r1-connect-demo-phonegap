#import <Foundation/Foundation.h>

#import "AppDelegate.h"

@interface AppDelegate (R1Connect)

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo;

@property (nonatomic, retain) NSDictionary        *launchNotification;

@end