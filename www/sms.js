'use strict';

var exec = require('cordova/exec');

var sms = {};

function convertPhoneToArray(phone) {
    if (typeof phone === 'string' && phone.indexOf(',') !== -1) {
        phone = phone.split(',');
    }
    if (Object.prototype.toString.call(phone) !== '[object Array]') {
        phone = [phone];
    }
    return phone;
}


sms.send = function(phone, message, options, success, failure) {
    // parsing phone numbers
    phone = convertPhoneToArray(phone);

    // parsing options
    var replaceLineBreaks = false;
    var androidIntent = '';
    var attachments = null;
    if (typeof options === 'string') { // ensuring backward compatibility
        window.console.warn('[DEPRECATED] Passing a string as a third argument is deprecated. Please refer to the documentation to pass the right parameter: https://github.com/cordova-sms/cordova-sms-plugin.');
        androidIntent = options;
    }
    else if (typeof options === 'object') {
        replaceLineBreaks = options.replaceLineBreaks || false;
        if (options.android && typeof options.android === 'object') {
            androidIntent = options.android.intent;
        }
        if (options.attachments) {
            attachments = options.attachments;
        }
    }

    // fire
    exec(
        success,
        failure,
        'Sms',
        'send', [phone, message, androidIntent, replaceLineBreaks, attachments]
    );
};

sms.hasPermission = function(success, failure) {
    // fire
    exec(
        success,
        failure,
        'Sms',
        'has_permission', []
    );
};

sms.requestPermission = function(success, failure) {
    // fire
    exec(
        success,
        failure,
        'Sms',
        'request_permission', []
    );
};

module.exports = sms;