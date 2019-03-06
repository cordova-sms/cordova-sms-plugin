package com.cordova.plugins.sms;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import android.os.Environment;
import android.os.StrictMode;
import android.telephony.SmsManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

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
        switch (action) {
            case ACTION_SEND_SMS:
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
            case ACTION_HAS_PERMISSION:
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, hasPermission()));
                return true;
            case ACTION_REQUEST_PERMISSION:
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

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
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

    private void sendSMS() {
        cordova.getThreadPool().execute(() -> {
            try {
                String separator = ";";
                if (Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
                    separator = ",";
                }
                String phoneNumber = args.getJSONArray(0).join(separator).replace("\"", "");
                String message = args.getString(1);
                String image = args.getString(2);
                String method = args.getString(3);
                boolean replaceLineBreaks = Boolean.parseBoolean(args.getString(4));

                // replacing \n by new line if the parameter replaceLineBreaks is set to true
                if (replaceLineBreaks) {
                    message = message.replace("\\n", System.getProperty("line.separator"));
                }
                if (!checkSupport()) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS not supported on this platform"));
                    return;
                }
                if (method.equalsIgnoreCase("INTENT")) {
                    invokeSMSIntent(phoneNumber, message, image);
                    // always passes success back to the app
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                } else {
                    send(callbackContext, phoneNumber, message);
                }
            } catch (JSONException ex) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        });
    }

    private boolean checkSupport() {
        Activity ctx = this.cordova.getActivity();
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }


    private void invokeSMSIntentNoImage(String phoneNumber, String message) {
        Intent sendIntent;
        sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", message);
        sendIntent.putExtra("address", phoneNumber);
        sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
        this.cordova.getActivity().startActivity(sendIntent);
    }


    @SuppressLint("NewApi")
    private void invokeSMSIntent(String phoneNumber, String message, String imageBase64) {

        if (imageBase64.equals("")) {
            invokeSMSIntentNoImage(phoneNumber, message);
            return;
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent sendIntent;
        sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra("sms_body", message);
        sendIntent.putExtra("address", phoneNumber);

        byte[] decodedString = Base64.getDecoder().decode(imageBase64);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String saveFilePath = Environment.getExternalStorageDirectory() + "/HealthAngel";
        File dir = new File(saveFilePath);


        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "logo.png");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            decodedByte.compress(Bitmap.CompressFormat.PNG, 80, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(saveFilePath + "/logo.png")));
        sendIntent.setType("image/*");
        this.cordova.getActivity().startActivity(sendIntent);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void send(final CallbackContext callbackContext, String phoneNumber, String message) {
        SmsManager manager = SmsManager.getDefault();
        final ArrayList<String> parts = manager.divideMessage(message);

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
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                sentIntents.add(sentIntent);
            }
            manager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
        } else {
            manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
        }
    }
}