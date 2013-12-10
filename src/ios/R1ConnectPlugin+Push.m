#import "R1ConnectPlugin.h"
#import "R1ConnectPlugin+Common.h"

#import "R1SDK.h"
#import "R1Push.h"

@implementation R1ConnectPlugin (Push)

BOOL _hasPushObservers;

- (void) addPushObservers
{
    if (_hasPushObservers)
        return;
    
    _hasPushObservers = YES;
    
    [[R1Push sharedInstance] addObserver:self forKeyPath:@"deviceToken" options:0 context:nil];
}

- (void) removePushObservers
{
    if (!_hasPushObservers)
        return;
    
    _hasPushObservers = NO;
    
    [[R1Push sharedInstance] removeObserver:self forKeyPath:@"deviceToken" context:nil];
}

- (void) observePushValueForKeyPath:(NSString *)keyPath
{
    NSString *notification = nil;
    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    
    if ([keyPath isEqualToString:@"deviceToken"])
    {
        notification = @"deviceToken";
        
        if ([R1Push sharedInstance].deviceToken == nil)
            [data setObject:[NSNull null] forKey:@"deviceToken"];
        else
            [data setObject:[R1Push sharedInstance].deviceToken forKey:@"deviceToken"];
    }
    
    if (notification == nil)
        return;
    
    [self sendNotificationFromService:@"R1Push" notification:notification info:data];
}


- (void) handleForegroundNotification:(NSDictionary *)notification
{
    [self sendNotificationFromService:@"R1Push" notification:@"foregroundNotification" info:notification];
}

- (void) handleBackgroundNotification:(NSDictionary *)notification
{
    [self performSelector:@selector(sendBackgroundNotification:) withObject:notification afterDelay:0.5];
}

- (void) sendBackgroundNotification:(NSDictionary *)notification
{
    [self sendNotificationFromService:@"R1Push" notification:@"backgroundNotification" info:notification];
}

- (void) push_isStarted:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withBool:[R1Push sharedInstance].isStarted];
    }];
}

- (void) push_setPushEnabled:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1Push sharedInstance].pushEnabled = [self getBoolFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) push_isPushEnabled:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withBool:[R1Push sharedInstance].pushEnabled];
    }];
}

- (void) push_getDeviceToken:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withString:[R1Push sharedInstance].deviceToken];
    }];
}

- (void) push_setBadgeNumber:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        [R1Push sharedInstance].badgeNumber = [self getIntegerFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) push_getBadgeNumber:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withInteger:[R1Push sharedInstance].badgeNumber];
    }];
}

- (void) push_getTags:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:[R1Push sharedInstance].tags.tags];
    
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:command.callbackId];
    }];
}

- (void) push_setTags:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        NSArray *newTags = [self getArrayFromCommand:command parameterIndex:0];
        for (NSString *newTag in newTags)
        {
            if (![newTag isKindOfClass:[NSString class]])
            {
                [self sendWrongParameterToCommand:command];
                return;
            }
        }
        
        [R1Push sharedInstance].tags.tags = newTags;
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) push_addTag:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        NSString *tagName = [self getStringFromCommand:command parameterIndex:0];
        if (tagName == nil)
        {
            [self sendWrongParameterToCommand:command];
            return;
        }
        
        [[R1Push sharedInstance].tags addTag:tagName];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) push_removeTag:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        NSString *tagName = [self getStringFromCommand:command parameterIndex:0];
        if (tagName == nil)
        {
            [self sendWrongParameterToCommand:command];
            return;
        }
        
        [[R1Push sharedInstance].tags removeTag:tagName];
        
        [self sendOkResultToCommand:command];
    }];
}


@end
