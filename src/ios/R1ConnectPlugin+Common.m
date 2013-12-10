#import "R1ConnectPlugin+Common.h"

#define WRONG_PARAMETERS_COUNT @"Wrong parameters count"
#define WRONG_PARAMETER @"One of parameter is wrong"

@implementation R1ConnectPlugin (Common)

- (void) dispatch:(dispatch_block_t) dispatchBlock
{
    if (dispatchBlock == nil)
        return;
    
    dispatch_async(dispatch_get_main_queue(), dispatchBlock);
}

- (void) sendFailedWithMessage:(NSString *) message toCommand:(CDVInvokedUrlCommand *) command
{
    CDVPluginResult* pluginResult = nil;
    
    if (message == nil)
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    else
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) sendWrongParametersCountToCommand:(CDVInvokedUrlCommand *) command
{
    [self sendFailedWithMessage:WRONG_PARAMETERS_COUNT toCommand:command];
}

- (void) sendWrongParameterToCommand:(CDVInvokedUrlCommand *) command
{
    [self sendFailedWithMessage:WRONG_PARAMETER toCommand:command];
}

- (void) sendNotificationFromService:(NSString *) service notification:(NSString *) notification info:(NSObject *) info
{
    NSData *jsonInfo = [NSJSONSerialization dataWithJSONObject:info options:0 error:nil];
    
    NSString *json = @"null";
    if (jsonInfo != nil)
        json = [[NSString alloc] initWithData:jsonInfo encoding:NSUTF8StringEncoding];
    
    NSString *js = [NSString stringWithFormat:@"cordova.fireDocumentEvent('%@.%@', %@);", service, notification, json];
    
    [self.commandDelegate evalJs:js scheduledOnRunLoop:NO];
}

- (NSObject *) getObjectFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    if ([command.arguments count] <= parameterIndex)
        return nil;
    
    return [command.arguments objectAtIndex:parameterIndex];
}

- (NSInteger) getIntegerFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSObject *obj = [self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([obj isKindOfClass:[NSString class]])
        return [(NSString *)obj integerValue];
    
    if ([obj isKindOfClass:[NSNumber class]])
        return [(NSNumber *)obj integerValue];
    
    return 0;
}

- (long long) getLongFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSObject *obj = [self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([obj isKindOfClass:[NSString class]])
        return [(NSString *)obj longLongValue];
    
    if ([obj isKindOfClass:[NSNumber class]])
        return [(NSNumber *)obj longLongValue];
    
    return 0;
}

- (double) getDoubleFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSObject *obj = [self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([obj isKindOfClass:[NSString class]])
        return [(NSString *)obj doubleValue];
    
    if ([obj isKindOfClass:[NSNumber class]])
        return [(NSNumber *)obj doubleValue];
    
    return 0;
}

- (BOOL) getBoolFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSObject *obj = [self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([obj isKindOfClass:[NSNumber class]])
        return [(NSNumber *)obj boolValue];
    
    if ([obj isKindOfClass:[NSString class]])
    {
        NSString *str = [(NSString *) obj lowercaseString];
        return ([str isEqualToString:@"true"] || [str isEqualToString:@"1"] || [str isEqualToString:@"yes"]);
    }
    
    return false;
}

- (NSString *) getStringFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSObject *obj = [self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([obj isKindOfClass:[NSString class]])
    {
        if ([(NSString *)obj length] == 0)
            return nil;
        
        return (NSString *)obj;
    }
    
    if ([obj isKindOfClass:[NSNumber class]])
        return [(NSNumber *)obj stringValue];
    
    return nil;
}

- (NSDictionary *) getDictionaryFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSDictionary *dict = (NSDictionary *)[self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([dict isKindOfClass:[NSDictionary class]])
        return dict;
    
    return nil;
}

- (NSArray *) getArrayFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSArray *arr = (NSArray *)[self getObjectFromCommand:command parameterIndex:parameterIndex];
    
    if ([arr isKindOfClass:[NSArray class]])
        return arr;
    
    return nil;
}

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command
{
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
                                callbackId:command.callbackId];
}

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withBool:(BOOL) boolValue
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:boolValue];
    
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withInteger:(NSInteger) integerValue
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:integerValue];
    
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withDouble:(double) doubleValue
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:doubleValue];
    
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withString:(NSString *) stringValue
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:stringValue];
    
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}


@end
