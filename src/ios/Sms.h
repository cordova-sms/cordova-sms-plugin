#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/NSData+Base64.h>

#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMessageComposeViewController.h>

@interface Sms : CDVPlugin <MFMessageComposeViewControllerDelegate>

@property(strong) NSString* callbackID;
@property(retain) NSString* tempStoredFile;

- (void)send:(CDVInvokedUrlCommand*)command;

@end