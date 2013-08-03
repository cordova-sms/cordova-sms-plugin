var SmsPlugin = function () {};

SmsPlugin.prototype.send = function (phone, message, method, successCallback, failureCallback) {    
    return PhoneGap.exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [phone, message, method]);
};

PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("sms", new SmsPlugin());
});