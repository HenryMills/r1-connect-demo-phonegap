#import "R1ConnectPlugin.h"


@interface R1ConnectPlugin (Common)

- (void) dispatch:(dispatch_block_t) dispatchBlock;

- (void) sendFailedWithMessage:(NSString *) message toCommand:(CDVInvokedUrlCommand *) command;
- (void) sendWrongParametersCountToCommand:(CDVInvokedUrlCommand *) command;
- (void) sendWrongParameterToCommand:(CDVInvokedUrlCommand *) command;

- (void) sendNotificationFromService:(NSString *) service notification:(NSString *) notification info:(NSObject *) info;

- (NSInteger) getIntegerFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (long long) getLongFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (double) getDoubleFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (BOOL) getBoolFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (NSString *) getStringFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (NSDictionary *) getDictionaryFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;
- (NSArray *) getArrayFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex;

- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command;
- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withBool:(BOOL) boolValue;
- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withInteger:(NSInteger) integerValue;
- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withDouble:(double) doubleValue;
- (void) sendOkResultToCommand:(CDVInvokedUrlCommand *) command withString:(NSString *) stringValue;

@end
