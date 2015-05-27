#import "LDSharePlugin.h"

static NSDictionary * errorToDic(NSError * error)
{
    return @{@"code":[NSNumber numberWithInteger:error.code], @"message":error.localizedDescription};
}

@implementation LDSharePlugin
{
    
}

- (void)pluginInitialize
{
    
}

-(void) share:(NSString *) text image:(UIImage*) image callbackId:(NSString*) callbackId
{
    NSMutableArray *items = [NSMutableArray new];
    [items addObject:text];
    if (image) {
        [items addObject:image];
    }
    UIActivityViewController * activityController = [[UIActivityViewController alloc] initWithActivityItems:items applicationActivities:nil];
    // Exclude activities that are irrelevant
    activityController.excludedActivityTypes = @[UIActivityTypePrint,UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact];
    
    if ([activityController respondsToSelector:@selector(completionWithItemsHandler)]) {
        activityController.completionWithItemsHandler = ^(NSString *activityType, BOOL completed, NSArray *returnedItems, NSError *error) {
            // When completed flag is YES, user performed specific activity
            
            NSMutableArray * array = [NSMutableArray arrayWithObjects:activityType?:@"", [NSNumber numberWithBool:completed],nil];
            if (error) {
                [array addObject:errorToDic(error)];
            }
            
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:error ? CDVCommandStatus_ERROR : CDVCommandStatus_OK messageAsArray:array];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        };
    } else {
        activityController.completionHandler = ^(NSString *activityType, BOOL completed) {
            CDVPluginResult * result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:@[activityType?:@"", [NSNumber numberWithBool:completed]]];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        };
    }
    
    
    [self.viewController presentViewController:activityController animated:YES completion:nil];
    
    //iPad compatibility
    if ([activityController respondsToSelector:@selector(popoverPresentationController)]) {
        UIPopoverPresentationController * pop = activityController.popoverPresentationController;
        if (pop) {
            pop.sourceView = self.viewController.view;
        }
    }
    

}

-(UIImage*)getImage: (NSString *)imageName {
    UIImage *image = nil;
    if (imageName) {
        if ([imageName hasPrefix:@"http"]) {
            image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageName]]];
        } else if ([imageName hasPrefix:@"www/"]) {
            image = [UIImage imageNamed:imageName];
        } else if ([imageName hasPrefix:@"file://"]) {
            image = [UIImage imageWithData:[NSData dataWithContentsOfFile:[[NSURL URLWithString:imageName] path]]];
        } else if ([imageName hasPrefix:@"data:"]) {
            // using a base64 encoded string
            NSURL *imageURL = [NSURL URLWithString:imageName];
            NSData *imageData = [NSData dataWithContentsOfURL:imageURL];
            image = [UIImage imageWithData:imageData];
        } else if ([imageName hasPrefix:@"assets-library://"]) {
            // use assets-library
            NSURL *imageURL = [NSURL URLWithString:imageName];
            NSData *imageData = [NSData dataWithContentsOfURL:imageURL];
            image = [UIImage imageWithData:imageData];
        } else {
            // assume anywhere else, on the local filesystem
            image = [UIImage imageWithData:[NSData dataWithContentsOfFile:imageName]];
        }
    }
    return image;
}

-(void) jobInBackground:(CDVInvokedUrlCommand*) command
{
    NSString * text = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    NSString * imageName = [command argumentAtIndex:1 withDefault:nil andClass:[NSString class]];
    UIImage * image = [self getImage:imageName];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self share:text image:image callbackId:command.callbackId];
    });
    
}

-(void) share:(CDVInvokedUrlCommand*) command
{
    [self performSelectorInBackground:@selector(jobInBackground:) withObject:command];
}

@end
