var sms = {
    send: function(phone, message, method, successCallback, failureCallback) {
    	// iOS plugin used to accept comma-separated phone numbers, keep the
    	// compatibility
    	if (typeof phone === 'string' && phone.indexOf(',') !== -1) {
    	    phone = phone.split(',');
    	}
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