phonegap-sms-plugin
=====================

Note! I'm currently testing this plugin. but if it doesn't work, and you know how to make it work, please let me know how to fix it or send me a pull request.

This Android Phonegap plugin allows you to easily send SMS in android using both native SMS Manager or by invoking the default android SMS app. This plugin works with PhoneGap 3.x version.

This was forked from https://github.com/javatechig/phonegap-sms-plugin so that I could upgreade it to phonegap 3.0.

Installation
=================

Using the Phonegap CLI run:

    phonegap local plugin add https://github.com/aharris88/phonegap-sms-plugin

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