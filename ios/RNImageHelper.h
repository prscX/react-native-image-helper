
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@interface RNImageHelper : NSObject <RCTBridgeModule>

+ (UIImage *) GenerateImage: (NSDictionary *) props;
+ (UIImage *) GenerateVectorIcon: (NSDictionary *) icon;

@end
  
