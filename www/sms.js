var sms = {
  send: function(phone, message, method, successCallback, failureCallback) {
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