//
//  R1WebCommand.h
//  R1EmitterSDK
//
//  Created by Alexey Goliatin on 03.12.14.
//  Copyright (c) 2014 RadiumOne. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface R1WebCommand : NSObject

@property (nonatomic, retain) NSString *command;
@property (nonatomic, retain) NSDictionary *parameters;

@end
