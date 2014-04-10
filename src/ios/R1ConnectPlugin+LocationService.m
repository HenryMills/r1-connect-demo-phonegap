#import "R1ConnectPlugin+LocationService.h"
#import "R1ConnectPlugin+Common.h"
#import "R1SDK.h"
#import "R1LocationService.h"

@implementation R1ConnectPlugin (LocationService)

BOOL _hasLocationObservers;

- (void) addLocationObservers
{
    if (_hasLocationObservers)
        return;
    
    _hasLocationObservers = YES;
    
    [[R1SDK sharedInstance].locationService addObserver:self forKeyPath:@"lastLocation" options:0 context:nil];
    [[R1SDK sharedInstance].locationService addObserver:self forKeyPath:@"state" options:0 context:nil];
}

- (void) removeLocationObservers
{
    if (!_hasLocationObservers)
        return;
    
    _hasLocationObservers = NO;
    
    [[R1SDK sharedInstance].locationService removeObserver:self forKeyPath:@"lastLocation" context:nil];
    [[R1SDK sharedInstance].locationService removeObserver:self forKeyPath:@"state" context:nil];
}

- (void) observeLocationValueForKeyPath:(NSString *)keyPath
{
    NSString *notification = nil;
    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    
    if ([keyPath isEqualToString:@"state"])
    {
        notification = @"state";
        
        [data setObject:[self stringLocationState:[R1SDK sharedInstance].locationService.state] forKey:@"state"];
    }else if ([keyPath isEqualToString:@"lastLocation"])
    {
        notification = @"coordinate";
        
        if ([R1SDK sharedInstance].locationService.lastLocation != nil)
        {
            CLLocationCoordinate2D coordinate = [R1SDK sharedInstance].locationService.lastLocation.coordinate;
            [data setObject:[NSNumber numberWithDouble:coordinate.latitude] forKey:@"latitude"];
            [data setObject:[NSNumber numberWithDouble:coordinate.longitude] forKey:@"longitude"];
        }
    }
    
    if (notification == nil)
        return;
    
    [self sendNotificationFromService:@"R1LocationService" notification:notification info:data];
}

- (NSString *) stringLocationState:(R1LocationServiceState) state
{
    switch (state)
    {
        case R1LocationServiceStateDisabled:
            return @"Disabled";
        case R1LocationServiceStateOff:
            return @"Off";
        case R1LocationServiceStateSearching:
            return @"Searching";
        case R1LocationServiceStateHasLocation:
            return @"Has Location";
        case R1LocationServiceStateWaitNextUpdate:
            return @"Wait Next Update";
            
        default:
            break;
    }
    
    return @"Unknown";
}

- (void) location_setEnabled:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        [R1SDK sharedInstance].locationService.enabled = [self getBoolFromCommand:command parameterIndex:0];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) location_isEnabled:(CDVInvokedUrlCommand *)command
{
    [self sendOkResultToCommand:command
                       withBool:[R1SDK sharedInstance].locationService.enabled];
}

- (void) location_setAutoupdateTimeout:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1SDK sharedInstance].locationService.autoupdateTimeout = [self getIntegerFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) location_getAutoupdateTimeout:(CDVInvokedUrlCommand *)command
{
    [self sendOkResultToCommand:command withInteger:[R1SDK sharedInstance].locationService.autoupdateTimeout];
}

- (void) location_getState:(CDVInvokedUrlCommand *)command
{
    [self sendOkResultToCommand:command withString:[self stringLocationState:[R1SDK sharedInstance].locationService.state]];
}

- (void) location_getCoordinate:(CDVInvokedUrlCommand *)command
{
    if ([R1SDK sharedInstance].locationService.lastLocation == nil)
        [self sendOkResultToCommand:command];

    CLLocationCoordinate2D coordinate = [R1SDK sharedInstance].locationService.lastLocation.coordinate;
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"latitude":[NSNumber numberWithDouble:coordinate.latitude],
                                                                                                                @"longitude":[NSNumber numberWithDouble:coordinate.longitude]}];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

- (void) location_updateNow:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [[R1SDK sharedInstance].locationService updateNow];
    
        [self sendOkResultToCommand:command];
    }];
}

@end
