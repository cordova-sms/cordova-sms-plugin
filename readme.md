#Cordova SMS Plugin

Cross-platform plugin for Cordova / PhoneGap to to easily send SMS. Available for **Android**, **iOS**, **Windows Phone 8** and **Windows 10 Universal (BETA)**.

This plugin works with Cordova 3.x, 4.x and 5.x version.

##Installing the plugin

Using the Cordova CLI and NPM, run:

    cordova plugin add cordova-sms-plugin

It is also possible to install via repo url directly (unstable), run :

    cordova plugin add https://github.com/cordova-sms/cordova-sms-plugin.git

##Using the plugin
HTML

    <input id="numberTxt" placeholder="Enter mobile number" value="" type="tel" />
    <textarea id="messageTxt" placeholder="Enter message"></textarea>
    <input type="button" onclick="app.sendSms()" value="Send SMS" />

Javascript

    var app = {
        sendSms: function() {
            var number = document.getElementById('numberTxt').value;
            var message = document.getElementById('messageTxt').value;
            alert(number);
            alert(message);
  
            //CONFIGURATION
            var options = {
                replaceLineBreaks: false, // true to replace \n by a new line, false by default
                android: {
                    intent: 'INTENT'  // send SMS with the native android SMS messaging
                    //intent: '' // send SMS without open any other app
                }
            };

            var success = function () { alert('Message sent successfully'); };
            var error = function (e) { alert('Message Failed:' + e); };
            sms.send(number, message, options, success, error);
        }
    };

##FAQ

###Is the plugin available on [Adobe PhoneGap Build](https://build.phonegap.com)?

Yes, the plugin is available, please see instructions here: https://build.phonegap.com/plugins/1999.

###I get this error. What's wrong?

    compile:
        [javac] Compiling 4 source files to /Users/username/MyProject/platforms/android/bin/classes
        [javac] /Users/username/MyProject/platforms/android/src/org/apache/cordova/plugin/sms/Sms.java:15: cannot find symbol
        [javac] symbol  : class Telephony
        [javac] location: package android.provider
        [javac] import android.provider.Telephony;
        [javac]                        ^
        [javac] /Users/username/MyProject/platforms/android/src/org/apache/cordova/plugin/sms/Sms.java:60: cannot find symbol
        [javac] symbol  : variable KITKAT
        [javac] location: class android.os.Build.VERSION_CODES
        [javac]     if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        [javac]                                                    ^
        [javac] /Users/username/MyProject/platforms/android/src/org/apache/cordova/plugin/sms/Sms.java:61: package Telephony does not exist
        [javac]       String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this.cordova.getActivity());
        [javac]                                               ^
        [javac] 3 errors

    BUILD FAILED

The problem is that you need to make sure that you set the target to android-19 or later in your ./platforms/android/project.properties file like this:

    # Project target.
    target=android-19


#### How can I send an sms in my iOS app without passing control to the native app like it can be done on Android?

This isn't possible on iOS. It requires that you show the user the native sms composer, to be able to send an sms.

Contributing
============

I believe that everything is working, feel free to put in an issue or to fork and make pull requests if you want to add a new feature.

Things you can fix:
* Allow for null number to be passed in
  Right now, it breaks when a null value is passed in for a number, but it works if it's a blank string, and allows the user to pick the number
  It should automatically convert a  null value to an empty string

Thanks for considering contributing to this project.

### Finding something to do

Ask, or pick an issue and comment on it announcing your desire to work on it. Ideally wait until we assign it to you to minimize work duplication.

### Reporting an issue

- Search existing issues before raising a new one.

- Include as much detail as possible.

### Pull requests

- Make it clear in the issue tracker what you are working on, so that someone else doesn't duplicate the work.

- Use a feature branch, not master.

- Rebase your feature branch onto origin/master before raising the PR.

- Keep up to date with changes in master so your PR is easy to merge.

- Be descriptive in your PR message: what is it for, why is it needed, etc.

- Make sure the tests pass

- Squash related commits as much as possible.

### Coding style

- Try to match the existing indent style.

- Don't mix platform-specific stuff into the main code.




## History

-  The Android portion was forked from https://github.com/javatechig/phonegap-sms-plugin by @javatechig and then modified to upgrade it to phonegap 3.0.
- The iOS portion was copied from https://github.com/phonegap/phonegap-plugins by Jesse MacFadyen and then modified slightly to work with this plugin and phonegap 3.x by @aharris88.
- The Windows Phone 8 part was contributed by [fredrikeldh](https://github.com/fredrikeldh)
- This repository is now maintained by @dbaq.

## License

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
