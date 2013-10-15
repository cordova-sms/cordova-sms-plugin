// phonegap-sms-plugin https://github.com/aharris88/phonegap-sms-plugin
//  from SMS Composer plugin for PhoneGap- SMSComposer.m Created by Grant Sanders on 12/25/2010.
//  https://github.com/phonegap/phonegap-plugins/blob/master/iOS/SMSComposer

// Revised by Adam Harris https://github.com/aharris88
// Revised by Clément Vollet https://github.com/dieppe
// Quick Revision by Johnny Slagle 10/15/2013

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMessageComposeViewController.h>

@interface Sms : CDVPlugin <MFMessageComposeViewControllerDelegate>

- (void)send:(CDVInvokedUrlCommand*)command;

@end