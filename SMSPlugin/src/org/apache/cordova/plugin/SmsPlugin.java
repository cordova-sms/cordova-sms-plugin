package org.apache.cordova.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;

public class SmsPlugin extends CordovaPlugin {
	public final String ACTION_SEND_SMS = "SendSMS";

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION_SEND_SMS)) {
			try {				
				String phoneNumber = args.getString(0);
				String message = args.getString(1);
				String method = args.getString(2);
				
				if(method.equalsIgnoreCase("INTENT")){
					invokeSMSIntent(phoneNumber, message);
                    callbackContext.sendPluginResult(new PluginResult( PluginResult.Status.NO_RESULT));
				} else{
					sendSMS(phoneNumber, message);
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
	
	private void invokeSMSIntent(String phoneNumber, String message) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", message);
        sendIntent.setType("vnd.android-dir/mms-sms");
        this.cordova.getActivity().startActivity(sendIntent);
	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager manager = SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getActivity(this.cordova.getActivity(), 0, new Intent(), 0);
		manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
	}
	
}
