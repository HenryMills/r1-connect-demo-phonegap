cordova.define("com.radiumone.R1Connect.phonegap.R1Connect", function(require, exports, module) { var R1SDK = function() {}

R1SDK.prototype.call_native = function(callback, name, args) {
     if (arguments.length == 2) {
          args = []
     }
     return cordova.exec(callback, null, "R1ConnectPlugin", name, args);
}

R1SDK.prototype.isIOS = function() {
     if (device.platform == "iOS")
          return true;
     if (device.platform == "iPhone" || device.platform == "iPod touch")
          return true;
     if (device.platform == "iPad")
          return true;
     return false;
}

R1SDK.prototype.isWP = function() {
     if (device.platform == null || device.platform === undefined)
          return false;

     if (device.platform.indexOf("Win") == 0)
          return true;

     return false;
}

R1SDK.prototype.setApplicationUserId = function(applicationUserId) {
     this.call_native(null, "setApplicationUserId", [applicationUserId]);
}

R1SDK.prototype.getApplicationUserId = function(callback) {
     this.call_native(callback, "getApplicationUserId");
}

R1SDK.prototype.setAdvertisingEnabled = function(advertisingEnabled) {
     this.call_native(null, "setAdvertisingEnabled", [advertisingEnabled]);
}

R1SDK.prototype.isAdvertisingEnabled = function(callback) {
     this.call_native(callback, "isAdvertisingEnabled");
}

R1SDK.prototype.isStarted = function(callback) {
     this.call_native(callback, "isStarted");
}

window.R1SDK = new R1SDK();

var R1LocationService = function() {}

R1LocationService.prototype.setEnabled = function(locationEnabled) {
     window.R1SDK.call_native(null, "location_setEnabled", [locationEnabled]);
}

R1LocationService.prototype.isEnabled = function(callback) {
     window.R1SDK.call_native(callback, "location_isEnabled");
}

R1LocationService.prototype.setAutoupdateTimeout = function(autoupdateTimeout) {
     window.R1SDK.call_native(null, "location_setAutoupdateTimeout", [autoupdateTimeout]);
}

R1LocationService.prototype.getAutoupdateTimeout = function(callback) {
     window.R1SDK.call_native(callback, "location_getAutoupdateTimeout");
}

R1LocationService.prototype.getState = function(callback) {
     window.R1SDK.call_native(callback, "location_getState");
}

R1LocationService.prototype.getCoordinate = function(callback) {
     window.R1SDK.call_native(callback, "location_getCoordinate");
}

R1LocationService.prototype.updateNow = function() {
     window.R1SDK.call_native(null, "location_updateNow");
}

window.R1LocationService = new R1LocationService();

var R1Emitter = function() {}

R1Emitter.prototype.isStarted = function(callback) {
     window.R1SDK.call_native(callback, "emitter_isStarted");
}

R1Emitter.prototype.setAppName = function(appName) {
     window.R1SDK.call_native(null, "emitter_setAppName", [appName]);
}

R1Emitter.prototype.getAppName = function(callback) {
     window.R1SDK.call_native(callback, "emitter_getAppName");
}

R1Emitter.prototype.setAppId = function(appId) {
     window.R1SDK.call_native(null, "emitter_setAppId", [appId]);
}

R1Emitter.prototype.getAppId = function(callback) {
     window.R1SDK.call_native(callback, "emitter_getAppId");
}

R1Emitter.prototype.setAppVersion = function(appVersion) {
     window.R1SDK.call_native(null, "emitter_setAppVersion", [appVersion]);
}

R1Emitter.prototype.getAppVersion = function(callback) {
     window.R1SDK.call_native(callback, "emitter_getAppVersion");
}

R1Emitter.prototype.setSessionTimeout = function(sessionTimeout) {
     window.R1SDK.call_native(null, "emitter_setSessionTimeout", [sessionTimeout]);
}

R1Emitter.prototype.getSessionTimeout = function(callback) {
     window.R1SDK.call_native(callback, "emitter_getSessionTimeout");
}

R1Emitter.prototype.emitEvent = function(eventName, eventParameters) {
     window.R1SDK.call_native(null, "emitter_emitEvent", [eventName, eventParameters]);
}

R1Emitter.prototype.emitUserInfo = function(userInfo, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitUserInfo", [userInfo, otherInfo]);
}

R1Emitter.prototype.emitLogin = function(userID, userName, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitLogin", [userID, userName, otherInfo]);
}

R1Emitter.prototype.emitRegistration = function(userID, userName, country, state, city, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitRegistration", [userID, userName, country, state, city, otherInfo]);
}

R1Emitter.prototype.emitFBConnect = function(permissions, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitFBConnect", [permissions, otherInfo]);
}

R1Emitter.prototype.emitTConnect = function(userID, userName, permissions, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitTConnect", [userID, userName, permissions, otherInfo]);
}

R1Emitter.prototype.emitTransaction = function(transactionID, storeID, storeName, cartID, orderID, totalSale, currency, shippingCosts, transactionTax, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitTransaction", [transactionID, storeID, storeName, cartID, orderID, totalSale, currency, shippingCosts, transactionTax, otherInfo]);
}

R1Emitter.prototype.emitTransactionItem = function(transactionID, lineItem, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitTransactionItem", [transactionID, lineItem, otherInfo]);
}

R1Emitter.prototype.emitCartCreate = function(cartID, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitCartCreate", [cartID, otherInfo]);
}

R1Emitter.prototype.emitCartDelete = function(cartID, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitCartDelete", [cartID, otherInfo]);
}

R1Emitter.prototype.emitAddToCart = function(cartID, lineItem, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitAddToCart", [cartID, lineItem, otherInfo]);
}

R1Emitter.prototype.emitDeleteFromCart = function(cartID, lineItem, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitDeleteFromCart", [cartID, lineItem, otherInfo]);
}

R1Emitter.prototype.emitUpgrade = function(otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitUpgrade", [otherInfo]);
}

R1Emitter.prototype.emitTrialUpgrade = function(otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitTrialUpgrade", [otherInfo]);
}

R1Emitter.prototype.emitScreenView = function(documentTitle, contentDescription, documentLocationUrl, documentHostName, documentPath, otherInfo) {
     window.R1SDK.call_native(null, "emitter_emitScreenView", [documentTitle, contentDescription, documentLocationUrl, documentHostName, documentPath, otherInfo]);
}

var R1Push = function() {}

R1Push.prototype.isStarted = function(callback) {
     window.R1SDK.call_native(callback, "push_isStarted");
}

R1Push.prototype.setEnabled = function(pushEnabled) {
     window.R1SDK.call_native(null, "push_setPushEnabled", [pushEnabled]);
}

R1Push.prototype.isEnabled = function(callback) {
     window.R1SDK.call_native(callback, "push_isPushEnabled");
}

R1Push.prototype.getDeviceToken = function(callback) {
     window.R1SDK.call_native(callback, "push_getDeviceToken");
}

R1Push.prototype.setBadgeNumber = function(badgeNumber) {
     if (!window.R1SDK.isIOS())
          return;

     window.R1SDK.call_native(null, "push_setBadgeNumber", [badgeNumber]);
}

R1Push.prototype.getBadgeNumber = function(callback) {
     if (!window.R1SDK.isIOS()) {
          callback(0);
          return;
     }

     window.R1SDK.call_native(callback, "push_getBadgeNumber");
}

R1Push.prototype.setTags = function(tags) {
     window.R1SDK.call_native(null, "push_setTags", [tags])
}

R1Push.prototype.addTag = function(tag) {
     window.R1SDK.call_native(null, "push_addTag", [tag])
}

R1Push.prototype.removeTag = function(tag) {
     window.R1SDK.call_native(null, "push_removeTag", [tag])
}

R1Push.prototype.getTags = function(callback) {
     window.R1SDK.call_native(callback, "push_getTags");
}

window.R1Emitter = new R1Emitter();
window.R1Push = new R1Push();
});
