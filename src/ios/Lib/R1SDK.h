#import <Foundation/Foundation.h>

@class R1Emitter, R1Push, R1LocationService, CLLocation;

@interface R1SDK : NSObject

@property(nonatomic, copy) NSString *applicationId;
@property(nonatomic, copy) NSString *clientKey; // Only for push

@property(nonatomic, copy) NSString *applicationUserId;

/*!
 The current user location.
 Use this variable only if your application already used location services.
 */
@property(nonatomic, copy) CLLocation *location;

+ (R1SDK *) sharedInstance;

@property (nonatomic, readonly) R1Emitter *emitter;
@property (nonatomic, readonly) R1Push    *push;
@property (nonatomic, readonly) R1LocationService *locationService;

- (void) startEmitter;
- (void) startPush;

- (void) start; // Start emitter and push

// Show Push options
+ (void) presendModalPushOptionsFromViewController:(UIViewController *) viewController animated:(BOOL)animated;
+ (void) showPushOptionsInNavigationController:(UINavigationController *) navigationController animated:(BOOL)animated;

// Show Location options
+ (void) presendModalLocationOptionsFromViewController:(UIViewController *) viewController animated:(BOOL)animated;
+ (void) showLocationOptionsInNavigationController:(UINavigationController *) navigationController animated:(BOOL)animated;

+ (NSString *) sha1:(NSString *) inputString;
+ (NSString *) md5:(NSString *) inputString;

@end
