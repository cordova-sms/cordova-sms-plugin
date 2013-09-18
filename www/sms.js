
var sms = {
    send: function(phone, message, method, successCallback, failureCallback) {
        alert("sms.send");
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
