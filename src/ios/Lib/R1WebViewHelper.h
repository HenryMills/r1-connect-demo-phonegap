#import <Foundation/Foundation.h>

@interface R1WebViewHelper : NSObject

+ (BOOL) webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType;

@end
