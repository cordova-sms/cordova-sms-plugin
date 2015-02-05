cordova-sms-plugin
=====================

This Cordova plugin allows you to easily send SMS in android, iOS, and Windows Phone 8. In Android you can use either the native SMS Manager or by invoking the default android SMS app.

This plugin works with PhoneGap 3.x version.

Installation
============

Using the Cordova CLI, run:

    cordova plugin add https://github.com/cordova-sms/cordova-sms-plugin.git

This will place the plugin in your plugins directory and update your android.json file that keeps track of installed plugins.

Then when you run:

    cordova build android


    cordova run android

or

    corcova emulate android

Cordova will put the necessary files into the platforms/android directory. It will update AndroidManifest.xml, res/xml/config.xml, and it will add the src/org/apache/cordova/sms directory.

Example Usage
=============

HTML

    <input name="" id="numberTxt" placeholder="Enter mobile number" value="" type="tel" />
    <textarea name="" id="messageTxt" placeholder="Enter message"></textarea>
    <input type="button" onclick="app.sendSms()" value="Send SMS" />

Javascript

    var app = {
        sendSms: function() {
            alert('click');
            var number = document.getElementById('numberTxt').value;
            var message = document.getElementById('messageTxt').value;
            alert(number);
            alert(message);
            var intent = 'INTENT'; //leave empty for sending sms using default intent
            var success = function () { alert('Message sent successfully'); };
            var error = function (e) { alert('Message Failed:' + e); };
            sms.send(number, message, intent, success, error);
        }
    };

Frequently Asked Questions
==========================

### How can I send an sms in my iOS app without passing control to the native app like it can be done on Android?

This isn't possible on iOS. It requires that you show the user the native sms composer, to be able to send an sms.

Contributing
============

I believe that everything is working, feel free to put in an issue or to fork and make pull requests if you want to add a new feature.

Things you can fix:
* Allow for null number to be passed in
  Right now, it breaks when a null value is passed in for a number, but it works if it's a blank string, and allows the user to pick the number
  It should automatically convert a  null value to an empty string

History
=======

The Android portion was forked from https://github.com/javatechig/phonegap-sms-plugin by javatechig and then modified to upgrade it to phonegap 3.0.

The iOS portion was copied from https://github.com/phonegap/phonegap-plugins by Jesse MacFadyen and then modified slightly to work with this plugin and phonegap 3.x.

The Windows Phone 8 part was contributed by [fredrikeldh](https://github.com/fredrikeldh)

License
=======

The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
