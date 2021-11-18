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
import android.provider.Telephony;
import android.telephony.SmsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.util.Base64;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import androidx.core.content.FileProvider;

public class Sms extends CordovaPlugin {

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
					if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
						// See http://stackoverflow.com/questions/18974898/send-sms-through-intent-to-multiple-phone-numbers/18975676#18975676
						separator = ",";
					}
					String phoneNumber = args.getJSONArray(0).join(separator).replace("\"", "");
					String message = args.getString(1);
					String method = args.getString(2);
					boolean replaceLineBreaks = Boolean.parseBoolean(args.getString(3));
					JSONObject attachments = null;

					if (!args.isNull(4)) {
						attachments = args.getJSONObject(4);
					}

					// replacing \n by new line if the parameter replaceLineBreaks is set to true
					if (replaceLineBreaks) {
						message = message.replace("\\n", System.getProperty("line.separator"));
					}
					if (!checkSupport()) {
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS not supported on this platform"));
						return;
					}
					if (method.equalsIgnoreCase("INTENT")) {
						invokeSMSIntent(phoneNumber, message, attachments);
					} else {
						send(callbackContext, phoneNumber, message);
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
	private void invokeSMSIntent(String phoneNumber, String message, JSONObject attachments) {
		Intent sendIntent;
		List<File> images = null;

		if (attachments != null) {
			Iterator<String> keys = attachments.keys();
			while(keys.hasNext()) {
				String fileName = keys.next();
				String base64String;
				try {
					base64String = attachments.getString(fileName);
				} catch (JSONException e) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
					return;
				}
				byte[] imgBytes = Base64.decode(base64String, Base64.DEFAULT);

				File tmpDir = cordova.getContext().getCacheDir();
				File imgFile = new File(tmpDir, fileName);

				try {
					imgFile.createNewFile();
				} catch (IOException e) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
				}

				try (OutputStream stream = new FileOutputStream(imgFile)) {
					stream.write(imgBytes);
				} catch (NullPointerException e) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
				} catch (IOException e) {
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
				}
				if (images == null) {
					images = new ArrayList<>();
				}
				images.add(imgFile);
			}
		}

		final boolean hasImage = images != null;
		final boolean hasMultipleImages = hasImage && images.size() > 1;
		final boolean emptyPhoneNumber = "".equals(phoneNumber);

		if ((emptyPhoneNumber || hasImage)  && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this.cordova.getActivity());

			sendIntent = new Intent(hasMultipleImages ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, message);

			if (!emptyPhoneNumber) {	
				// See http://stackoverflow.com/questions/7242190/sending-sms-using-intent-does-not-add-recipients-on-some-devices
				sendIntent.putExtra("address", phoneNumber);
				sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
			}

			if (defaultSmsPackageName != null) {
				sendIntent.setPackage(defaultSmsPackageName);
			}
			sendIntent.setType( hasImage ? "image/*" : "text/plain");
		} else {
			sendIntent = new Intent(Intent.ACTION_VIEW);
			sendIntent.putExtra("sms_body", message);
			// See http://stackoverflow.com/questions/7242190/sending-sms-using-intent-does-not-add-recipients-on-some-devices
			sendIntent.putExtra("address", phoneNumber);
			sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
		}

		if (hasImage) {
			String FILE_AUTHORITY =  cordova.getContext().getPackageName() + ".cordova.plugins.sms.fileprovider";

			ArrayList<Uri> imageUris = images.stream()
					.map(file -> FileProvider.getUriForFile(cordova.getContext(), FILE_AUTHORITY, file))
					.collect(Collectors.toCollection(ArrayList::new));
			if (hasMultipleImages) {
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
			} else {
				sendIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0));
			}
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}

		callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
		this.cordova.getActivity().startActivity(sendIntent);
	}

	private void send(final CallbackContext callbackContext, String phoneNumber, String message) {
		SmsManager manager = SmsManager.getDefault();
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
		this.cordova.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(intentFilterAction));

		PendingIntent sentIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, new Intent(intentFilterAction), 0);

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
