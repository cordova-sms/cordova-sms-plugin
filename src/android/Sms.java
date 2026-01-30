package com.cordova.plugins.sms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.LOG;

public class Sms extends CordovaPlugin {

	private static final String LOG_TAG = "CordovaPluginSMS";
	public final String ACTION_SEND_SMS = "send";

	public final String ACTION_HAS_PERMISSION = "has_permission";
	
	public final String ACTION_REQUEST_PERMISSION = "request_permission";

	private static final String INTENT_FILTER_SMS_SENT = "SMS_SENT";

	private static final int SEND_SMS_REQ_CODE = 0;

	private static final int REQUEST_PERMISSION_REQ_CODE = 1;

	private CallbackContext callbackContext;

	private JSONArray args;

	@Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		this.args = args;
		if (action.equals(ACTION_SEND_SMS)) {
			boolean isIntent = false;
			try {
				isIntent = args.getString(2).equalsIgnoreCase("INTENT");
			} catch (NullPointerException npe) {
				// It might throw a NPE, but it doesn't matter.
			}
			if (isIntent || hasPermission()) {
				sendSMS();
			} else {
				requestPermission(SEND_SMS_REQ_CODE);
			}
			return true;
		}
		else if (action.equals(ACTION_HAS_PERMISSION)) {
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, hasPermission()));
			return true;
		}
		else if (action.equals(ACTION_REQUEST_PERMISSION)) {
			requestPermission(REQUEST_PERMISSION_REQ_CODE);
			return true;
		}
		return false;
	}

	private boolean hasPermission() {
		return cordova.hasPermission(android.Manifest.permission.SEND_SMS);
	}

	private void requestPermission(int requestCode) {
		cordova.requestPermission(this, requestCode, android.Manifest.permission.SEND_SMS);
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "User has denied permission"));
				return;
			}
		}
		if (requestCode == SEND_SMS_REQ_CODE) {
			sendSMS();
			return;
		}
		callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
	}

	private boolean sendSMS() {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					//parsing arguments
					String separator = ";";
					LOG.i(LOG_TAG, "SMS args" + args);
					if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
						// See http://stackoverflow.com/questions/18974898/send-sms-through-intent-to-multiple-phone-numbers/18975676#18975676
						separator = ",";
					}
					String phoneNumber = args.getJSONArray(0).join(separator).replace("\"", "");
					String message = args.getString(1);
					String method = args.getString(2);
                    String slot = args.getString(4);
					boolean replaceLineBreaks = Boolean.parseBoolean(args.getString(3));

					// replacing \n by new line if the parameter replaceLineBreaks is set to true
					if (replaceLineBreaks) {
						message = message.replace("\\n", System.getProperty("line.separator"));
					}
					if (!checkSupport()) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS not supported on this platform"));
						return;
					}
					if (method.equalsIgnoreCase("INTENT")) {
						invokeSMSIntent(phoneNumber, message);
						// always passes success back to the app
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
					} else {
						send(callbackContext, phoneNumber, message, slot);
					}
					return;
				} catch (JSONException ex) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
				}
			}
		});
		return true;
	}

	private boolean checkSupport() {
		Activity ctx = this.cordova.getActivity();
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	}

	@SuppressLint("NewApi")
	private void invokeSMSIntent(String phoneNumber, String message) {
		Intent sendIntent;
		if ("".equals(phoneNumber) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this.cordova.getActivity());

			sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, message);

			if (defaultSmsPackageName != null) {
				sendIntent.setPackage(defaultSmsPackageName);
			}
		} else {
			sendIntent = new Intent(Intent.ACTION_VIEW);
			sendIntent.putExtra("sms_body", message);
			// See http://stackoverflow.com/questions/7242190/sending-sms-using-intent-does-not-add-recipients-on-some-devices
			sendIntent.putExtra("address", phoneNumber);
			sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
		}
		this.cordova.getActivity().startActivity(sendIntent);
	}
    private int getSubscriptionId(String str) {
        if (str == null) {
            return -1;
        }
        try {
            int parseInt = Integer.parseInt(str);
            if (VERSION.SDK_INT < 22) {
                return -1;
            }
            int i = -1;
			
            for (SubscriptionInfo subscriptionInfo : SubscriptionManager.from(this.cordova.getActivity()).getActiveSubscriptionInfoList()) {
                if (parseInt == subscriptionInfo.getSimSlotIndex()) {
                    i = subscriptionInfo.getSubscriptionId();
                }
            }
            return i;
        } catch (Exception e) {
            return -1;
        }
    }
	private void send(final CallbackContext callbackContext, String phoneNumber, String message, String slot) {
		SmsManager manager;
        int subscriptionId = getSubscriptionId(slot);
        if (subscriptionId < 0) {
            manager = SmsManager.getDefault();
        } else {
            manager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        }
		final ArrayList<String> parts = manager.divideMessage(message);

		// by creating this broadcast receiver we can check whether or not the SMS was sent
		final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			boolean anyError = false; //use to detect if one of the parts failed
			int partsCount = parts.size(); //number of parts to send

			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case SmsManager.STATUS_ON_ICC_SENT:
				case Activity.RESULT_OK:
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_NO_SERVICE:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					anyError = true;
					break;
				}
				// trigger the callback only when all the parts have been sent
				partsCount--;
				if (partsCount == 0) {
					if (anyError) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
					} else {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
					}
					cordova.getActivity().unregisterReceiver(this);
				}
			}
		};

		// randomize the intent filter action to avoid using the same receiver
		String intentFilterAction = INTENT_FILTER_SMS_SENT + java.util.UUID.randomUUID().toString();
		//this.cordova.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(intentFilterAction));
        // Apps and services that target Android 14 (API level 34) or higher and use context-registered receivers are required to specify a flag to indicate whether or not the receiver should be exported to all other apps on the device: either RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED , respectively. 
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
		this.cordova.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(intentFilterAction),Context.RECEIVER_EXPORTED);
		}
		else
		{
		this.cordova.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(intentFilterAction));
		}

		PendingIntent sentIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, new Intent(intentFilterAction), PendingIntent.FLAG_IMMUTABLE);

		// depending on the number of parts we send a text message or multi parts
		if (parts.size() > 1) {
			ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
			for (int i = 0; i < parts.size(); i++) {
				sentIntents.add(sentIntent);
			}
			manager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
		}
		else {
			manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
		}
	}
}
