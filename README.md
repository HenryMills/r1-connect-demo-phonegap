# R1Connect PhoneGap/Cordova Plugin

### Platform Support

This plugin supports PhoneGap/Cordova apps running on iOS, Android and Windows Phone 8.

### Version Requirements

This plugin is meant to work with PhoneGap 3.0.0+ 

## Installation

#### Overview

This integration doc assumes you have already set up Google Play Services in your application project for android, which is needed to use Google Cloud Messaging (GCM), the notification gateway R1 Connect utilizes. Also you will need to have created the app you will be using in R1 Connect.

In order to use R1 Connect with your application you will need a project number (sender ID) and API key from Google. Please visit “GCM Getting Started” [here](http://developer.android.com/intl/ru/google/gcm/gs.html) and create a google project and an API key.

If you do not want to use Push on android you can skip Google Play Services setup (but we recommend not skip this setup); 


#### Automatic Installation using PhoneGap/Cordova CLI (iOS, Android and WP8)
1. Install this plugin using PhoneGap/Cordova cli:

		cordova plugin add https://github.com/radiumone/r1-connect-demo-phonegap


2. Modify the www/config.xml ( res/xml/config.xml for android ) directory to contain (replacing with your configuration settings) :
Required (for Emitter only):

	<preference name="com.radiumone.r1connect.applicationId" value="Your application Id" />

If you want to use Push on all platforms:

	<preference name="com.radiumone.r1connect.clientKey" value="Your client Key" />

If you want to use Push on Android:

	<preference name="com.radiumone.r1connect.senderId" value="Your GCM sender id" />

If you want to use Push on WP8:


    <preference name="com.radiumone.r1connect.MPNSServiceName" value="[YOUR SERVICE NAME]" />
    <preference name="com.radiumone.r1connect.MPNSChannelName" value="[YOUR PUSH CHANNEL]" />
    <preference name="com.radiumone.r1connect.TileAllowedDomains" value="[TILE DOMAIN 1], [TILE DOMAIN 2]" />
		
If you have set up Google Play Services add this line

	<meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
		

#### iOS manual installation (unnecessary if installed automatically)
1. Copy all files from src/ios with subfolders to your project
1. Add LibR1Connect.a as a library (Target -> Build Phases -> Link Binary With Libraries)
1. Make sure the following frameworks are linked (Target -> Build Phases -> Link Binary With Libraries):

		libsqlite3.dylib
        UIKit.framework
        Foundation.framework
        CoreGraphics.framework
        AdSupport.framework
        CoreTelephony.framework
        CoreLocation.framework
        SystemConfiguration.framework
        Security.framework

1. Modify the cordova config.xml file to include the R1ConnectPlugin and preferences:


        <feature name="R1ConnectPlugin">
        	<param name="ios-package" value="R1ConnectPlugin" />
	        <param name="onload" value="true" />
    	</feature>
        
		<preference name="com.radiumone.r1connect.applicationId" value="Your application Id" />
	    <preference name="com.radiumone.r1connect.clientKey" value="Your client Key (For Push only)" />

1. Copy www/R1Connect.js into the project's www directory

1. Require the R1Connect module `var R1Connect = require('<Path to R1Connect.js>')`

#### Android manual installation (unnecessary if installed automatically)
1. Copy src/Android/R1ConnectPlugin.java file to your projects src/com/radiumone/cordova/plugin/ directory
1. Copy src/Android/*.jar files to your projects libs directory
2. Copy src/Android/r1connect.properties to your assets directory

1. Modify the AndroidManifest.xml to include these permissions:


        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    	<uses-permission android:name="android.permission.VIBRATE" />
    	<uses-permission android:name="android.permission.GET_ACCOUNTS" />
    	<uses-permission android:name="android.permission.WAKE_LOCK" />
    	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    	
    	<!-- Please, replace $PACKAGE_NAME with your apps package name-->
    	<uses-permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" />
    	
    	<!-- Please, replace $PACKAGE_NAME with your apps package name-->
    	<permission android:name="$PACKAGE_NAME.permission.C2D_MESSAGE" android:protectionLevel="signature" /> 
    	   

1. Modify the AndroidManifest.xml Application section to include:


        <receiver android:exported="true" android:name="com.radiumone.emitter.gcm.R1GCMPushReceiver" android:permission="com.google.android.c2dm.permission.SEND">
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                
                <!-- Please, replace $PACKAGE_NAME with your apps package name-->
                <category android:name="$PACKAGE_NAME" />
            </intent-filter>
        </receiver>
        <receiver android:exported="false" android:name="com.radiumone.emitter.push.R1PushBroadcastReceiver">
            <intent-filter>
                <action android:name="com.radiumone.r1push.OPENED_INTERNAL" />
            </intent-filter>
        </receiver>
        <service android:name="com.radiumone.emitter.push.R1ConnectService" />
        <service android:name="com.radiumone.emitter.location.LocationService" />
        
If you have set up Google Play Services add this line

	<meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />


1. Modify the cordova config.xml file to include the PushNotificationPlugin:


        <feature name="R1ConnectPlugin">
        	<param name="android-package" value="com.radiumone.cordova.plugin.R1ConnectPlugin" />
        <param name="onload" value="true" />
    	</feature>

        <preference name="com.radiumone.r1connect.applicationId" value="Your application Id" />
	    <preference name="com.radiumone.r1connect.clientKey" value="Your client Key (For Push only)" />
	    <preference name="com.radiumone.r1connect.senderId" value="Your google sender id (For Push only)" />

1. Copy www/R1Connect.js into the project's www directory

1. Require the R1Connect module `var R1Connect = require('<Path to R1Connect.js>')`

#### Windows Phone 8 manual installation (unnecessary if installed automatically)

1. Copy all files from src/wp8 with subfolders to your project
2. Select PROJECT -> Add Reference...
3. In opened "Reference Manager" window press "Browse"
4. Select copied libraries *R1ConnectLibrary.dll*, *Newtonsoft.Json.dll* and *protobuf-net.dll*
5. Check your application Capabilities. For it
6. Open [Your project]->Properties->WMAppManifest.xml in Solution Explorer.
7. Select *Capabilities* tab
8. Check ID_CAP_NETWORKING, ID_CAP_IDENTITY_DEVICE, ID_CAP_LOCATION
9. Check ID_CAP_PUSH_NOTIFICATION if you want use R1 Connect Push


1. Modify the cordova config.xml file to include the R1ConnectPlugin and preferences:

	<feature name="R1ConnectPlugin">
		<param name="wp-package" value="R1ConnectPlugin" />
		<param name="onload" value="true" />
	</feature>

    <preference name="com.radiumone.r1connect.applicationId" value="[Your application Id]" />
    <preference name="com.radiumone.r1connect.clientKey" value="[Your client key]" />
    <preference name="com.radiumone.r1connect.senderId" value="Your GCM sender id" />
    <preference name="com.radiumone.r1connect.MPNSServiceName" value="[YOUR SERVICE NAME]" />
    <preference name="com.radiumone.r1connect.MPNSChannelName" value="[YOUR PUSH CHANNEL]" />

1. Copy www/R1Connect.js into the project's www directory

1. Require the R1Connect module `var R1Connect = require('<Path to R1Connect.js>')`

## R1Connect methods

### R1SDK

#### isStarted

*Callback arguments:* (bool isStarted)

Indicates whether the SDK is started. The SDK started automatically if you setted up config.xml.

	R1SDK.isStarted(function (isStarted) {
		//...
    });
    
#### setAdvertisingEnabled

Indicates (to the SDK) whether or not the application is displaying advertisiments. A value of true prevents the SDK from accessing IDFA to comply with Apple's advertising policy when advertisements are served within the application (outside of the SDK).

	R1SDK.setAdvertisingEnabled(true);

#### isAdvertisingEnabled

*Callback arguments:* (bool isAdvertisingEnabled)

Indicates (to the SDK) whether or not the application is displaying advertisiments. A value of true prevents the SDK from accessing IDFA to comply with Apple's advertising policy when advertisements are served within the application (outside of the SDK).

	R1SDK.isAdvertisingEnabled(function (isAdvertisingEnabled) {
		//...
    });


#### setApplicationUserId

Set optional current user identifier.

	R1SDK.setApplicationUserId("Your application user id");

#### getApplicationUserId

*Callback arguments:* (string applicationUserId)

Get last setted application user identifier.

	R1SDK.getApplicationUserId(function (applicationUserId) {
		//...
    });

### R1LocationService

#### setEnabled

Enable or disable the location service for giving location in the SDK.

	R1LocationService.setEnabled(true);

#### isEnabled

*Callback arguments:* (bool isEnabled)

Indicates whether R1LocationService is enabled.

	R1LocationService.isEnabled(function (isEnabled) {
		//...
    });

#### setAutoupdateTimeout

When location service is enabled location information will be sent automatically. However, locationService doesn’t fetch the location constantly. For instance, when the location is received the SDK will turn off the location in CLLocationManager and wait 10 minutes (by default) before attempting to retrieve it again. You can change this value.

	R1LocationService.setAutoupdateTimeout(1200); // 20 minutes

#### getAutoupdateTimeout

*Callback arguments:* (float autoupdateTimeout)

Get currently used autoupdate timeout.

	R1LocationService.getAutoupdateTimeout(function (autoupdateTimeout) {
		//...
    });

#### getState

*Callback arguments:* (string state)

Get current state of R1LocationService.

	R1LocationService.getState(function (state) {
		//...
    });

Possible state values:

- "Disabled"
- "Off"
- "Searching"
- "Has Location"
- "Wait Next Update"

#### getCoordinate

*Callback arguments:* (object coordinate)

Get currently used user coordinate.

	R1LocationService.getCoordinate(function (coordinate) {
		if (coordinate == null)
		{
			// Location not received yet
		}else
		{
			// coordinate.latitude
			// coordinate.longitude
		}		
		//...
    });

#### updateNow

If R1LocationService is in "Wait Next Update" state, you can update the location manually.

	R1LocationService.updateNow();
	
#### Events

##### R1LocationService.state

Notify the location service *state* has changed

	document.addEventListener("R1LocationService.state", function(event) {
		// event.state 
    }, false);

##### R1LocationService.coordinate

Notify the location service *coordinate* has changed

	document.addEventListener("R1LocationService.coordinate", function(event) {
		// event.latitude, event.longitude
    }, false);
    
### R1Emitter

#### isStarted

*Callback arguments:* (bool isStarted)

Indicates whether the Emitter has started. The Emitter starts automatically if you set the up *com.radiumone.r1connect.applicationId* key in config.xml.

	R1Emitter.isStarted(function (isStarted) {
		//...
    });
    
#### setAppName

The application name associated with this emitter. By default, this property is populated with the `CFBundleName` string from the application bundle. If you wish to override this property, you must do so before making any tracking calls.

	R1Emitter.setAppName("Custom application name");

#### getAppName

*Callback arguments:* (string appName)

Get last setted application name or `CFBundleName` by default.

	R1Emitter.getAppName(function (appName) {
		//...
    });

#### setAppId

The application identifier associated with this emitter.  If you wish to set this property, you must do so before making any tracking calls. Note: that this is not your app's bundle id, like e.g. com.example.appname.

	R1Emitter.setAppId("12345678");

#### getAppId

*Callback arguments:* (string appId)

Get last setted app id.

	R1Emitter.getAppId(function (appId) {
		//...
    });

#### setAppVersion

The application version associated with this emitter. By default, this property is populated with the `CFBundleShortVersionString` string from the application bundle. If you wish to override this property, you must do so before making any tracking calls.

	R1Emitter.setAppId("12345678");

#### getAppVersion

*Callback arguments:* (string appVersion)

Get last setted app version or `CFBundleShortVersionString` string from the application bundle.

	R1Emitter.getAppVersion(function (appVersion) {
		//...
    });

#### setSessionTimeout

Indicates how long, in seconds, the application must transition to the inactive or background state for before the tracker will automatically indicate the start of a new session when the app becomes active again by setting sessionStart to true. For example, if this is set to 30 seconds, and the user receives a phone call that lasts for 45 seconds while using the app, upon returning to the app, the sessionStart parameter will be set to true. If the phone call instead lasted 10 seconds, sessionStart will not be modified.
 
By default, this is 30 seconds.

	R1Emitter.setSessionTimeout(15); // 15 seconds

#### getSessionTimeout

*Callback arguments:* (float sessionTimeout)

Get last setted session timeout or default value

	R1Emitter.getSessionTimeout(function (sessionTimeout) {
		//...
    });

#### Emitter EventsThe R1 Connect SDK will automatically capture some generic events, but in order to get the most meaningful data on how users interact with your app the SDK also offers pre-built user-defined events for popular user actions as well as the ability to create your own custom events.

##### State Events

Some events are emitted automatically when the state of the application is changed by the OS and, therefore, they do not require any additional code to be written in the app in order to work out of the box:**Launch** - emitted when the app starts
**First Launch** - emitted when the app starts for the first time**First Launch After Update** - emitted when the app starts for the first time after a version upgrade**Suspend** - emitted when the app is put into the background state**Resume** - emitted when the app returns from the background state

##### Pre-Defined Events

Pre-Defined Events are also helpful in measuring certain metrics within the apps and do not require further developer input to function. These particular events below are used to help measure app open events and track Sessions.
**Application Opened** - This event is very useful for push notifications and can measure when your app is opened after a message is sent.
**Session Start** - As the name implies the Session Start event is used to start a new session.

**Session End** - The Session End event is used to end a session and passes with it a Session Length attribute that calculates the session length in seconds.

##### User-Defined Events

User-Defined Events are not sent automatically so it is up to you if you want to use them or not. They can provide some great insights on popular user actions if you decide to track them.  In order to set this up the application code needs to include the emitter callbacks in order to emit these events.

*Note: The last argument in all of the following emitter callbacks, otherInfo, is a dictionary of “key”,”value” pairs or nil*

**User Info**

This event enables you to send user profiles to the backend.

	var userInfo = {"userID":"userId", "userName":"userName", "email":"email",
                    "firstName":"firstName", "lastName":"lastName",
                    "streetAddress":"streetAddress", "phone":"phone",
                    "city":"city", "state":"state", "zip":"zip"};

	R1Emitter.emitUserInfo(userInfo, {"custom_key":"value"});

**Login**

Tracks a user login within the app

	R1Emitter.emitLogin("userId", "user_name", {"custom_key":"value"});

**Registration**

Records a user registration within the app	R1Emitter.emitRegistration("userId", "userName", "country", "state", "city", {"custom_key":"value"});**Facebook connect**

Allows access to Facebook services

	R1Emitter.emitFBConnect([{name:"photos", granted:true}], {"custom_key":"value"});

**Twitter connect**

Allows access to Twitter services
	R1Emitter.emitTConnect("12345", "user_name", [{name:"photos", granted:true}], {"custom_key":"value"});

**Upgrade**

Tracks an application version upgrade

	R1Emitter.emitUpgrade({"custom_key":"value"});

**Trial Upgrade**

Tracks an application upgrade from a trial version to a full version	R1Emitter.emitTrialUpgrade({"custom_key":"value"});

**Screen View**

Basically, a page view, it provides info about that screen

	R1Emitter.emitScreenView("title", "description", "http://www.example.com/path", "example.com", "path", {"custom_key":"value"});

##### E-Commerce Events

**Transaction**

	R1Emitter.emitTransaction("transaction_id", "store_id", "store_name", "cart_id", "order_id", 1.5, "USD", 10.5, 12.0, {"custom_key":"value"});

**TransactionItem**

	var lineItem = {productID:"product_id", productName:"product_name", quantity:5, unitOfMeasure:"unit", msrPrice:10, pricePaid:10, currency:"USD", itemCategory:"category"};

	R1Emitter.emitTransactionItem("transaction_id", lineItem, {"custom_key":"value"});


**Create Cart**

	R1Emitter.emitCartCreate("cart_id", {"custom_key":"value"});

**Delete Cart**

	R1Emitter.emitCartDelete("cart_id", {"custom_key":"value"});

**Add To Cart**

	var lineItem = {productID:"product_id", productName:"product_name", quantity:5, unitOfMeasure:"unit", msrPrice:10, pricePaid:10, currency:"USD", itemCategory:"category"};

	R1Emitter.emitAddToCart("cart_id", lineItem, {"custom_key":"value"});

**Delete From Cart**

	var lineItem = {productID:"product_id", productName:"product_name", quantity:5, unitOfMeasure:"unit", msrPrice:10, pricePaid:10, currency:"USD", itemCategory:"category"};

	R1Emitter.emitDeleteFromCart("cart_id", lineItem, {"custom_key":"value"});

#####Custom Events
With custom events you have the ability to create and track specific events that are more closely aligned with your app. If planned and structured correctly, custom events can be strong indicators of user intent and behavior. Some examples include pressing the “like” button, playing a song, changing the screen mode from portrait to landscape, and zooming in or out of an image. These are all actions by the user that could be tracked with events.To include tracking of custom events for the mobile app, the following callbacks need to be included in the application code:

	// Emits a custom event without parameters
	R1Emitter.emitEvent("Your custom event name");
	
	// Emits a custom event with parameters
	R1Emitter.emitEvent(@"Your custom event name", {"key":"value"});

### R1Push

#### isStarted

*Callback arguments:* (bool isStarted)

Indicates whether Push has started. Push starts automatically if you set up all the keys in config.xml.

	R1Push.isStarted(function (isStarted) {
		//...
    });

#### setEnabled

Enable or disable R1Push.

	R1Push.setEnabled(true);

#### isEnabled

*Callback arguments:* (bool isEnabled)

Indicates whether R1Push is enabled.

	R1Push.isEnabled(function (isEnabled) {
		//...
    });
    
#### getDeviceToken

*Callback arguments:* (String deviceToken)

Get the push identifier for the device. Can be null if R1Push not enabled.

	R1Push.getDeviceToken(function (deviceToken) {
		//...
    });

#### setBadgeNumber

**Note:** iOS only

Set application badge number.

	R1Push.setBadgeNumber(2);

#### getBadgeNumber

**Note:** iOS only. For Android the callback will always be a 0 value.

*Callback arguments:* (int badgeNumber)

Get currently used badge number.

	R1Push.getBadgeNumber(function (badgeNumber) {
		//...
    });

#### setTags

Set tags for the device.

	R1Push.setTags(["Tag1", "Tag2"]);

#### addTag

Add tag for the device

	R1Push.addTag("Tag1");

#### removeTag

Remove tag from the device

	R1Push.removeTag("Tag1");

#### getTags

*Callback arguments:* (Array tags)

Get the current tags.

	R1Push.getTags(function (tags) {
		//...
    });

#### Events

##### R1Push.deviceToken

Notify the push *deviceToken* has changed

	document.addEventListener("R1Push.deviceToken", function(event) {
		// event.deviceToken
    }, false);

##### R1Push.foregroundNotification

For iOS and Android:
Notify when a push notification is recieved when the application is running in the foreground

For Windows Phone 8:
Notify when a Toast notification is recieved when the application is running in the foreground

	document.addEventListener("R1Push.foregroundNotification", function(event) {
		// event - object with full information about notification
		alert(event);
    }, false);
    
##### R1Push.backgroundNotification

Only for iOS and Android!
Notify when a push notification is received when the application is running in the background and the app is opened by this notification

	document.addEventListener("R1Push.backgroundNotification", function(event) {
		// event - object with full information about notification
		alert(event.aps.alert);
    }, false);

##### R1Push.foregroundHttpNotification

Only for Windows Phone 8:
Notify when a Raw notification is recieved when the application is running in the foreground

	document.addEventListener("R1Push.foregroundHttpNotification", function(event) {
		// event - object with full information about notification
		alert(event);
    }, false);
    
## Example

An example can be found in Demo. To run it, copy the files:

- Demo/index.html to www/index.html
- Demo/css/* to www/css
- Demo/js/* to www/js