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
import java.util.ArrayList;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
//--------------- added to read Sms ----------
import org.json.JSONObject;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
//--------------- added to read Sms ----------
public class Sms extends CordovaPlugin {
    public final String ACTION_SEND_SMS = "send";
    public final String ACTION_HAS_PERMISSION = "has_permission";
    private static final String INTENT_FILTER_SMS_SENT = "SMS_SENT";
    private static final int SEND_SMS_REQ_CODE = 0;
    private CallbackContext callbackContext;
    private JSONArray args;
    //--------------- added to read Sms ----------
    private static final String TAG = "ReadSmsPlugin";
    private static final String GET_TEXTS_ACTION = "GetTexts";
    private static final String GET_TEXTS_AFTER = "GetTextsAfter";
    // Defaults:
    private static final Integer READ_ALL = -1;
    //--------------- added to read Sms ----------
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.args = args;
        if (action.equals(ACTION_SEND_SMS)) {
            if (hasPermission()) {
                sendSMS();
            } else {
                requestPermission();
            }
            return true;
        } else if (action.equals(ACTION_HAS_PERMISSION)) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, hasPermission()));
            return true;
        }
        return false;
    }
    private boolean hasPermission() {
        return cordova.hasPermission(android.Manifest.permission.SEND_SMS);
    }
    private void requestPermission() {
        cordova.requestPermission(this, SEND_SMS_REQ_CODE, android.Manifest.permission.SEND_SMS);
    }
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r: grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "User has denied permission"));
                return;
            }
        }
        sendSMS();
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
    private void send(final CallbackContext callbackContext, String phoneNumber, String message) {
            SmsManager manager = SmsManager.getDefault();
            final ArrayList < String > parts = manager.divideMessage(message);
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
                ArrayList < PendingIntent > sentIntents = new ArrayList < PendingIntent > ();
                for (int i = 0; i < parts.size(); i++) {
                    sentIntents.add(sentIntent);
                }
                manager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
            } else {
                manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
            }
        }
        //--------------- added to read Sms ----------
        public class ReadSms extends CordovaPlugin {
        private static final String TAG = "ReadSmsPlugin";
        private static final String GET_TEXTS_ACTION = "GetTexts";
        private static final String GET_TEXTS_AFTER  = "GetTextsAfter";

        // Defaults:
        private static final Integer READ_ALL = -1;

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            Log.d(TAG, "Inside ReadSms plugin.");

            JSONObject result = new JSONObject();

            if (args.length() == 0) {
                result.put("error", "No phone number provided.");
                callbackContext.success(result);
                return false;
            }

            String phoneNumber = args.getString(0);
            result.put("phone_number", phoneNumber);

            if (action.equals("") || action.equals(GET_TEXTS_ACTION)) {
                return getTexts(args, callbackContext, result, phoneNumber);
            } else if (action.equals(GET_TEXTS_AFTER)) {
                return getTextsAfterTimeStamp(callbackContext, phoneNumber, args, result);
            } else {
                Log.e(TAG, "Unknown action provided.");
                result.put("error", "Unknown action provided.");
                callbackContext.success(result);
                return false;
            }
        }

        private boolean getTextsAfterTimeStamp( CallbackContext callbackContext,
                String phoneNumber, JSONArray args,    JSONObject result) throws JSONException {
            Log.d(TAG, "Read texts after timestamp.");

            if (args.length() < 2) {
                Log.e(TAG, "Time stamp is not provided.");
                result.put("error", "No timestamp provided.");
                callbackContext.success(result);
                return false;
            }

            String timeStamp = args.getString(1);
            if (!isNumeric(timeStamp)) {
                Log.e(TAG, "Time stamp provided is non-numeric.");
                result.put("error",
                    String.format("Time stamp provided (%s) is non-numeric.", timeStamp));
                callbackContext.success(result);
                return false;
            } else if (timeStamp.startsWith("-") || timeStamp.startsWith("-")) {
                // isNumeric(...) corner case
                timeStamp = timeStamp.substring(1, timeStamp.length() - 1);
            }

            Log.d(TAG, String.format(
                "Querying for number %s with texts received after %s time stamp.",
                phoneNumber, timeStamp));
            JSONArray readResults = readTextsAfter(phoneNumber, timeStamp);
            Log.d(TAG, "Read results received: " + readResults.toString());

            result.put("texts", readResults);
            callbackContext.success(result);
            return true;
        }

        private boolean getTexts(JSONArray args, CallbackContext callbackContext,
                JSONObject result, String phoneNumber) throws JSONException {
            Log.d(TAG, "Get texts from specified number.");
            Integer numberOfTexts = READ_ALL; // Default
            if (args.length() >= 2) { // We want numberOfTexts to be the second one
                Log.d(TAG, "Setting maximum number of texts to retrieve.");
                try {
                    numberOfTexts = Integer.valueOf(args.getString(1));
                } catch (NumberFormatException nfe) {
                    String errorMessage =  String.format("Input provided (%s) is not a number",
                            args.getString(1));
                    Log.e(TAG, errorMessage);
                    result.put("error", errorMessage);
                    return false;
                }
                if (numberOfTexts <= 0) {
                    numberOfTexts = READ_ALL;
                }
            }

            JSONArray readResults = readTextsFrom(phoneNumber, numberOfTexts);
            Log.d(TAG, "read results: " + readResults.toString());
            result.put("texts", readResults);
            callbackContext.success(result);
            return true;
        }

        private JSONArray readTextsFrom(String numberToCheck, Integer numberOfTexts
                ) throws JSONException {
            ContentResolver contentResolver = cordova.getActivity().getContentResolver();
            String[] smsNo = new String[] { numberToCheck };

            String sortOrder = "date DESC"
                    + ((numberOfTexts == READ_ALL) ? "" : " limit " + numberOfTexts);

            Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null,
                    "address=?", smsNo, sortOrder);

            JSONArray results = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject current = new JSONObject();
                try {
                    current.put("time_received", cursor.getString(cursor.getColumnIndex("date")));
                    current.put("message", cursor.getString(cursor.getColumnIndex("body")));
                    Log.d(TAG, "time: " + cursor.getString(cursor.getColumnIndex("date"))
                            + " message: " + cursor.getString(cursor.getColumnIndex("body")));
                } catch (JSONException e) {
                    e.printStackTrace();
                    current.put("error", new String("Error reading text"));
                }
                results.put(current);
            }

            return results;
        }

        private JSONArray readTextsAfter(String numberToCheck, String timeStamp
                ) throws JSONException {
            ContentResolver contentResolver = cordova.getActivity().getContentResolver();
            String[] queryData = new String[] { numberToCheck, timeStamp };

            String sortOrder = "date DESC";

            Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null,
                "address=? AND date>=?", queryData, sortOrder);

            JSONArray results = new JSONArray();
            while (cursor.moveToNext()) {
                JSONObject current = new JSONObject();
                try {
                    current.put("time_received", cursor.getString(cursor.getColumnIndex("date")));
                    current.put("message", cursor.getString(cursor.getColumnIndex("body")));
                    Log.d(TAG, "time: " + cursor.getString(cursor.getColumnIndex("date"))
                        + " message: " + cursor.getString(cursor.getColumnIndex("body")));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error reading text", e);
                    current.put("error", new String("Error reading text(s)."));
                }
                results.put(current);
            }

            Log.d(TAG, "Before returning results");
            return results;
        }

        /**
         * From Rosetta code: {@link http://rosettacode.org/wiki/Determine_if_a_string_is_numeric#Java}
         * @param inputData
         * @return
         */
        private static boolean isNumeric(String inputData) {
             return inputData.matches("[-+]?\\d+(\\.\\d+)?");
        }
        //--------------- added to read Sms ----------
}
