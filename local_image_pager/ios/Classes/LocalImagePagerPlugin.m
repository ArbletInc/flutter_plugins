#import "LocalImagePagerPlugin.h"
#if __has_include(<local_image_pager/local_image_pager-Swift.h>)
#import <local_image_pager/local_image_pager-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "local_image_pager-Swift.h"
#endif

@implementation LocalImagePagerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftLocalImagePagerPlugin registerWithRegistrar:registrar];
}
@end
