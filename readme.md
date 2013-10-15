phonegap-sms-plugin
=====================

This Android Phonegap plugin allows you to easily send SMS in android using both native SMS Manager or by invoking the default android SMS app. This plugin works with PhoneGap 3.x version.

The Android portion was forked from https://github.com/javatechig/phonegap-sms-plugin by javatechig and then modified to upgrade it to phonegap 3.0.

The iOS portion was copied from https://github.com/phonegap/phonegap-plugins by Jesse MacFadyen and then modified slightly to work with this plugin and phonegap 3.x.



Installation
=================

Using the Phonegap CLI run:

    phonegap local plugin add https://github.com/aharris88/phonegap-sms-plugin.git

This will place the plugin in your plugins directory and update your android.json file that keeps track of installed plugins.

Then when you run:

	phonegap build android

or
	phonegap install android

phonegap will put the necessary files into the platforms/android directory. It will update AndroidManifest.xml, res/xml/config.xml, and it will add the src/org/apache/cordova/sms directory.

Example Usage
=================

HTML

	<input name="" id="numberTxt" placeholder="Enter mobile number" value="" type="tel" />
    <br/>
    <textarea name="" id="messageTxt" placeholder="Enter message"></textarea>
    <br/>
    <input id="btnDefaultSMS" type="button" value="Send SMS" />

Javascript
Note that the following code uses jquery.

	var app = {
	    // Application Constructor
	    initialize: function() {
	        this.bindEvents();
	    },
	    // Bind Event Listeners
	    //
	    // Bind any events that are required on startup. Common events are:
	    // 'load', 'deviceready', 'offline', and 'online'.
	    bindEvents: function() {
	        document.addEventListener('deviceready', this.onDeviceReady, false);
	    },
	    // deviceready Event Handler
	    //
	    // The scope of 'this' is the event. In order to call the 'receivedEvent'
	    // function, we must explicity call 'app.receivedEvent(...);'
	    onDeviceReady: function() {
	        $("#btnDefaultSMS").click(function(){
	            alert("click");
	            var number = $("#numberTxt").val();
	            var message = $("#messageTxt").val();
	            var intent = "INTENT"; //leave empty for sending sms using default intent
	            var success = function () { alert('Message sent successfully'); };
	            var error = function (e) { alert('Message Failed:' + e); };
	            sms.send(number, message, intent, success, error);
	        });
	    }
	};

Contributing
=================

I believe that everything is working, feel free to put in an issue or to fork and make pull requests if you want to add a new feature.

Copyright from android code
=================

The MIT License (MIT)

Copyright (c) 2013 javatechig

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

Copyright from iOS code
=================

The MIT License

Copyright (c) 2010 Jesse MacFadyen

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
