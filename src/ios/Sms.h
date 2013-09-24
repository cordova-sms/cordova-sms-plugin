//  from SMS Composer plugin for PhoneGap- SMSComposer.h Created by Grant Sanders on 12/25/2010.
//  https://github.com/phonegap/phonegap-plugins/blob/master/iOS/SMSComposer

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMessageComposeViewController.h>

@interface Sms : CDVPlugin <MFMessageComposeViewControllerDelegate>{}

- (void)send:(CDVInvokedUrlCommand*)command;
@end