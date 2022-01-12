#import "Sms.h"

@implementation Sms
@synthesize callbackID;

- (void)send:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        self.callbackID = command.callbackId;
        
        if(![MFMessageComposeViewController canSendText]) {

        	dispatch_async(dispatch_get_main_queue(), ^{
				NSString *errorMessage = NSLocalizedString(@"SMS Text not available.", nil);

				UIAlertController * alert = [UIAlertController
											 alertControllerWithTitle:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"]
											 message:errorMessage
											 preferredStyle:UIAlertControllerStyleAlert];

				UIAlertAction *ok =[
									UIAlertAction actionWithTitle:NSLocalizedString(@"OK", nil)
									style:UIAlertActionStyleDefault
									handler:^(UIAlertAction *action){
										CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
										[self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
										}
									];
				[alert addAction:ok];
				[self.viewController presentViewController:alert animated:YES completion:nil];
			});
			return;
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            MFMessageComposeViewController *composeViewController = [[MFMessageComposeViewController alloc] init];
            composeViewController.messageComposeDelegate = self;
        
            NSString* body = [command.arguments objectAtIndex:1];
            if (body != nil) {
                BOOL replaceLineBreaks = [[command.arguments objectAtIndex:3] boolValue];
                if (replaceLineBreaks) {
                    body = [body stringByReplacingOccurrencesOfString: @"\\n" withString: @"\n"];
                }
                [composeViewController setBody:body];
            }
        
            NSMutableArray* recipients = [command.arguments objectAtIndex:0];
            if (recipients != nil) {
                if ([recipients.firstObject isEqual: @""]) {
                    [recipients replaceObjectAtIndex:0 withObject:@"?"];
                }

                [composeViewController setRecipients:recipients];
            }
            [self.viewController presentViewController:composeViewController animated:YES completion:nil];
        });
    }];
}

#pragma mark - MFMessageComposeViewControllerDelegate Implementation
// Dismisses the composition interface when users tap Cancel or Send. Proceeds to update the message field with the result of the operation.
- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    // Notifies users about errors associated with the interface
    int webviewResult = 0;
    NSString* message = @"";
    
    switch(result) {
        case MessageComposeResultCancelled:
            webviewResult = 0;
            message = @"Message cancelled.";
            break;
        case MessageComposeResultSent:
            webviewResult = 1;
            message = @"Message sent.";
            break;
        case MessageComposeResultFailed:
            webviewResult = 2;
            message = @"Message failed.";
            break;
        default:
            webviewResult = 3;
            message = @"Unknown error.";
            break;
    }
    
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    if(webviewResult == 1) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsString:message];
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
    } else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString:message];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
    }
}

@end
