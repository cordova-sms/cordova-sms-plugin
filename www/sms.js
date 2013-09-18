
var sms = {
    send: function (phone, message, method, successCallback, failureCallback) {
        alert("sms.send");
        cordova.eexec(
        	successCallback,
        	failureCallback,
        	'Sms',
        	"send",
        	[phone, message, method]
        );
    }
}

/*var exec = require("cordova/exec");
var SmsPlugin = function () {};

SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {
    exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [phone, message, method]);
};

module.exports = new SmsPlugin();*/
//window.sms = new sms();

/*SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {
    return PhoneGap.exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [phone, message, method]);
};

PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("sms", new SmsPlugin());
});*/
