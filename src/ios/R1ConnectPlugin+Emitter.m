#import "R1ConnectPlugin.h"
#import "R1ConnectPlugin+Common.h"

#import "R1SDK.h"
#import "R1Emitter.h"

#define EMITTER_NOT_STARTED @"R1Emitter not started"

@implementation R1ConnectPlugin (Emitter)

- (BOOL) testFailedCommand:(CDVInvokedUrlCommand *)command parametersCount:(NSUInteger) parametersCount
{
    if (![R1Emitter sharedInstance].isStarted)
    {
        [self sendFailedWithMessage:EMITTER_NOT_STARTED toCommand:command];
        return YES;
    }
    
    if ([command.arguments count] != parametersCount)
    {
        [self sendWrongParametersCountToCommand:command];
        return YES;
    }

    return NO;
}

- (NSArray *) getPermissionsArrayFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSArray *permissionsInfo = [self getArrayFromCommand:command parameterIndex:2];
    
    NSMutableArray *permissions = [NSMutableArray array];
    if (permissionsInfo != nil)
    {
        for (NSDictionary *info in permissionsInfo)
        {
            if ([info isKindOfClass:[NSDictionary class]])
            {
                [permissions addObject:[R1EmitterSocialPermission socialPermissionWithName:[info objectForKey:@"name"]
                                                                                   granted:[[info objectForKey:@"granted"] boolValue]]];
            }
        }
    }

    return permissions;
}

- (NSString *) getString:(NSObject *) string
{
    if ([string isKindOfClass:[NSString class]])
        return (NSString *)string;
    if ([string isKindOfClass:[NSNumber class]])
        return [(NSNumber *)string stringValue];
    return nil;
}

- (NSUInteger) getInteger:(NSObject *) string
{
    if ([string isKindOfClass:[NSString class]])
        return [(NSString *)string integerValue];
    if ([string isKindOfClass:[NSNumber class]])
        return [(NSNumber *)string integerValue];
    return 0;
}

- (double) getDouble:(NSObject *) string
{
    if ([string isKindOfClass:[NSString class]])
        return [(NSString *)string doubleValue];
    if ([string isKindOfClass:[NSNumber class]])
        return [(NSNumber *)string doubleValue];
    return 0;
}


- (R1EmitterLineItem *) getLineItemFromCommand:(CDVInvokedUrlCommand *) command parameterIndex:(NSInteger) parameterIndex
{
    NSDictionary *info = [self getDictionaryFromCommand:command parameterIndex:parameterIndex];
    
    if (![info isKindOfClass:[NSDictionary class]])
        return nil;
    
    R1EmitterLineItem *lineItem = [[R1EmitterLineItem alloc] init];
    
    lineItem.productID = [self getString:info[@"productID"]];
    lineItem.productName = [self getString:info[@"productName"]];
    lineItem.quantity = [self getInteger:info[@"quantity"]];
    lineItem.unitOfMeasure = [self getString:info[@"unitOfMeasure"]];
    lineItem.msrPrice = [self getDouble:info[@"msrPrice"]];
    lineItem.pricePaid = [self getDouble:info[@"pricePaid"]];
    lineItem.currency = [self getString:info[@"currency"]];
    lineItem.itemCategory = [self getString:info[@"itemCategory"]];
    
    return lineItem;
}

- (void) emitter_isStarted:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withBool:[R1Emitter sharedInstance].isStarted];
    }];
}

- (void) emitter_setAppName:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        [R1Emitter sharedInstance].appName = [self getStringFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_getAppName:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withString:[R1Emitter sharedInstance].appName];
    }];
}

- (void) emitter_setAppId:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1Emitter sharedInstance].appId = [self getStringFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_getAppId:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withString:[R1Emitter sharedInstance].appId];
    }];
}

- (void) emitter_setAppVersion:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }
    
    [self dispatch:^{
        [R1Emitter sharedInstance].appVersion = [self getStringFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_getAppVersion:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withString:[R1Emitter sharedInstance].appVersion];
    }];
}

- (void) emitter_setSessionTimeout:(CDVInvokedUrlCommand *)command
{
    if ([command.arguments count] != 1)
    {
        [self sendWrongParametersCountToCommand:command];
        return;
    }

    [self dispatch:^{
        [R1Emitter sharedInstance].sessionTimeout = [self getDoubleFromCommand:command parameterIndex:0];
    
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_getSessionTimeout:(CDVInvokedUrlCommand *)command
{
    [self dispatch:^{
        [self sendOkResultToCommand:command withDouble:[R1Emitter sharedInstance].sessionTimeout];
    }];
}

- (void) emitter_emitEvent:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:2])
        return;
    
    [self dispatch:^{
        NSString *name = [self getStringFromCommand:command parameterIndex:0];
        
        if (name == nil)
        {
            [self sendWrongParameterToCommand:command];
            return;
        }
        
        [[R1Emitter sharedInstance] emitEvent:name withParameters:[self getDictionaryFromCommand:command parameterIndex:1]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitAction:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:4])
        return;

    [self dispatch:^{
        NSString *action = [self getStringFromCommand:command parameterIndex:0];
        
        if (action == nil)
        {
            [self sendWrongParameterToCommand:command];
            return;
        }
        
        [[R1Emitter sharedInstance] emitAction:action
                                         label:[self getStringFromCommand:command parameterIndex:1]
                                         value:[self getLongFromCommand:command parameterIndex:2]
                                     otherInfo:[self getDictionaryFromCommand:command parameterIndex:3]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitLogin:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:3])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitLoginWithUserID:[self getStringFromCommand:command parameterIndex:0]
                                               userName:[self getStringFromCommand:command parameterIndex:1]
                                              otherInfo:[self getDictionaryFromCommand:command parameterIndex:3]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitRegistration:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:9])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitRegistrationWithUserID:[self getStringFromCommand:command parameterIndex:0]
                                                      userName:[self getStringFromCommand:command parameterIndex:1]
                                                         email:[self getStringFromCommand:command parameterIndex:2]
                                                 streetAddress:[self getStringFromCommand:command parameterIndex:3]
                                                         phone:[self getStringFromCommand:command parameterIndex:4]
                                                          city:[self getStringFromCommand:command parameterIndex:5]
                                                         state:[self getStringFromCommand:command parameterIndex:6]
                                                           zip:[self getStringFromCommand:command parameterIndex:7]
                                                     otherInfo:[self getDictionaryFromCommand:command parameterIndex:8]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitFBConnect:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:4])
        return;

    [self dispatch:^{
        [[R1Emitter sharedInstance] emitFBConnectWithUserID:[self getStringFromCommand:command parameterIndex:0]
                                                   userName:[self getStringFromCommand:command parameterIndex:1]
                                                permissions:[self getPermissionsArrayFromCommand:command parameterIndex:2]
                                                  otherInfo:[self getDictionaryFromCommand:command parameterIndex:3]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitTConnect:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:4])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitTConnectWithUserID:[self getStringFromCommand:command parameterIndex:0]
                                                  userName:[self getStringFromCommand:command parameterIndex:1]
                                               permissions:[self getPermissionsArrayFromCommand:command parameterIndex:2]
                                                 otherInfo:[self getDictionaryFromCommand:command parameterIndex:3]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitTransaction:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:10])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitTransactionWithID:[self getStringFromCommand:command parameterIndex:0]
                                                  storeID:[self getStringFromCommand:command parameterIndex:1]
                                                storeName:[self getStringFromCommand:command parameterIndex:2]
                                                   cartID:[self getStringFromCommand:command parameterIndex:3]
                                                  orderID:[self getStringFromCommand:command parameterIndex:4]
                                                totalSale:[self getDoubleFromCommand:command parameterIndex:5]
                                                 currency:[self getStringFromCommand:command parameterIndex:6]
                                            shippingCosts:[self getDoubleFromCommand:command parameterIndex:7]
                                           transactionTax:[self getDoubleFromCommand:command parameterIndex:8]
                                                otherInfo:[self getDictionaryFromCommand:command parameterIndex:9]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitTransactionItem:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:3])
        return;

    [self dispatch:^{
        [[R1Emitter sharedInstance] emitTransactionItemWithTransactionID:[self getStringFromCommand:command parameterIndex:0]
                                                                lineItem:[self getLineItemFromCommand:command parameterIndex:1]
                                                               otherInfo:[self getDictionaryFromCommand:command parameterIndex:2]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitCartCreate:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:2])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitCartCreateWithCartID:[self getStringFromCommand:command parameterIndex:0]
                                                   otherInfo:[self getDictionaryFromCommand:command parameterIndex:1]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitCartDelete:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:2])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitCartDeleteWithCartID:[self getStringFromCommand:command parameterIndex:0]
                                                   otherInfo:[self getDictionaryFromCommand:command parameterIndex:1]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitAddToCart:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:3])
        return;

    [self dispatch:^{
        [[R1Emitter sharedInstance] emitAddToCartWithCartID:[self getStringFromCommand:command parameterIndex:0]
                                                   lineItem:[self getLineItemFromCommand:command parameterIndex:1]
                                                  otherInfo:[self getDictionaryFromCommand:command parameterIndex:2]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitDeleteFromCart:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:3])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitDeleteFromCartWithCartID:[self getStringFromCommand:command parameterIndex:0]
                                                        lineItem:[self getLineItemFromCommand:command parameterIndex:1]
                                                       otherInfo:[self getDictionaryFromCommand:command parameterIndex:2]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitUpgrade:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:1])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitUpgradeWithOtherInfo:[self getDictionaryFromCommand:command parameterIndex:0]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitTrialUpgrade:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:1])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitTrialUpgradeWithOtherInfo:[self getDictionaryFromCommand:command parameterIndex:0]];
        
        [self sendOkResultToCommand:command];
    }];
}

- (void) emitter_emitScreenView:(CDVInvokedUrlCommand *)command
{
    if ([self testFailedCommand:command parametersCount:6])
        return;
    
    [self dispatch:^{
        [[R1Emitter sharedInstance] emitScreenViewWithDocumentTitle:[self getStringFromCommand:command parameterIndex:0]
                                                 contentDescription:[self getStringFromCommand:command parameterIndex:1]
                                                documentLocationUrl:[self getStringFromCommand:command parameterIndex:2]
                                                   documentHostName:[self getStringFromCommand:command parameterIndex:3]
                                                       documentPath:[self getStringFromCommand:command parameterIndex:4]
                                                          otherInfo:[self getDictionaryFromCommand:command parameterIndex:5]];
        
        [self sendOkResultToCommand:command];
    }];
}

@end
