#import "Sms.h"
#import <Cordova/NSArray+Comparisons.h>

@implementation Sms
@synthesize callbackID;

- (CDVPlugin *)initWithWebView:(UIWebView *)theWebView {
    self = (Sms *)[super initWithWebView:theWebView];
    return self;
}


- (bool)isSMSAvailable {
    Class messageClass = (NSClassFromString(@"MFMessageComposeViewController"));
    return messageClass != nil && [messageClass canSendText];
}

- (bool)isMMSAvailable {
    return [self isSMSAvailable] && [(NSClassFromString(@"MFMessageComposeViewController")) respondsToSelector:@selector(canSendAttachments)];
}

- (NSString *)parseBody:(NSString*)body replaceLineBreaks:(BOOL)replaceLineBreaks {
    return (body != nil && replaceLineBreaks) ? [body stringByReplacingOccurrencesOfString: @"\\n" withString: @"\n"] : body;
}

- (NSMutableArray *)parseRecipients:(NSObject*)param {
    NSMutableArray *recipients = [[NSMutableArray alloc] init];
    if (![param isKindOfClass:[NSNull class]] && [param isKindOfClass:[NSString class]]) {
        [recipients addObject:[NSString stringWithFormat:@"%@", param]];
    }
    return recipients;
}

- (void)send:(CDVInvokedUrlCommand*)command {
    self.callbackID = command.callbackId;

    // test SMS availability
    if(![self isSMSAvailable]) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"SMS_NOT_AVAILABLE"];
        return [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
    }

    // retrieve the options dictionnary
    NSDictionary* options = [command.arguments objectAtIndex:2];
    // parse the body parameter
    NSString *body = [self parseBody:[command.arguments objectAtIndex:1] replaceLineBreaks:[[options objectForKey:@"replaceLineBreaks"]  boolValue]];
    // parse the recipients parameter
    NSMutableArray *recipients = (![[command.arguments objectAtIndex:0] isKindOfClass:[NSMutableArray class]]) ? [command.arguments objectAtIndex:0] : [self parseRecipients:[command.arguments objectAtIndex:0]];
        
    // initialize the composer
    MFMessageComposeViewController *composeViewController = [[MFMessageComposeViewController alloc] init];
    composeViewController.messageComposeDelegate = self;
    if (recipients != nil) {
        if ([recipients.firstObject isEqual: @""]) { // http://stackoverflow.com/questions/19951040/mfmessagecomposeviewcontroller-opens-mms-editing-instead-of-sms-and-buddy-name
            [recipients replaceObjectAtIndex:0 withObject:@"?"];
        }
        
        [composeViewController setRecipients:recipients];
    }
    // append the body the composer
    if (body != nil) {
        [composeViewController setBody:body];
    }

    // fire the composer
    [self.viewController presentViewController:composeViewController animated:YES completion:nil];
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
