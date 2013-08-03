var SmsPlugin = function () {};

SmsPlugin.prototype.send = function (method, phone, message, successCallback, failureCallback) {    
    return PhoneGap.exec(successCallback, failureCallback, 'SmsPlugin', "SendSMS", [method , phone, message]);
};

PhoneGap.addConstructor(function() {
    PhoneGap.addPlugin("sms", new SmsPlugin());
});