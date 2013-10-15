package org.apache.cordova.plugin.sms;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class Sms extends CordovaPlugin {
	private final String LOG_TAG = "SMSPlugin";
	public final String ACTION_SEND_SMS = "send";

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

				if(method.equalsIgnoreCase("INTENT")){
					invokeSMSIntent(phoneNumber, message);
                    callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.NO_RESULT));
				} else{
					send(phoneNumber, message);
				}
				
				callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
				return true;
			}
			catch (JSONException ex) {
				callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.JSON_EXCEPTION));
			}			
		}
		return false;
	}

	private boolean checkSupport() {
		Activity ctx = this.cordova.getActivity();
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	}

	private void invokeSMSIntent(String phoneNumber, String message) {
		// See http://stackoverflow.com/a/7242594
		Log.d(LOG_TAG, "Starting SMS app, with number(s): " + phoneNumber + " and message " + message);
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", message);
		sendIntent.putExtra("address", phoneNumber);
		sendIntent.setData(Uri.parse("smsto:" + phoneNumber));
		this.cordova.getActivity().startActivity(sendIntent);
	}

	private void send(String phoneNumber, String message) {
		Log.d(LOG_TAG, "Sending SMS to " + phoneNumber + " and message " + message);
		SmsManager manager = SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getActivity(this.cordova.getActivity(), 0, new Intent(), 0);
		manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
	}
	
}