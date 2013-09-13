var exec = require("cordova/exec");
var SmsPlugin = function () {};

SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {
    return cordova.exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [phone, message, method]);
};

window.sms = new SmsPlugin();

/*SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {
    return PhoneGap.exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [phone, message, method]);
};

PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("sms", new SmsPlugin());
});*/