'use strict';

var exec = require('cordova/exec');

var sms = function() {

    var isArray = function(o) {
        return Object.prototype.toString.call(o) === '[object Array]';
    };

    var parseOptions = function(options) {
        var opts = {
            attachments: [],
            replaceLineBreaks: false,
            android: {
                intent: 'INTENT'
            }
        };

        if (options === null) {
            return opts;
        }

        if (typeof options === 'string') { // ensuring backward compatibility
            window.console.warn('[DEPRECATED] Passing a string as a third argument is deprecated. Please refer to the documentation to pass the right parameter: https://github.com/cordova-sms/cordova-sms-plugin.');
            opts.android.intent = options;
        } else if (typeof options === 'object') {
            opts.replaceLineBreaks = options.replaceLineBreaks || false;
            opts.attachments = isArray(options.attachments) ? options.attachments : [];
            if (options.android && typeof options.android === 'object') {
                opts.android.intent = options.android.intent;
            }
        }

        return opts;
    };

    return {
        send: function(recipients, message, options, success, failure) {
            return exec(success, failure, 'Sms', 'send', [recipients, message, parseOptions(options)]);
        }
    };
}();

module.exports = sms;