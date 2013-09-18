
var sms = {
    send: function(phone, message, method, successCallback, failureCallback) {
        cordova.exec(
            successCallback,
            failureCallback,
            'Sms',
            'send',
            [phone, message, method]
        );
    }
}

module.exports = sms;
