var sms = {
  send: function(phone, message, method, successCallback, failureCallback) {
    phone = sms.convertPhoneToArray(phone);

    cordova.exec(
      successCallback,
      failureCallback,
      'Sms',
      'send',
      [phone, message, method]
    );
  },

  convertPhoneToArray: function(phone) {
    if(typeof phone === 'string' && phone.indexOf(',') !== -1) {
      phone = phone.split(',');
    }
    if(Object.prototype.toString.call(phone) !== '[object Array]') {
      phone = [phone];
    }
    return phone;
  }
};

module.exports = sms;