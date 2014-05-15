#import "R1ConnectPlugin.h"
#import "R1ConnectPlugin+LocationService.h"
#import "R1ConnectPlugin+Common.h"
#import "R1ConnectPlugin+Push.h"

#import "AppDelegate+R1Connect.h"

#import "R1SDK.h"
#import "R1Push.h"
#import "R1Emitter.h"

#define APP_ID_SETTINGS_KEY @"com.radiumone.r1connect.applicationid"
#define CLIENT_KEY_SETTINGS_KEY @"com.radiumone.r1connect.clientkey"

@implementation R1ConnectPlugin

- (void) pluginInitialize
{
    NSDictionary *settings = self.commandDelegate.settings;

    R1SDK *sdk = [R1SDK sharedInstance];
    
    NSString *applicationId = [settings valueForKey:APP_ID_SETTINGS_KEY];
    NSString *clientKey = [settings valueForKey:CLIENT_KEY_SETTINGS_KEY];

    if (applicationId == nil)
        return;
    
    sdk.applicationId = applicationId;
    sdk.clientKey = clientKey;
    
    sdk.push.delegate = (id)self;

    [sdk start];
    
    [self addLocationObservers];

    if (!sdk.push.isStarted)
        return;
    
    [self addPushObservers];
    
    UIApplication *application = [UIApplication sharedApplication];
    AppDelegate *appDelegate = (AppDelegate *)[self appDelegate];

    if (appDelegate.launchNotification != nil)
    {
        [sdk.push handleNotification:appDelegate.launchNotification
                    applicationState:application.applicationState];
    }
    
    [sdk.push registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge |
                                                                 UIRemoteNotificationTypeSound |
                                                                 UIRemoteNotificationTypeAlert)];
}

- (void) dealloc
{
    [self removePushObservers];
    [self removeLocationObservers];
}

- (void) observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (object == [R1SDK sharedInstance].locationService)
    {
        [self observeLocationValueForKeyPath:keyPath];
        
        return;
    }

    if (object == [R1Push sharedInstance])
    {
        [self observePushValueForKeyPath:keyPath];
        
        return;
    }
    
    [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
}


- (void) isStarted:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        BOOL started = [R1Push sharedInstance].isStarted || [R1Emitter sharedInstance].isStarted;
    
        [self sendOkResultToCommand:command withBool:started];
    }];
}

- (void) setApplicationUserId:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1SDK sharedInstance].applicationUserId = [self getStringFromCommand:command parameterIndex:0];

        [self sendOkResultToCommand:command];
    }];
}

- (void) getApplicationUserId:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withString:[R1SDK sharedInstance].applicationUserId];
    }];
}

- (void) setAdvertisingEnabled:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1SDK sharedInstance].advertisingEnabled = [self getBoolFromCommand:command parameterIndex:0];

        [self sendOkResultToCommand:command];
    }];
}

- (void) isAdvertisingEnabled:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withBool:[R1SDK sharedInstance].advertisingEnabled];
    }];
}

@end
