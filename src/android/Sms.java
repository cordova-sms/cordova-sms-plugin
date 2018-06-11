package com.cordova.plugins.sms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SMSPlugin
        extends CordovaPlugin {
    private static final String LOGTAG = "SMSPlugin";

    public static final String ACTION_SET_OPTIONS = "setOptions";
    private static final String ACTION_START_WATCH = "startWatch";
    private static final String ACTION_STOP_WATCH = "stopWatch";
    private static final String ACTION_ENABLE_INTERCEPT = "enableIntercept";
    private static final String ACTION_LIST_SMS = "listSMS";
    private static final String ACTION_DELETE_SMS = "deleteSMS";
    private static final String ACTION_RESTORE_SMS = "restoreSMS";
    private static final String ACTION_SEND_SMS = "sendSMS";

    public static final String OPT_LICENSE = "license";
    private static final String SEND_SMS_ACTION = "SENT_SMS_ACTION";
    private static final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SMS_EXTRA_NAME = "pdus";

    public static final String SMS_URI_ALL = "content://sms/";
    public static final String SMS_URI_INBOX = "content://sms/inbox";
    public static final String SMS_URI_SEND = "content://sms/sent";
    public static final String SMS_URI_DRAFT = "content://sms/draft";
    public static final String SMS_URI_OUTBOX = "content://sms/outbox";
    public static final String SMS_URI_FAILED = "content://sms/failed";
    public static final String SMS_URI_QUEUED = "content://sms/queued";

    public static final String BOX = "box";
    public static final String ADDRESS = "address";
    public static final String BODY = "body";
    public static final String READ = "read";
    public static final String SEEN = "seen";
    public static final String SUBJECT = "subject";
    public static final String SERVICE_CENTER = "service_center";
    public static final String DATE = "date";
    public static final String DATE_SENT = "date_sent";
    public static final String STATUS = "status";
    public static final String REPLY_PATH_PRESENT = "reply_path_present";
    public static final String TYPE = "type";
    public static final String PROTOCOL = "protocol";

    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;

    private static final String SMS_GENERAL_ERROR = "SMS_GENERAL_ERROR";
    private static final String NO_SMS_SERVICE_AVAILABLE = "NO_SMS_SERVICE_AVAILABLE";
    private static final String SMS_FEATURE_NOT_SUPPORTED = "SMS_FEATURE_NOT_SUPPORTED";
    private static final String SENDING_SMS_ID = "SENDING_SMS";

    private ContentObserver mObserver = null;
    private BroadcastReceiver mReceiver = null;
    private boolean mIntercept = false;
    private String lastFrom = "";
    private String lastContent = "";

    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        if (ACTION_SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            this.setOptions(options);
            result = new PluginResult(PluginResult.Status.OK);
        } else if (ACTION_START_WATCH.equals(action)) {
            result = this.startWatch(callbackContext);
        } else if (ACTION_STOP_WATCH.equals(action)) {
            result = this.stopWatch(callbackContext);
        } else if (ACTION_ENABLE_INTERCEPT.equals(action)) {
            boolean on_off = inputs.optBoolean(0);
            result = this.enableIntercept(on_off, callbackContext);
        } else if (ACTION_DELETE_SMS.equals(action)) {
            JSONObject msg = inputs.optJSONObject(0);
            try {
                result = this.deleteSMS(msg, callbackContext);
            } catch (Exception e) {
                Log.e(LOGTAG, "deleteSMS : " + e.getMessage());
                e.printStackTrace();
            }
        } else if (ACTION_RESTORE_SMS.equals(action)) {
            JSONArray smsList = inputs.optJSONArray(0);
            try {
                result = this.restoreSMS(smsList, callbackContext);
            } catch (Exception e) {
                Log.e(LOGTAG, "restoreSMS : " + e.getMessage());
                e.printStackTrace();
            }
        } else if (ACTION_LIST_SMS.equals(action)) {
            JSONObject filters = inputs.optJSONObject(0);
            try {
                result = this.listSMS(filters, callbackContext);
            } catch (Exception e) {
                Log.e(LOGTAG, "listSMS : " + e.getMessage());
                e.printStackTrace();
            }
        } else if (ACTION_SEND_SMS.equals(action)) {
            JSONArray addressList = inputs.optJSONArray(0);
            String message = inputs.optString(1);
            try {
                result = this.sendSMS(addressList, message, callbackContext);
            } catch (Exception e) {
                Log.e(LOGTAG, "sendSMS : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.d(LOGTAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        if (result != null) {
            callbackContext.sendPluginResult(result);
        }
        return true;
    }

    public void onDestroy() {
        this.stopWatch(null);
    }

    public void setOptions(JSONObject options) {
        Log.d(LOGTAG, ACTION_SET_OPTIONS);
    }

    protected String __getProductShortName() {
        return "SMS";
    }

    public final String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; ++i) {
                String h = Integer.toHexString(255 & messageDigest[i]);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException digest) {
            return "";
        }
    }

    private PluginResult startWatch(CallbackContext callbackContext) {
        try {
            Log.d(LOGTAG, ACTION_START_WATCH);
            if (this.mObserver == null) {
                this.createContentObserver();
            }
            if (this.mReceiver == null) {
                this.createIncomingSMSReceiver();
            }
            if (callbackContext != null) {
                callbackContext.success();
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "startWatch : " + e.getMessage());
        }
        return null;
    }

    private PluginResult stopWatch(CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_STOP_WATCH);
        Activity ctx = this.cordova.getActivity();
        if (this.mReceiver != null) {
            ctx.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
            Log.d(LOGTAG, "broadcast receiver unregistered");
        }
        if (this.mObserver != null) {
            ctx.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
            Log.d(LOGTAG, "sms inbox observer unregistered");
        }
        if (callbackContext != null) {
            callbackContext.success();
        }
        return null;
    }

    private PluginResult enableIntercept(boolean on_off, CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_ENABLE_INTERCEPT);
        this.mIntercept = on_off;
        if (callbackContext != null) {
            callbackContext.success();
        }
        return null;
    }

    private PluginResult sendSMS(JSONArray addressList, String text, CallbackContext callbackContext) throws Exception {
        Log.d(LOGTAG, ACTION_SEND_SMS);
        if (this.cordova.getActivity().getPackageManager().hasSystemFeature("android.hardware.telephony")) {
            int n;
            if ((n = addressList.length()) > 0) {
                PendingIntent sentIntent = PendingIntent.getBroadcast((Context) this.cordova.getActivity(), (int) 0, (Intent) new Intent("SENDING_SMS"), (int) 0);
                SmsManager sms = SmsManager.getDefault();
                for (int i = 0; i < n; ++i) {
                    String address;
                    if ((address = addressList.optString(i)).length() <= 0) continue;
                    sms.sendTextMessage(address, null, text, sentIntent, (PendingIntent) null);
                }
            } else {
                PendingIntent sentIntent = PendingIntent.getActivity((Context) this.cordova.getActivity(), (int) 0, (Intent) new Intent("android.intent.action.VIEW"), (int) 0);
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.putExtra("sms_body", text);
                intent.setType("vnd.android-dir/mms-sms");
                try {
                    sentIntent.send(this.cordova.getActivity().getApplicationContext(), 0, intent);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS is not supported"));
        }
        return null;
    }

    private PluginResult listSMS(JSONObject filter, CallbackContext callbackContext) throws Exception {
        Log.i(LOGTAG, ACTION_LIST_SMS);
        String uri_filter = filter.has(BOX) ? filter.optString(BOX) : "inbox";
        int fread = filter.has(READ) ? filter.optInt(READ) : -1;
        int fid = filter.has("_id") ? filter.optInt("_id") : -1;
        String faddress = filter.optString(ADDRESS);
        String fcontent = filter.optString(BODY);
        int indexFrom = filter.has("indexFrom") ? filter.optInt("indexFrom") : 0;
        int maxCount = filter.has("maxCount") ? filter.optInt("maxCount") : 10;
        JSONArray jsons = new JSONArray();
        Activity ctx = this.cordova.getActivity();
        Uri uri = Uri.parse((SMS_URI_ALL + uri_filter));
        Cursor cur = ctx.getContentResolver().query(uri, (String[]) null, "", (String[]) null, null);
        int i = 0;
        while (cur.moveToNext()) {
            JSONObject json;
            boolean matchFilter = false;
            if (fid > -1) {
                matchFilter = (fid == cur.getInt(cur.getColumnIndex("_id")));
            } else if (fread > -1) {
                matchFilter = (fread == cur.getInt(cur.getColumnIndex(READ)));
            } else if (faddress.length() > 0) {
                matchFilter = faddress.equals(cur.getString(cur.getColumnIndex(ADDRESS)).trim());
            } else if (fcontent.length() > 0) {
                matchFilter = fcontent.equals(cur.getString(cur.getColumnIndex(BODY)).trim());
            } else {
                matchFilter = true;
            }
            if (!matchFilter) continue;

            if (i < indexFrom) continue;
            if (i >= indexFrom + maxCount) break;
            ++i;

            if ((json = this.getJsonFromCursor(cur)) == null) {
                callbackContext.error("failed to get json from cursor");
                cur.close();
                return null;
            }
            jsons.put((Object) json);
        }
        cur.close();
        callbackContext.success(jsons);
        return null;
    }

    private JSONObject getJsonFromCursor(Cursor cur) throws Exception {
        JSONObject json = new JSONObject();

        int nCol = cur.getColumnCount();
        String keys[] = cur.getColumnNames();

        try {
            for (int j = 0; j < nCol; j++) {
                switch (cur.getType(j)) {
                    case Cursor.FIELD_TYPE_NULL:
                        json.put(keys[j], null);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        json.put(keys[j], cur.getLong(j));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        json.put(keys[j], cur.getFloat(j));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        json.put(keys[j], cur.getString(j));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        json.put(keys[j], cur.getBlob(j));
                        break;
                }
            }
        } catch (Exception e) {
            return null;
        }

        return json;
    }

    private void fireEvent(final String event, JSONObject json) throws Exception {
        final String str = json.toString();
        Log.d(LOGTAG, "Event: " + event + ", " + str);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String js = String.format("javascript:cordova.fireDocumentEvent(\"%s\", {\"data\":%s});", event, str);
                webView.loadUrl(js);
            }
        });
    }

    private void onSMSArrive(JSONObject json) throws Exception {
        String from = json.optString(ADDRESS);
        String content = json.optString(BODY);
        if (from.equals(this.lastFrom) && content.equals(this.lastContent)) {
            return;
        }
        this.lastFrom = from;
        this.lastContent = content;
        this.fireEvent("onSMSArrive", json);
    }

    protected void createIncomingSMSReceiver() throws Exception {
        Activity ctx = this.cordova.getActivity();
        this.mReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(LOGTAG, ("onRecieve: " + action));
                if (SMS_RECEIVED.equals(action)) {
                    Bundle bundle;
                    if (SMSPlugin.this.mIntercept) {
                        this.abortBroadcast();
                    }
                    if ((bundle = intent.getExtras()) != null) {
                        Object[] pdus;
                        if ((pdus = (Object[]) bundle.get("pdus")).length != 0) {
                            for (int i = 0; i < pdus.length; ++i) {
                                SmsMessage sms = SmsMessage.createFromPdu((byte[]) ((byte[]) pdus[i]));
                                JSONObject json = null;
                                try {
                                    json = SMSPlugin.this.getJsonFromSmsMessage(sms);
                                } catch (Exception e) {
                                    Log.e(LOGTAG, "createContentObserver :" + e.getMessage());
                                    e.printStackTrace();
                                }
                                try {
                                    SMSPlugin.this.onSMSArrive(json);
                                } catch (Exception e) {
                                    Log.e(LOGTAG, "createIncomingSMSReceiver :" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        };
        String[] filterstr = new String[]{SMS_RECEIVED};
        for (int i = 0; i < filterstr.length; ++i) {
            IntentFilter filter = new IntentFilter(filterstr[i]);
            filter.setPriority(100);
            ctx.registerReceiver(this.mReceiver, filter);
            Log.d(LOGTAG, ("broadcast receiver registered for: " + filterstr[i]));
        }
    }

    protected void createContentObserver() {
        Activity ctx = this.cordova.getActivity();
        this.mObserver = new ContentObserver(new Handler()) {

            public void onChange(boolean selfChange) {
                this.onChange(selfChange, null);
            }

            public void onChange(boolean selfChange, Uri uri) {
                try {
                    ContentResolver resolver = cordova.getActivity().getContentResolver();
                    Log.d(LOGTAG, ("onChange, selfChange: " + selfChange + ", uri: " + (Object) uri));
                    int id = -1;
                    String str;
                    if (uri != null && (str = uri.toString()).startsWith(SMS_URI_ALL)) {
                        try {
                            id = Integer.parseInt(str.substring(SMS_URI_ALL.length()));
                            Log.d(LOGTAG, ("sms id: " + id));
                        } catch (NumberFormatException var6_6) {
                            // empty catch block
                        }
                    }
                    if (id == -1) {
                        uri = Uri.parse(SMS_URI_INBOX);
                    }

                    Cursor cur = resolver.query(uri, null, null, null, "_id desc");
                    if (cur != null) {
                        int n = cur.getCount();
                        Log.d(LOGTAG, ("n = " + n));
                        if (n > 0 && cur.moveToFirst()) {
                            JSONObject json;
                            if ((json = SMSPlugin.this.getJsonFromCursor(cur)) != null) {
                                onSMSArrive(json);
                            } else {
                                Log.d(LOGTAG, "fetch record return null");
                            }
                        }
                        cur.close();
                    }
                } catch (Exception ex) {
                    Log.e(LOGTAG, "createContentObserver->" + ex.getMessage());
                    ex.printStackTrace();

                }
            }
        };
        ctx.getContentResolver().registerContentObserver(Uri.parse(SMS_URI_INBOX), true, this.mObserver);
        Log.d(LOGTAG, "sms inbox observer registered");
    }

    private PluginResult deleteSMS(JSONObject filter, CallbackContext callbackContext) throws Exception {
        Log.d(LOGTAG, ACTION_DELETE_SMS);
        String uri_filter = filter.has(BOX) ? filter.optString(BOX) : "inbox";
        int fread = filter.has(READ) ? filter.optInt(READ) : -1;
        int fid = filter.has("_id") ? filter.optInt("_id") : -1;
        String faddress = filter.optString(ADDRESS);
        String fcontent = filter.optString(BODY);
        Activity ctx = this.cordova.getActivity();
        int n = 0;
        try {
            Uri uri = Uri.parse((SMS_URI_ALL + uri_filter));
            Cursor cur = ctx.getContentResolver().query(uri, (String[]) null, "", (String[]) null, null);
            while (cur.moveToNext()) {
                int id = cur.getInt(cur.getColumnIndex("_id"));
                boolean matchId = fid > -1 && fid == id;
                int read = cur.getInt(cur.getColumnIndex(READ));
                boolean matchRead = fread > -1 && fread == read;
                String address = cur.getString(cur.getColumnIndex(ADDRESS)).trim();
                boolean matchAddr = faddress.length() > 0 && address.equals(faddress);
                String body = cur.getString(cur.getColumnIndex(BODY)).trim();
                boolean matchContent = fcontent.length() > 0 && body.equals(fcontent);
                if (!matchId && !matchRead && !matchAddr && !matchContent) continue;
                ctx.getContentResolver().delete(uri, "_id=" + id, (String[]) null);
                ++n;
            }
            callbackContext.success(n);
        } catch (Exception e) {
            callbackContext.error(e.toString());
        }
        return null;
    }

    private JSONObject getJsonFromSmsMessage(SmsMessage sms) throws Exception {
        JSONObject json = new JSONObject();

        try {
            json.put(ADDRESS, sms.getOriginatingAddress());
            json.put(BODY, sms.getMessageBody()); // May need sms.getMessageBody.toString()
            json.put(DATE_SENT, sms.getTimestampMillis());
            json.put(DATE, System.currentTimeMillis());
            json.put(READ, MESSAGE_IS_NOT_READ);
            json.put(SEEN, MESSAGE_IS_NOT_SEEN);
            json.put(STATUS, sms.getStatus());
            json.put(TYPE, MESSAGE_TYPE_INBOX);
            json.put(SERVICE_CENTER, sms.getServiceCenterAddress());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    private ContentValues getContentValuesFromJson(JSONObject json) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ADDRESS, json.optString(ADDRESS));
        values.put(BODY, json.optString(BODY));
        values.put(DATE_SENT, json.optLong(DATE_SENT));
        values.put(READ, json.optInt(READ));
        values.put(SEEN, json.optInt(SEEN));
        values.put(TYPE, json.optInt(TYPE));
        values.put(SERVICE_CENTER, json.optString(SERVICE_CENTER));
        return values;
    }

    private PluginResult restoreSMS(JSONArray array, CallbackContext callbackContext) throws Exception {
        ContentResolver resolver = this.cordova.getActivity().getContentResolver();
        Uri uri = Uri.parse(SMS_URI_INBOX);
        int n = array.length();
        int m = 0;
        for (int i = 0; i < n; ++i) {
            JSONObject json;
            if ((json = array.optJSONObject(i)) == null) continue;
            String str = json.toString();
            Log.d(LOGTAG, str);
            Uri newuri = resolver.insert(uri, this.getContentValuesFromJson(json));
            Log.d(LOGTAG, ("inserted: " + newuri.toString()));
            ++m;
        }
        if (callbackContext != null) {
            callbackContext.success(m);
        }
        return null;
    }

}
