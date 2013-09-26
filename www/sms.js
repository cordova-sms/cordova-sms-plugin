
var sms = {
    send: function(phone, message, method, successCallback, failureCallback) {
        if (Object.prototype.toString.call(phone) !== '[object Array]') {
          phone = [phone];
        }
        cordova.exec(
            successCallback,
            failureCallback,
            'Sms',
            'send',
            [phone, message, method]
        );
    }
};

module.exports = sms;



/*SMSComposer.prototype.showSMSComposerWithCB = function(cbFunction,toRecipients,body) {
	this.resultCallback = cbFunction;
	this.showSms.apply(this,[toRecipients,body]);
};

SMSComposer.prototype._didFinishWithResult = function(res) {
	this.resultCallback(res);
};*/
