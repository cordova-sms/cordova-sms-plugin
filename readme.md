# Cordova SMS Plugin

Cross-platform plugin for Cordova / PhoneGap to to easily send SMS. Available for **Android**, **iOS**, **Windows Phone 8** and **Windows 10 Universal**.

## Installing the plugin

Using the Cordova CLI and NPM, run:

```sh
$ cordova plugin add cordova-sms-plugin
```

It is also possible to install via repo url directly (unstable), run :

```sh
cordova plugin add https://github.com/cordova-sms/cordova-sms-plugin.git
```

## Using the plugin
HTML

```html
<input id="numberTxt" placeholder="Enter mobile number" value="" type="tel" />
<textarea id="messageTxt" placeholder="Enter message"></textarea>
<input type="button" onclick="app.sendSms()" value="Send SMS" />
```

Javascript

```js
var app = {
    sendSms: function() {
        var number = document.getElementById('numberTxt').value.toString(); /* iOS: ensure number is actually a string */
        var message = document.getElementById('messageTxt').value;
        console.log("number=" + number + ", message= " + message);

        //CONFIGURATION
        var options = {
            replaceLineBreaks: false, // true to replace \n by a new line, false by default
            android: {
                intent: 'INTENT'  // send SMS with the native android SMS messaging
                //intent: '' // send SMS without opening any other app
            }
        };

        var success = function () { alert('Message sent successfully'); };
        var error = function (e) { alert('Message Failed:' + e); };
        sms.send(number, message, options, success, error);
    }
};
```

On Android, two extra functions are exposed to know whether or not an app has permission and to request permission to send SMS (Android Marshmallow +).

```js
var app = {
    checkSMSPermission: function() {
        var success = function (hasPermission) { 
            if (hasPermission) {
                sms.send(...);
            }
            else {
                // show a helpful message to explain why you need to require the permission to send a SMS
                // read http://developer.android.com/training/permissions/requesting.html#explain for more best practices
            }
        };
        var error = function (e) { alert('Something went wrong:' + e); };
        sms.hasPermission(success, error);
    },
    requestSMSPermission: function() {
        var success = function (hasPermission) { 
            if (!hasPermission) {
                sms.requestPermission(function() {
                    console.log('[OK] Permission accepted')
                }, function(error) {
                    console.info('[WARN] Permission not accepted')
                    // Handle permission not accepted
                })
            }
        };
        var error = function (e) { alert('Something went wrong:' + e); };
        sms.hasPermission(success, error);
    }
};
```

## FAQ
#### `sms` is undefined

Please go through all the [closed issues about this subject](https://github.com/cordova-sms/cordova-sms-plugin/issues?q=is%3Aissue+is%3Aclosed+sms+label%3A%22sms+undefined%22). The issue is mostly coming from the way you installed the plugin, please double check everything before opening another issue.

#### Android: INTENT vs NO INTENT

If sending a SMS is a core feature of your application and you would like to send a SMS with `options = { android: { intent: '' } }`, you need to fill [this form](https://docs.google.com/forms/d/e/1FAIpQLSexGxix-00xgnBhPLDvxwjbTcYqHB7enz-cQVJIY4zLuJpRtQ/viewform). If it is not a core feature of your application, you have to use `options = { android: { intent: 'INTENT' } }`. Please, read [this page](https://support.google.com/googleplay/android-developer/answer/9047303) to learn more.

#### When building my project for android I get the following error: `cannot find symbol: cordova.hasPermission(string)`

You need to update `cordova-android` to the latest version (recommended), or at least to the version 5.1.1.

`cordova platform update android` or `cordova platform update android@5.1.1` 

#### Is the plugin available on [Adobe PhoneGap Build](https://build.phonegap.com)?

Yes, the plugin is available, please see instructions here: http://docs.phonegap.com/phonegap-build/configuring/plugins/. Use the npm or github source.

#### How can I receive SMS?

You can't receive SMS via this plugin. This plugin only sends SMS.

#### Android immediately passes success back to app? 

Please read [#issue 26](https://github.com/cordova-sms/cordova-sms-plugin/issues/26)

#### iOS closes the SMS dialog instantly. What's wrong?

Make sure the `number` argument passed is converted to string first using either `String(number)` or `number.toString()`. Notice that `toString()` won't work if the number argument is `null` or `undefined`.

#### I get this error. What's wrong?

```sh
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
```

BUILD FAILED

The problem is that you need to make sure that you set the target to android-19 or later in your ./platforms/android/project.properties file like this:

    # Project target.
    target=android-19


##### How can I send an sms in my iOS app without passing control to the native app like it can be done on Android?

This isn't possible on iOS. It requires that you show the user the native sms composer, to be able to send an sms.


## Donations

If your app is successful or if you are working for a company, please consider donating some beer money :beer::

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.me/dbaq/10)

Keep in mind that I am maintaining this repository on my free time so thank you for considering a donation. :+1:


## Contributing

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
