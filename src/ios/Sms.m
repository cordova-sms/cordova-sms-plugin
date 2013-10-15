// phonegap-sms-plugin https://github.com/aharris88/phonegap-sms-plugin
//  from SMS Composer plugin for PhoneGap- SMSComposer.m Created by Grant Sanders on 12/25/2010.
//  https://github.com/phonegap/phonegap-plugins/blob/master/iOS/SMSComposer

// Revised by Adam Harris https://github.com/aharris88
// Revised by Clément Vollet https://github.com/dieppe
// Quick Revision by Johnny Slagle 10/15/2013

#import "Sms.h"
#import <Cordova/NSArray+Comparisons.h>

@implementation Sms

- (CDVPlugin *)initWithWebView:(UIWebView *)theWebView {
	self = (Sms *)[super initWithWebView:theWebView];
	return self;
}

- (void)send:(CDVInvokedUrlCommand*)command {
	
	// MFMessageComposeViewController has been availible since iOS 4.0. There should be no issue with using it straight.
	if(![MFMessageComposeViewController canSendText]) {
		UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Notice"
														message:@"SMS Text not available."
													   delegate:self
											  cancelButtonTitle:@"OK"
											  otherButtonTitles:nil];
		[alert show];
		return;
	}
		
	MFMessageComposeViewController *composeViewController = [[MFMessageComposeViewController alloc] init];
	composeViewController.messageComposeDelegate = self;

	NSString* body = [command.arguments objectAtIndex:1];
	if (body != nil) {
		[composeViewController setBody:body];
	}

	NSArray* recipients = [command.arguments objectAtIndex:0];
	if (recipients != nil) {
		[composeViewController setRecipients:recipients];
	}

	[self.viewController presentViewController:composeViewController animated:YES completion:nil];
	[[UIApplication sharedApplication] setStatusBarHidden:YES];
}

#pragma mark - MFMessageComposeViewControllerDelegate Implementation
// Dismisses the composition interface when users tap Cancel or Send. Proceeds to update the message field with the result of the operation.
- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
	// Notifies users about errors associated with the interface
	int webviewResult = 0;

	switch (result) {
		case MessageComposeResultCancelled:
			webviewResult = 0;
			break;

		case MessageComposeResultSent:
			webviewResult = 1;
			break;

		case MessageComposeResultFailed:
			webviewResult = 2;
			break;

		default:
			webviewResult = 3;
			break;
	}

	[self.viewController dismissViewControllerAnimated:YES completion:nil];
	[[UIApplication sharedApplication] setStatusBarHidden:NO];	// Note: I put this in because it seemed to be missing.
	
	[self writeJavascript:[NSString stringWithFormat:@"window.plugins.sms._didFinishWithResult(%d);", webviewResult]];
}

@end