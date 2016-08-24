#import "AppDelegate+R1Connect.h"
#import <objc/runtime.h>
#import "R1Push.h"

static char launchNotificationKey;

@implementation AppDelegate (R1Connect)

- (id) getCommandInstance:(NSString*)className
{
    return [self.viewController getCommandInstance:className];
}

+ (void)load
{
    Method original, swizzled;
    
    original = class_getInstanceMethod(self, @selector(init));
    swizzled = class_getInstanceMethod(self, @selector(swizzled_init));
    method_exchangeImplementations(original, swizzled);
}

- (AppDelegate *)swizzled_init
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(createNotificationChecker:)
                                                 name:UIApplicationDidFinishLaunchingNotification object:nil];
    
    return [self swizzled_init];
}

- (void)createNotificationChecker:(NSNotification *)notification
{
    if (notification)
    {
        NSDictionary *launchOptions = [notification userInfo];
        if (launchOptions)
        {
            NSDictionary *remoteNotification = [launchOptions objectForKey: UIApplicationLaunchOptionsRemoteNotificationKey];

            if ([R1Push sharedInstance].isStarted)
            {
                if (remoteNotification != nil)
                {
                    [[R1Push sharedInstance] handleNotification:remoteNotification
                                               applicationState:[UIApplication sharedApplication].applicationState];
                }
            }else
                self.launchNotification = remoteNotification;
        }
        
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidFinishLaunchingNotification object:nil];
    }
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    if ([R1Push sharedInstance].isStarted)
        [[R1Push sharedInstance] registerDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    if ([R1Push sharedInstance].isStarted)
        [[R1Push sharedInstance] failToRegisterDeviceTokenWithError:error];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    if ([R1Push sharedInstance].isStarted)
        [[R1Push sharedInstance] handleNotification:userInfo
                                   applicationState:application.applicationState];
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    return [self application:application
                     openURL:url
                     options:sourceApplication ? @{@"UIApplicationOpenURLOptionsSourceApplicationKey":sourceApplication} : @{}];
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<NSString*, id> *)options
{
    R1SDK *sdk = [R1SDK sharedInstance];
    [sdk openURL:url];

    return YES;
}

- (NSMutableArray *)launchNotification
{
    return objc_getAssociatedObject(self, &launchNotificationKey);
}

- (void)setLaunchNotification:(NSDictionary *)aDictionary
{
    objc_setAssociatedObject(self, &launchNotificationKey, aDictionary, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void) dealloc
{
    self.launchNotification        = nil; // clear the association and release the object
}

@end