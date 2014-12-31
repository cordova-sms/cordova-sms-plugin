package org.apache.cordova.plugin.sms;

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
import android.provider.Telephony;
import android.telephony.SmsManager;
import java.util.ArrayList;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class Sms extends CordovaPlugin {
	public final String ACTION_SEND_SMS = "send";
	private static final String INTENT_FILTER_SMS_SENT = "SMS_SENT";

	BroadcastReceiver receiver;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

		if (action.equals(ACTION_SEND_SMS)) {
			try {
				String phoneNumber = args.getJSONArray(0).join(";").replace("\"", "");
				String message = args.getString(1);
				String method = args.getString(2);

				if (!checkSupport()) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS not supported on this platform"));
					return true;
				}

				if (method.equalsIgnoreCase("INTENT")) {
					invokeSMSIntent(phoneNumber, message);
					// always passes success back to the app
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
				} else {
					// by creating this broadcast receiver we can check whether or not the SMS was sent
					if (receiver == null) {
						this.receiver = new BroadcastReceiver() {
							@Override
							public void onReceive(Context context, Intent intent) {
								PluginResult pluginResult;

								switch (getResultCode()) {
									case SmsManager.STATUS_ON_ICC_SENT:
										pluginResult = new PluginResult(PluginResult.Status.OK);
										pluginResult.setKeepCallback(true);
										callbackContext.sendPluginResult(pluginResult);
										break;
									case Activity.RESULT_OK:
										pluginResult = new PluginResult(PluginResult.Status.OK);
										pluginResult.setKeepCallback(true);
										callbackContext.sendPluginResult(pluginResult);
										break;
									case SmsManager.RESULT_ERROR_NO_SERVICE:
										pluginResult = new PluginResult(PluginResult.Status.ERROR);
										pluginResult.setKeepCallback(true);
										callbackContext.sendPluginResult(pluginResult);
										break;
									default:
										pluginResult = new PluginResult(PluginResult.Status.ERROR);
										pluginResult.setKeepCallback(true);
										callbackContext.sendPluginResult(pluginResult);
										break;
								}
							}
						};
						final IntentFilter intentFilter = new IntentFilter();
						intentFilter.addAction(INTENT_FILTER_SMS_SENT);
						cordova.getActivity().registerReceiver(this.receiver, intentFilter);
					}
					send(phoneNumber, message);
				}
				return true;
			} catch (JSONException ex) {
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
			}
		}
		return false;
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
			sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
			sendIntent.putExtra("sms_body", message);
		}
		this.cordova.getActivity().startActivity(sendIntent);
	}

	private void send(String phoneNumber, String message) {
		SmsManager manager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(this.cordova.getActivity(),
			0, new Intent(INTENT_FILTER_SMS_SENT), 0);

		// Use SendMultipartTextMessage if the message requires it
		int parts_size = manager.divideMessage(message).size();
		if (parts_size > 1) {
			ArrayList<String> parts = manager.divideMessage(message);
			ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
			for (int i = 0; i < parts_size; ++i) {
				sentIntents.add(sentIntent);
			}
			manager.sendMultipartTextMessage(phoneNumber, null, parts,
					sentIntents, null);
		} else {
			manager.sendTextMessage(phoneNumber, null, message, sentIntent,
					null);
		}
	}

	@Override
	public void onDestroy() {
		if (this.receiver != null) {
			try {
				this.cordova.getActivity().unregisterReceiver(this.receiver);
				this.receiver = null;
			} catch (Exception ignore) {
			}
		}
	}
}

