#import "Sms.h"

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

- (bool)areAttachmentsAvailable {
    return [(NSClassFromString(@"MFMessageComposeViewController")) respondsToSelector:@selector(canSendAttachments)];
}

- (NSString *)parseBody:(NSString*)body replaceLineBreaks:(BOOL)replaceLineBreaks {
    return ((id)body != [NSNull null] && replaceLineBreaks) ? [body stringByReplacingOccurrencesOfString: @"\\n" withString: @"\n"] : body;
}

- (NSMutableArray *)parseRecipients:(id)param {
    NSMutableArray *recipients = [[NSMutableArray alloc] init];
    if (![param isKindOfClass:[NSNull class]]) {
        if ([param isKindOfClass:[NSString class]]) {
            [recipients addObject:[NSString stringWithFormat:@"%@", param]];
        }
        else if ([param isKindOfClass:[NSMutableArray class]]) {
            recipients = param;
        }

        // http://stackoverflow.com/questions/19951040/mfmessagecomposeviewcontroller-opens-mms-editing-instead-of-sms-and-buddy-name
        if ([recipients.firstObject isEqual: @""]) {
            [recipients replaceObjectAtIndex:0 withObject:@"?"];
        }
    }
    return recipients;
}

// shamelessly copied from https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin/blob/master/src/ios/SocialSharing.m#L557
-(NSURL *)getFile: (NSString *)fileName {
    NSURL *file = nil;
    if (fileName != (id)[NSNull null]) {
        if ([fileName hasPrefix:@"http"]) {
            NSURL *url = [NSURL URLWithString:fileName];
            NSData *fileData = [NSData dataWithContentsOfURL:url];
            file = [NSURL fileURLWithPath:[self storeInFile:(NSString*)[[fileName componentsSeparatedByString: @"/"] lastObject] fileData:fileData]];
        } else if ([fileName hasPrefix:@"www/"]) {
            NSString *bundlePath = [[NSBundle mainBundle] bundlePath];
            NSString *fullPath = [NSString stringWithFormat:@"%@/%@", bundlePath, fileName];
            file = [NSURL fileURLWithPath:fullPath];
        } else if ([fileName hasPrefix:@"file://"]) {
            // stripping the first 6 chars, because the path should start with / instead of file://
            file = [NSURL fileURLWithPath:[fileName substringFromIndex:6]];
        } else if ([fileName hasPrefix:@"data:"]) {
            // using a base64 encoded string
            // extract some info from the 'fileName', which is for example: data:text/calendar;base64,<encoded stuff here>
            NSString *fileType = (NSString*)[[[fileName substringFromIndex:5] componentsSeparatedByString: @";"] objectAtIndex:0];
            fileType = (NSString*)[[fileType componentsSeparatedByString: @"/"] lastObject];
            NSString *base64content = (NSString*)[[fileName componentsSeparatedByString: @","] lastObject];
            NSData *fileData = [NSData dataFromBase64String:base64content];
            file = [NSURL fileURLWithPath:[self storeInFile:[NSString stringWithFormat:@"%@.%@", @"file", fileType] fileData:fileData]];
        } else {
            // assume anywhere else, on the local filesystem
            file = [NSURL fileURLWithPath:fileName];
        }
    }
    return file;
}

// shamelessly copied from https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin/blob/master/src/ios/SocialSharing.m#L587
-(NSString *)storeInFile: (NSString*) fileName fileData: (NSData*) fileData {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *filePath = [documentsDirectory stringByAppendingPathComponent:fileName];
    [fileData writeToFile:filePath atomically:YES];
    _tempStoredFile = filePath;
    return filePath;
}

- (void)cleanupStoredFiles {
  if (_tempStoredFile != nil) {
    NSError *error;
    [[NSFileManager defaultManager]removeItemAtPath:_tempStoredFile error:&error];
  }
}

- (void)send:(CDVInvokedUrlCommand*)command {
    self.callbackID = command.callbackId;

    [self.commandDelegate runInBackground:^{
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
        NSMutableArray *recipients = [self parseRecipients:[command.arguments objectAtIndex:0]];
        // parse the attachments
        NSArray *attachments = [options objectForKey:@"attachments"];
            
        // initialize the composer
        MFMessageComposeViewController *composeViewController = [[MFMessageComposeViewController alloc] init];
        composeViewController.messageComposeDelegate = self;

        // add recipients
        [composeViewController setRecipients:recipients];
        
        // append the body to the composer
        if ((id)body != [NSNull null]) {
            [composeViewController setBody:body];
        }

        // append attachments
        if (attachments != nil && [attachments count] > 0) {
            if(![self areAttachmentsAvailable]) {
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"MMS_NOT_AVAILABLE"];
                return [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
            }
            
            for (id attachment in attachments) {
                NSURL *file = [self getFile:attachment];
                if (file != nil) {
                    [composeViewController addAttachmentURL:file withAlternateFilename:nil];
                }
            }
        }

        // fire the composer
        [self.viewController presentViewController:composeViewController animated:YES completion:nil];
    }];
    
}

#pragma mark - MFMessageComposeViewControllerDelegate Implementation
// Dismisses the composition interface when users tap Cancel or Send. Proceeds to update the message field with the result of the operation.
- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    // Notifies users about errors associated with the interface
    CDVCommandStatus* status = CDVCommandStatus_ERROR;
    NSString* message = @"";
    
    switch(result) {
        case MessageComposeResultCancelled:
            message = @"CANCELLED";
            break;
        case MessageComposeResultSent:
            status = CDVCommandStatus_OK;
            message = @"SENT";
            break;
        case MessageComposeResultFailed:
            message = @"FAILED";
            break;
        default:
            message = @"UNKNOWN_ERROR";
            break;
    }
    
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    // clean up stored files
    [self cleanupStoredFiles];
        
    // send the result
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:status messageAsString:message];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackID];
}

@end